package com.squareup.benchmarks.performance.complex.poetry

import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import androidx.tracing.Trace
import com.squareup.benchmarks.performance.complex.poetry.instrumentation.EventHandlingTracingInterceptor
import com.squareup.benchmarks.performance.complex.poetry.instrumentation.PerformanceTracingInterceptor
import com.squareup.benchmarks.performance.complex.poetry.instrumentation.Resettable
import com.squareup.benchmarks.performance.complex.poetry.instrumentation.SimulatedPerfConfig
import com.squareup.sample.container.SampleContainers
import com.squareup.sample.poetry.model.Poem
import com.squareup.workflow1.WorkflowInterceptor
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.ViewEnvironment.Companion.EMPTY
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowLayout
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.container.withEnvironment
import com.squareup.workflow1.ui.renderWorkflowIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@OptIn(WorkflowUiExperimentalApi::class)
private val viewEnvironment = EMPTY + (ViewRegistry to SampleContainers)

class PerformancePoetryActivity : AppCompatActivity() {

  class NavigationHolder(
    val originScreenName: String?,
    var destinationScreenName: String,
  )

  private val mainHandler = Handler(Looper.getMainLooper())
  private var navigationInFlight: NavigationHolder? = null
  private var lastScreenName: String? = null
  private var frameCount: Long = 0

  @OptIn(WorkflowUiExperimentalApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val debuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val profileable = applicationInfo.isProfileableByShell
    val traceMain = intent.getBooleanExtra(EXTRA_TRACE_ALL_MAIN_THREADS, false)

    if (traceMain && Build.VERSION.SDK_INT != 28 && (debuggable || profileable)) {
      // Add main message tracing to see everything happening in Perfetto.
      // See https://py.hashnode.dev/tracing-main-thread-messages for more background.
      Looper.getMainLooper().setMessageLogging { log ->
        if (!Trace.isEnabled()) {
          return@setMessageLogging
        }
        if (log.startsWith('>')) {
          val label = buildSectionLabel(log)
          Trace.beginSection(label.take(127))
        } else if (log.startsWith('<')) {
          Trace.endSection()
        }
      }
    }

    // Default is just to have the basic 'delay' complexity.
    val simulatedPerfConfig = SimulatedPerfConfig(
      isComplex = true,
      complexityDelay = 200L,
      useInitializingState = intent.getBooleanExtra(EXTRA_PERF_CONFIG_INITIALIZING, false),
      traceFrameLatency = intent.getBooleanExtra(EXTRA_PERF_CONFIG_FRAME_LATENCY, false),
      traceEventLatency = intent.getBooleanExtra(EXTRA_PERF_CONFIG_EVENT_LATENCY, false),
      traceRenderingPasses = intent.getBooleanExtra(EXTRA_PERF_CONFIG_RENDERING, false)
    )

    require(!(simulatedPerfConfig.traceFrameLatency && simulatedPerfConfig.traceRenderingPasses)) {
      "Only trace render latency OR rendering passes."
    }

    // If no interceptor is installed and we are not tracking latency, we will install the tracing
    // interceptor as that is launched from a separate process.
    if (installedInterceptor == null && simulatedPerfConfig.traceRenderingPasses) {
      installedInterceptor = PerformanceTracingInterceptor()
    } else if (installedInterceptor == null && simulatedPerfConfig.traceEventLatency) {
      installedInterceptor = EventHandlingTracingInterceptor()
    }

    val component =
      PerformancePoetryComponent(installedInterceptor, simulatedPerfConfig)
    val model: PoetryModel by viewModels { component.poetryModelFactory(this) }

    val instrumentedRenderings = if (simulatedPerfConfig.traceFrameLatency) {
      model.renderings.onEach { screen ->
        traceRenderingLatency(screen)
      }
    } else {
      model.renderings
    }

    setContentView(
      WorkflowLayout(this).apply {
        take(
          lifecycle,
          instrumentedRenderings.map { it.withEnvironment(viewEnvironment) }
        )
      }
    )

    // We can report this here as the first rendering from the Workflow is rendered synchronously.
    this.reportFullyDrawn()
  }

  /**
   * When running this with a benchmark, we can use this to reset anything needed in between
   * benchmark scenarios, even for a HOT start.
   */
  override fun onStart() {
    (installedInterceptor as? Resettable)?.reset()
    super.onStart()
  }

