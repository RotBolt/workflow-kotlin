package workflow.tutorial

import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

@OptIn(WorkflowUiExperimentalApi::class)
data class TodoListScreen(
  val username: String,
  val todoTitles: List<String>,
  val onTodoSelected: (Int) -> Unit,
  val onBack: () -> Unit
): Screen
