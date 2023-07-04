package workflow.tutorial

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.container.BackStackScreen
import com.squareup.workflow1.ui.container.toBackStackScreen
import workflow.tutorial.RootWorkflow.State
import workflow.tutorial.RootWorkflow.State.Todo
import workflow.tutorial.RootWorkflow.State.Welcome
import workflow.tutorial.TodoListWorkflow.ListProps
import workflow.tutorial.TodoWorkflow.TodoProps

@OptIn(WorkflowUiExperimentalApi::class)
object RootWorkflow : StatefulWorkflow<Unit, State, Nothing, BackStackScreen<Screen>>() {

  sealed class State {
    object Welcome : State()
    data class Todo(val username: String) : State()
  }
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?
  ): State = Welcome

  override fun render(
    renderProps: Unit,
    renderState: State,
    context: RenderContext
  ): BackStackScreen<Screen> {

    val backStackScreens = mutableListOf<Screen>()

    val welcomeScreen = context.renderChild(child = WelcomeWorkflow, key = "") { output ->
      login(output.username)
    }

    backStackScreens += welcomeScreen


    when(renderState) {
      is Welcome -> {}
      is Todo -> {
        val todoScreens = context.renderChild(child = TodoWorkflow, TodoProps(username = renderState.username)) {
          logout()
        }
        backStackScreens.addAll(todoScreens)
      }
    }

    return backStackScreens.toBackStackScreen()
  }

  override fun snapshotState(state: State): Snapshot? = null

  private fun login(username: String) = action {
    state = Todo(username)
  }

  private fun logout() = action {
    state = Welcome
  }
}