  @OptIn(WorkflowUiExperimentalApi::class)
  private fun traceRenderingLatency(screen: Screen) {
    // Start the trace sections for new rendering produced -> shown.
    val navigationHolder = navigationInFlight
    if (navigationHolder == null) {
      val tag = "$FRAME_LATENCY_SECTION-${frameCount.toString().padStart(2, '0')} "
      Trace.beginAsyncSection(
        tag,
        TRACE_COOKIE
      )
      navigationInFlight = NavigationHolder(
        originScreenName = lastScreenName,
        destinationScreenName = screen.toString(),
      )
      postFrameRenderedCallback {
        navigationInFlight = null
        val endTag = "$FRAME_LATENCY_SECTION-${frameCount.toString().padStart(2, '0')} "
        // End the trace sections for new rendering produced -> shown.
        Trace.endAsyncSection(
          endTag,
          TRACE_COOKIE
        )
        frameCount++
      }
    } else {
      // Keep the previous update and update the destination
      navigationHolder.destinationScreenName = screen.toString()
    }
    lastScreenName = screen.toString()
  }

  private fun postFrameRenderedCallback(block: () -> Unit) {
    // The frame callback runs somewhat in the middle of rendering, so by posting at the front
    // of the queue from there we get the timestamp for right when the next frame is done
    // rendering.
    Choreographer.getInstance().postFrameCallback {
      mainHandler.postAtFrontOfQueue {
        block()
      }
    }
  }

  /**
   * See methodology from Py - https://py.hashnode.dev/tracing-main-thread-messages
   */
  private fun buildSectionLabel(log: String): String {
    val logNoPrefix = log.removePrefix(">>>>> Dispatching to ")
    val indexOfWhat = logNoPrefix.lastIndexOf(": ")
    val indexOfCallback = logNoPrefix.indexOf("} ")

    val targetHandler = logNoPrefix.substring(0, indexOfCallback + 1)
    val callback = logNoPrefix.substring(indexOfCallback + 2, indexOfWhat)
      .removePrefix("DispatchedContinuation[Dispatchers.Main, Continuation at ")
      .removePrefix("DispatchedContinuation[Dispatchers.Main.immediate, Continuation at ")
    val what = logNoPrefix.substring(indexOfWhat + 2)

    return if (callback != "null") {
      "$callback $targetHandler $what"
    } else {
      "$targetHandler $what"
    }
  }

  companion object {
    // Async traces require a unique int not used by any other async trace.
    private const val TRACE_COOKIE = 133742
    const val FRAME_LATENCY_SECTION = "Frame-Latency"
    const val EXTRA_TRACE_ALL_MAIN_THREADS = "complex.poetry.performance.config.trace.main"
    const val EXTRA_PERF_CONFIG_INITIALIZING = "complex.poetry.performance.config.use.initializing"
    const val EXTRA_PERF_CONFIG_EVENT_LATENCY =
      "complex.poetry.performance.config.track.event.latency"
    const val EXTRA_PERF_CONFIG_FRAME_LATENCY =
      "complex.poetry.performance.config.track.frame.latency"
    const val EXTRA_PERF_CONFIG_RENDERING = "complex.poetry.performance.config.track.rendering"
    var installedInterceptor: WorkflowInterceptor? = null

    init {
      Timber.plant(Timber.DebugTree())
    }
  }
}

class PoetryModel(
  savedState: SavedStateHandle,
  workflow: MaybeLoadingGatekeeperWorkflow<List<Poem>>,
  interceptor: WorkflowInterceptor?,
) : ViewModel() {
  @OptIn(WorkflowUiExperimentalApi::class) val renderings: StateFlow<Screen> by lazy {
    renderWorkflowIn(
      workflow = workflow,
      scope = viewModelScope,
      savedStateHandle = savedState,
      interceptors = interceptor?.let { listOf(it) } ?: emptyList()
    )
  }

  class Factory(
    owner: SavedStateRegistryOwner,
    private val workflow: MaybeLoadingGatekeeperWorkflow<List<Poem>>,
    private val workflowInterceptor: WorkflowInterceptor?,
  ) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel> create(
      key: String,
      modelClass: Class<T>,
      handle: SavedStateHandle
    ): T {
      if (modelClass == PoetryModel::class.java) {
        @Suppress("UNCHECKED_CAST")
        return PoetryModel(handle, workflow, workflowInterceptor) as T
      }

      throw IllegalArgumentException("Unknown ViewModel type $modelClass")
    }
  }
}
