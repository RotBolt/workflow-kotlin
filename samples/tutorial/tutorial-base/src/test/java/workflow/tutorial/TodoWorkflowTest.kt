package workflow.tutorial

import com.squareup.workflow1.WorkflowOutput
import com.squareup.workflow1.testing.expectWorkflow
import com.squareup.workflow1.testing.launchForTestingFromStartWith
import com.squareup.workflow1.testing.testRender
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import org.junit.Test
import workflow.tutorial.RootWorkflow.State.Todo
import workflow.tutorial.TodoEditWorkflow.Output.Save
import workflow.tutorial.TodoListWorkflow.Output.SelectTodo
import workflow.tutorial.TodoWorkflow.State
import workflow.tutorial.TodoWorkflow.State.Step
import workflow.tutorial.TodoWorkflow.TodoProps
import workflow.tutorial.TodoWorkflow.initialState
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(WorkflowUiExperimentalApi::class)
class TodoWorkflowTest {

  @Test
  fun `selecting todo`() {
    val todos = listOf(TodoModel("Title", note = "Note"))

    TodoWorkflow
      .testRender(
        props = TodoProps(username = "Ada"),
        initialState = State(todos = todos, step = Step.List)
      )
      .expectWorkflow(
        workflowType = TodoListWorkflow::class,
        rendering = TodoListScreen(
          username = "",
          todoTitles = listOf("Title"),
          onTodoSelected = {},
          onBack = {}
        ),
        output = WorkflowOutput(SelectTodo(index = 0))
      )
      .render { rendering ->
        assertEquals(1, rendering.size)
      }
      .verifyActionResult { newState, _ ->
        assertEquals(State(todos = listOf(TodoModel(title = "Title", note = "Note")), step = Step.Edit(0)), newState)
      }
  }

  @Test
  fun `saving todo`() {
    val todos = listOf(TodoModel("Title", note = "Note"))

    TodoWorkflow
      .testRender(
        props = TodoProps(username = "Ada"),
        initialState = State(todos = todos, step = Step.Edit(0))
      )
      .expectWorkflow(
        workflowType = TodoListWorkflow::class,
        rendering = TodoListScreen(
          username = "",
          todoTitles = listOf("Title"),
          onTodoSelected = {},
          onBack = {}
        ),
      )
      .expectWorkflow(
        workflowType = TodoEditWorkflow::class,
        rendering = TodoEditScreen(
          title = "Title",
          note = "Note",
          onTitleChanged = {},
          onNoteChanged = {},
          discardChanges = {},
          saveChanges = {}
        ),
        output = WorkflowOutput(
          Save(TodoModel(title = "Updated Title", note = "Updated Note"))
        )
      )
      .render { rendering ->
        assertEquals(2, rendering.size)
      }
      .verifyActionResult { newState, _ ->
        assertEquals(
          State(
            todos = listOf(TodoModel(title = "Updated Title", note = "Updated Note")),
            step = Step.List
          ),
          newState
        )
      }
  }


 /////// Integration testing ///////

  @Test
 fun `app flow`() {
   RootWorkflow.launchForTestingFromStartWith {
     awaitNextRendering().let { rendering ->
       assertEquals(1, rendering.frames.size)
       val welcomeScreen = rendering.frames[0] as WelcomeScreen

       welcomeScreen.onUsernameChanged("Ada")
     }

     awaitNextRendering().let { rendering ->
       assertEquals(1, rendering.frames.size)

       val welcomeScreen = rendering.frames[0] as WelcomeScreen

       welcomeScreen.onLoginTapped()
     }

     awaitNextRendering().let {rendering ->
       assertEquals(2, rendering.frames.size)
       assertTrue(rendering.frames[0] is WelcomeScreen)
       val todoScreen = rendering.frames[1] as TodoListScreen

       assertEquals(1, todoScreen.todoTitles.size)

       todoScreen.onTodoSelected(0)
     }

     awaitNextRendering().let { rendering ->
       assertEquals(3, rendering.frames.size)
       assertTrue(rendering.frames[0] is WelcomeScreen)
       assertTrue(rendering.frames[1] is TodoListScreen)

       val editScreen = rendering.frames[2] as TodoEditScreen
       editScreen.onTitleChanged("New title")
     }

     awaitNextRendering().let { rendering ->
       assertEquals(3, rendering.frames.size)
       assertTrue(rendering.frames[0] is WelcomeScreen)
       assertTrue(rendering.frames[1] is TodoListScreen)

       val editScreen = rendering.frames[2] as TodoEditScreen
       editScreen.saveChanges()
     }

     awaitNextRendering().let { rendering ->
       assertEquals(2, rendering.frames.size)
       assertTrue(rendering.frames[0] is WelcomeScreen)

       val todoScreen = rendering.frames[1] as TodoListScreen
       assertEquals(1, todoScreen.todoTitles.size)
       assertEquals("New title", todoScreen.todoTitles[0])
     }
   }
 }

}
