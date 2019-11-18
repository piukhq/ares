package com.bink.wallet.scenes.login

import android.os.Bundle
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.LoginFragmentBinding
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment<LoginViewModel, LoginFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.login_fragment
    override val viewModel: LoginViewModel by viewModel()

    private val loginData = MutableLiveData<LoginBody>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.retrieveStoredLoginData()
        viewModel.loginData.observeNonNull(this) {
            if (verifyAvailableNetwork(requireActivity())) {
                viewModel.authenticate()
            } else {
                showNoInternetConnectionDialog()
            }
        }

        with(viewModel) {
            email.observeNonNull(this@LoginFragment) {
                if (!Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                    binding.emailField.error = getString(R.string.incorrect_email_text)
                } else {
                    binding.emailField.error = null
                }
            }

            password.observeNonNull(this@LoginFragment) {
                if (!UtilFunctions.isValidField(PASSWORD_REGEX, it)) {
                    binding.passwordField.error =
                        getString(R.string.password_description)
                } else {
                    binding.passwordField.error = null
                }
            }
        }

        viewModel.logInResponse.observeNonNull(this) {
            findNavController().navigateIfAdded(this, R.id.global_to_home)
        }

        binding.logInButton.setOnClickListener {
            viewModel.logIn(
                SignUpRequest(
                    email = viewModel.email.value,
                    password = viewModel.password.value
                )
            )
        }

        viewModel.loginData.observeNonNull(this) {

        }
    }
}
