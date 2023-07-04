package workflow.tutorial

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.StatelessWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import workflow.tutorial.TodoListWorkflow.ListProps
import workflow.tutorial.TodoListWorkflow.Output

@OptIn(WorkflowUiExperimentalApi::class)
object TodoListWorkflow : StatelessWorkflow<ListProps, Output, TodoListScreen>() {

  data class ListProps(
    val username: String,
    val todos: List<TodoModel>
  )

  sealed class Output {
    object Back : Output()
    data class SelectTodo(val index: Int) : Output()
  }


  private fun onBack() = action {
    setOutput(Output.Back)
  }

  private fun selectTodo(index: Int) = action {
    setOutput(Output.SelectTodo(index))
  }

  override fun render(
    renderProps: ListProps,
    context: RenderContext
  ): TodoListScreen {
    val titles = renderProps.todos.map { it.title }
    return TodoListScreen(
      username = renderProps.username,
      todoTitles = titles,
      onTodoSelected = { context.actionSink.send(selectTodo(it)) },
      onBack = { context.actionSink.send(onBack()) }
    )
  }
}
