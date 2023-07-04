package workflow.tutorial

import com.squareup.workflow1.applyTo
import org.junit.Test
import workflow.tutorial.TodoEditWorkflow.EditProps
import workflow.tutorial.TodoEditWorkflow.Output.Save
import workflow.tutorial.TodoEditWorkflow.State
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TodoEditWorkflowTest {

  private val startState = State(todo = TodoModel(title = "Title", note = "Note"))

  @Test
  fun `title is updated`() {
    val props = EditProps(initialTodo = TodoModel(title = "", note = ""))

    val (newState, actionApplied) = TodoEditWorkflow.onTitleChanged("Updated Title")
      .applyTo(props, startState)

    assertNull(actionApplied.output)
    assertEquals(TodoModel(title = "Updated Title", note = "Note"), newState.todo)
  }

  @Test
  fun `note is updated`() {
    val props = EditProps(initialTodo = TodoModel(title = "", note = ""))

    val (newState, actionApplied) = TodoEditWorkflow.onNoteChanged("Updated Note")
      .applyTo(props, startState)

    assertNull(actionApplied.output)
    assertEquals(TodoModel(title = "Title", note = "Updated Note"), newState.todo)
  }

  @Test
  fun `save emits model`() {
    val props = EditProps(initialTodo = TodoModel(title = "", note = ""))

    val (_, actionApplied) = TodoEditWorkflow.onSave().applyTo(props, startState)

    assertEquals(Save(TodoModel(title = "Title", note = "Note")), actionApplied.output?.value)
  }

  @Test
  fun `changed props updated local state`() {
    val initialProps = EditProps(TodoModel(title = "Title", note = "Note"))
    var state = TodoEditWorkflow.initialState(initialProps, null)

    assertEquals("Title", state.todo.title)
    assertEquals("Note", state.todo.note)

    state = State(TodoModel(title = "Updated Title", note = "Note"))

    state = TodoEditWorkflow.onPropsChanged(initialProps, initialProps, state)
    assertEquals("Updated Title", state.todo.title)
    assertEquals("Note", state.todo.note)

    val updatedProps = EditProps(initialTodo = TodoModel(title = "New Title", note = "New Note"))
    state = TodoEditWorkflow.onPropsChanged(initialProps, updatedProps, state)
    assertEquals("New Title", state.todo.title)
    assertEquals("New Note", state.todo.note)

  }
}
