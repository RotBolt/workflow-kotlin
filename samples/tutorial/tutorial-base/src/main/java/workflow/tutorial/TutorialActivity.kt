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
import com.squareup.workflow1.ui.container.BackStackContainer
import com.squareup.workflow1.ui.container.withRegistry
import com.squareup.workflow1.ui.renderWorkflowIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class TutorialActivity : AppCompatActivity() {


  private val viewRegistry = ViewRegistry(
    // No need to add BackStackContainer. Its now by default built in ViewRegistry
    // com.squareup.workflow1.ui.backstack.BackStackContainer,
    WelcomeScreenViewRunner,
    TodoListScreenViewRunner
  )

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
  val renderings : StateFlow<Any> by lazy {
    renderWorkflowIn(
      workflow = RootWorkflow,
      scope = viewModelScope,
      savedStateHandle = savedState
    )
  }
}
