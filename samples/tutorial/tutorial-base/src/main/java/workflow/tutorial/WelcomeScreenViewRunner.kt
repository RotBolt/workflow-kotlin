package workflow.tutorial

import com.squareup.workflow1.ui.ScreenViewFactory
import com.squareup.workflow1.ui.ScreenViewRunner
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.setTextChangedListener
import com.squareup.workflow1.ui.updateText
import workflow.tutorial.views.databinding.WelcomeViewBinding

@OptIn(WorkflowUiExperimentalApi::class)
class WelcomeScreenViewRunner(
  private val binding: WelcomeViewBinding
) : ScreenViewRunner<WelcomeScreen> {

  override fun showRendering(
    rendering: WelcomeScreen,
    viewEnvironment: ViewEnvironment
  ) {
    binding.username.updateText(rendering.username)
    binding.username.setTextChangedListener {
      rendering.onUsernameChanged(it.toString())
    }
    binding.login.setOnClickListener { rendering.onLoginTapped() }
  }

  companion object : ScreenViewFactory<WelcomeScreen> by ScreenViewFactory.Companion.fromViewBinding(
    bindingInflater = WelcomeViewBinding::inflate,
    constructor = { welcomeViewBinding -> WelcomeScreenViewRunner(welcomeViewBinding) }
  )
}
