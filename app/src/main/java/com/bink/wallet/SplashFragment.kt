package com.bink.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bink.sdk.BinkCore
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.SESSION_HANDLER_DESTINATION_ONBOARDING
import com.bink.wallet.utils.getSessionHandlerNavigationDestination
import com.bink.wallet.utils.navigateIfAdded
import com.scottyab.rootbeer.RootBeer

class SplashFragment : Fragment() {

    companion object {
        init {
            System.loadLibrary("spreedly-lib")
        }
    }

    external fun spreedlyKey(): String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        LocalStoreUtils.setAppSharedPref(
            LocalStoreUtils.KEY_SPREEDLY, spreedlyKey()
        )
        findNavController().navigateIfAdded(this, getDirections())
    }

    private fun getDirections(): Int {
        val rootBeer = RootBeer(context)
//        return when (rootBeer.isRooted) {
//            true -> R.id.splash_to_rooted_device
//            else -> getUnRootedDirections()
//        }
        return getUnRootedDirections()
    }

    private fun getUnRootedDirections(): Int {
        if (!requireContext().let { LocalStoreUtils.isLoggedIn(LocalStoreUtils.KEY_TOKEN) }) {
            val binkCore = BinkCore(requireContext())
            val key = binkCore.sessionConfig.apiKey
            val email = binkCore.sessionConfig.userEmail
            if (!key.isNullOrEmpty()) {
                SharedPreferenceManager.isUserLoggedIn = true
                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_TOKEN,
                    getString(R.string.token_api_v1, key)
                )

                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_EMAIL,
                    email ?: EMPTY_STRING
                )
            }
        }

        return when (requireContext().let { LocalStoreUtils.isLoggedIn(LocalStoreUtils.KEY_TOKEN) }) {
            true -> R.id.global_to_home
            else ->
                /**
                 *      Since in the future we might want to redirect the user to
                 * different screens we can do that based on a destination
                 * string in the intent
                 *      If the user isn't logged in then it is sent to onboadring.
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
}
