package workflow.tutorial

import com.squareup.workflow1.applyTo
import com.squareup.workflow1.testing.testRender
import org.junit.Test
import workflow.tutorial.WelcomeWorkflow.LoggedIn
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WelcomeWorkflowTest {

  @Test
  fun `username updates`() {
    val startState = WelcomeWorkflow.State("")
    val action = WelcomeWorkflow.onUsernameChanged("myName")

    val (state, output) = action.applyTo(state = startState, props = Unit)
    assertNull(output.output)

    assertEquals("myNamea", state.username)
  }

  @Test
  fun `login works`() {
    val startState = WelcomeWorkflow.State("myName")
    val action = WelcomeWorkflow.onLogin()

    val (_, actionApplied) = action.applyTo(state = startState, props = Unit)

    assertEquals(LoggedIn("myName"), actionApplied.output?.value)
  }

  @Test
  fun `login does nothing when name is empty`() {
    val startState = WelcomeWorkflow.State("")
    val action = WelcomeWorkflow.onLogin()
    val (state, actionApplied) = action.applyTo(state = startState, props = Unit)

    assertNull(actionApplied.output)

    assertEquals("", state.username)
  }

  @Test
  fun `rendering initial`() {
    WelcomeWorkflow.testRender(props = Unit)
      .render { screen ->
        assertEquals("", screen.username)

        screen.onLoginTapped()
      }
      .verifyActionResult { _, output ->
        assertNull(output)
      }
  }

  @Test
  fun `rendering name change`() {
    WelcomeWorkflow.testRender(props = Unit)
      .render { screen ->
        screen.onUsernameChanged("myName")
      }
      .verifyActionResult { state, _ ->
        assertEquals("myNamea", (state as WelcomeWorkflow.State).username)
      }
  }

  @Test
  fun `rendering login`() {
    WelcomeWorkflow.testRender( initialState = WelcomeWorkflow.State("myName"),props = Unit)
      .render { screen ->
        screen.onLoginTapped()
      }
      .verifyActionResult { _, output ->
        assertEquals(LoggedIn("myName"), output?.value)
      }
  }
}
