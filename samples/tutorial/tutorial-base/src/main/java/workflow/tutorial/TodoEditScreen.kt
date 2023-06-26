package workflow.tutorial

import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

@OptIn(WorkflowUiExperimentalApi::class)
data class TodoEditScreen(
  val title: String,
  val note: String,
  val onTitleChanged: (String) -> Unit,
  val onNoteChanged: (String) -> Unit,

  val discardChanges: () -> Unit,
  val saveChanges: () -> Unit
): Screen
