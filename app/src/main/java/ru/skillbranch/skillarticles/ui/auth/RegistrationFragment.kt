package ru.skillbranch.skillarticles.ui.auth

import android.text.Spannable
import androidx.core.text.set
import androidx.fragment.app.viewModels
import androidx.navigation.navGraphViewModels
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.FragmentRegistrationBinding
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.ui.BaseFragment
import ru.skillbranch.skillarticles.ui.custom.spans.UnderlineSpan
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.auth.AuthState
import ru.skillbranch.skillarticles.viewmodels.auth.AuthViewModel

/**
 * @author Valeriy Minnulin
 */
class RegistrationFragment: BaseFragment<AuthState, AuthViewModel, FragmentRegistrationBinding>(R.layout.fragment_registration), IRegistrationView {
    override val viewModel: AuthViewModel by navGraphViewModels(R.id.auth_flow)

    override val viewBinding: FragmentRegistrationBinding by viewBinding(FragmentRegistrationBinding::bind)

    private val decorColor by lazy { requireContext().attrValue(android.R.attr.colorPrimary) }
    override fun renderUi(data: AuthState) { }

    override fun setupViews() {
        with(viewBinding) {
            tvPrivacy.setOnClickListener { onClickPrivacy() }
            (tvPrivacy.text as Spannable).let { it[0..it.length] = UnderlineSpan(decorColor) }
        }
    }

    override fun onClickPrivacy() {
        viewModel.navigateToPrivacy()
    }

    override fun onClickRegistration() {
        with(viewBinding) {
            viewModel.handleRegistration(
                etName.text.toString(),
                etLogin.text.toString(),
                etPassword.text.toString()
            )
        }
    }
}