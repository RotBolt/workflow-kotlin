package workflow.tutorial

import com.squareup.workflow1.ui.LayoutRunner
import com.squareup.workflow1.ui.LayoutRunner.Companion.bind
import com.squareup.workflow1.ui.ScreenViewFactory
import com.squareup.workflow1.ui.ScreenViewFactory.Companion
import com.squareup.workflow1.ui.ScreenViewRunner
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.backPressedHandler
import com.squareup.workflow1.ui.setTextChangedListener
import com.squareup.workflow1.ui.updateText
import workflow.tutorial.views.databinding.TodoEditViewBinding

@OptIn(WorkflowUiExperimentalApi::class)
class TodoEditScreenViewRunner(
  private val binding: TodoEditViewBinding
) : ScreenViewRunner<TodoEditScreen> {

  override fun showRendering(
    rendering: TodoEditScreen,
    viewEnvironment: ViewEnvironment
  ) {
    binding.root.backPressedHandler = rendering.discardChanges
    binding.save.setOnClickListener { rendering.saveChanges() }
    binding.todoTitle.updateText(rendering.title)
    binding.todoTitle.setTextChangedListener { rendering.onTitleChanged(it.toString()) }
    binding.todoNote.updateText(rendering.note)
    binding.todoNote.setTextChangedListener { rendering.onNoteChanged(it.toString()) }
  }

  companion object : ScreenViewFactory<TodoEditScreen> by ScreenViewFactory.Companion.fromViewBinding(
    bindingInflater = TodoEditViewBinding::inflate,
    constructor = ::TodoEditScreenViewRunner
  )
}
