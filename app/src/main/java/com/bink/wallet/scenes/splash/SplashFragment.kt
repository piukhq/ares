package com.bink.wallet.scenes.splash

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.auth0.android.jwt.JWT
import com.bink.sdk.BinkCore
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.FragmentSplashBinding
import com.bink.wallet.model.Consent
import com.bink.wallet.model.PostServiceRequest
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.scottyab.rootbeer.RootBeer
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : BaseFragment<SplashViewModel, FragmentSplashBinding>() {
    override val layoutRes: Int
        get() = R.layout.fragment_splash
    override val viewModel: SplashViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder().build()
    }

    companion object {
        init {
            System.loadLibrary("spreedly-lib")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setAppPrefs()
    }

    external fun spreedlyKey(): String

    private fun getDirections(): Int {
        val rootBeer = RootBeer(context)
        return when (rootBeer.isRooted) {
            true -> R.id.splash_to_rooted_device
            else -> getUnRootedDirections()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.postServiceResponse.observeNonNull(this) {
            findNavController().navigateIfAdded(this, getDirections())
        }
        viewModel.postServiceErrorResponse.observeNonNull(this) {
            findNavController().navigateIfAdded(this, getDirections())
        }
    }

    private fun getUnRootedDirections(): Int {

        return when (requireContext().let { LocalStoreUtils.isLoggedIn(LocalStoreUtils.KEY_TOKEN) }) {
            true -> R.id.global_to_home
            else ->
                /**
                 *      Since in the future we might want to redirect the user to
                 * different screens we can do that based on a destination
                 * string in the intent
                 *      If the user isn't logged in then it is sent to onboarding.
                 * Since an 'else' branch can't be merged together with another
                 * option in a when clause, we will have for two clauses with the
                 * same destination for now.
                 **/
                when (requireActivity().intent.getSessionHandlerNavigationDestination()) {
                    SESSION_HANDLER_DESTINATION_ONBOARDING -> R.id.splash_to_onboarding
                    else -> R.id.splash_to_onboarding
                }
        }
    }

    private fun setAppPrefs() {
        if (!requireContext().let { LocalStoreUtils.isLoggedIn(LocalStoreUtils.KEY_TOKEN) }) {
            val binkCore = BinkCore(requireContext())
            val key = binkCore.sessionConfig.apiKey
            val email = binkCore.sessionConfig.userEmail
            if (!key.isNullOrEmpty()) {
                val jwt = JWT(key)
                if(email.isNullOrEmpty()) {
                   // email = jwt
                }
                SharedPreferenceManager.isUserLoggedIn = true
                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_TOKEN,
                    getString(R.string.token_api_v1, key)
                )

                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_EMAIL,
                    email ?: EMPTY_STRING
                )

                viewModel.postService(
                    PostServiceRequest(
                        consent = Consent(
                            email,
                            System.currentTimeMillis() / 1000
                        )
                    )
                )
            } else {
                findNavController().navigateIfAdded(this, getDirections())
            }
        }
    }
}
