package ru.skillbranch.skillarticles.viewmodels.auth

import androidx.lifecycle.SavedStateHandle
import ru.skillbranch.skillarticles.ui.auth.AuthFragmentDirections
import ru.skillbranch.skillarticles.ui.auth.RegistrationFragmentDirections
import ru.skillbranch.skillarticles.viewmodels.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.NavCommand
import ru.skillbranch.skillarticles.viewmodels.VMState

/**
 * @author Valeriy Minnulin
 */
class RegisterViewModel(savedStateHandle: SavedStateHandle): BaseViewModel<RegistrationState>(RegistrationState(), savedStateHandle) {
    fun navigateToPrivacy() {
        val action = RegistrationFragmentDirections.actionRegistrationFragmentToPrivacyPolicyFragment()
        navigate(NavCommand.Action(action))
    }
}
data class RegistrationState(
    val inputErrors: Map<String, String> = emptyMap()
): VMState