@file:OptIn(WorkflowUiExperimentalApi::class)
package workflow.tutorial

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowLayout
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.asScreen
import com.squareup.workflow1.ui.container.withRegistry
import com.squareup.workflow1.ui.renderWorkflowIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class TutorialActivity : AppCompatActivity() {


  private val viewRegistry = ViewRegistry(WelcomeScreenViewRunner)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.welcome_view)

    val viewModel: TutorialViewModel by viewModels()

    setContentView(
      WorkflowLayout(this).apply {
          take(
            this@TutorialActivity.lifecycle,
            viewModel.renderings.map { asScreen(it).withRegistry(viewRegistry) }
          )
      }
    )
  }
}

class TutorialViewModel(savedState: SavedStateHandle): ViewModel() {
  val renderings : StateFlow<WelcomeScreen> by lazy {
    renderWorkflowIn(
      workflow = WelcomeWorkflow,
      scope = viewModelScope,
      savedStateHandle = savedState
    )
  }
}
