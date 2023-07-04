package workflow.tutorial

import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

@OptIn(WorkflowUiExperimentalApi::class)
data class WelcomeScreen(
  val username: String,
  val onUsernameChanged: (String) -> Unit,
  val onLoginTapped: () -> Unit
): Screen
