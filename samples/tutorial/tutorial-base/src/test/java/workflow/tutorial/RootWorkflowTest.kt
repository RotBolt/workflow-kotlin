package workflow.tutorial

import com.squareup.workflow1.WorkflowOutput
import com.squareup.workflow1.testing.expectWorkflow
import com.squareup.workflow1.testing.testRender
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import org.junit.Test
import workflow.tutorial.RootWorkflow.State.Todo
import workflow.tutorial.RootWorkflow.State.Welcome
import workflow.tutorial.WelcomeWorkflow.LoggedIn
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(WorkflowUiExperimentalApi::class)
class RootWorkflowTest {
  @Test
  fun `welcome rendering`() {
    RootWorkflow
      .testRender(initialState = Welcome, props = Unit)
      .expectWorkflow(
        workflowType = WelcomeWorkflow::class,
        rendering = WelcomeScreen(
          username = "Ada",
          onUsernameChanged = {},
          onLoginTapped = {}
        )
      )
      .render {rendering ->
        val backstack = rendering.frames
        assertEquals(1, backstack.size)

        val welcomeScreen = backstack[0] as WelcomeScreen
        assertEquals("Ada", welcomeScreen.username)
      }
      .verifyActionResult { _, output ->
        assertNull(output)
      }
  }

  @Test
  fun `login event`() {
    RootWorkflow
      .testRender(initialState = Welcome, props = Unit)
      .expectWorkflow(
        workflowType = WelcomeWorkflow::class,
        rendering = WelcomeScreen(
          username = "Ada",
          onUsernameChanged = {},
          onLoginTapped = {}
        ),
        output = WorkflowOutput(LoggedIn("Ada"))
      )
      .render { rendering ->
        val backStack = rendering.frames
        val welcomeScreen = backStack[0] as WelcomeScreen
        assertEquals(1, backStack.size)
        assertEquals("Ada", welcomeScreen.username)
      }
      .verifyActionResult { newState, _ ->
        assertEquals(Todo(username = "Ada"), newState)
      }
  }
}
