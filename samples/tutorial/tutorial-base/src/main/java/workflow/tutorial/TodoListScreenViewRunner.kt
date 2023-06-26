package workflow.tutorial

import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.workflow1.ui.ScreenViewFactory
import com.squareup.workflow1.ui.ScreenViewRunner
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.backPressedHandler
import workflow.tutorial.views.TodoListAdapter
import workflow.tutorial.views.databinding.TodoListViewBinding

@OptIn(WorkflowUiExperimentalApi::class)
class TodoListScreenViewRunner(
  private val binding: TodoListViewBinding
) : ScreenViewRunner<TodoListScreen> {

  val adapter = TodoListAdapter()
  init {
    binding.todoList.layoutManager = LinearLayoutManager(binding.root.context)
    binding.todoList.adapter = adapter
  }
  override fun showRendering(
    rendering: TodoListScreen,
    viewEnvironment: ViewEnvironment
  ) {
    binding.root.backPressedHandler = rendering.onBack

    with(binding.todoListWelcome) {
      text = resources.getString(R.string.todo_list_welcome, rendering.username)
    }

    adapter.todoList = rendering.todoTitles
    adapter.onTodoSelected = rendering.onTodoSelected
    adapter.notifyDataSetChanged()
  }

  companion object : ScreenViewFactory<TodoListScreen> by ScreenViewFactory.fromViewBinding(
    TodoListViewBinding::inflate, ::TodoListScreenViewRunner
  )
}
