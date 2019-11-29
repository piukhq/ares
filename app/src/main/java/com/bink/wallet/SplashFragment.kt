package com.bink.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bink.sdk.BinkCore
import com.bink.sdk.util.BinkSecurityUtil
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.navigateIfAdded
import com.scottyab.rootbeer.RootBeer


class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        findNavController().navigateIfAdded(this, getDirections())
    }

    private fun getDirections(): Int {
        val rootBeer = RootBeer(context)
        return when (rootBeer.isRooted) {
            true -> R.id.splash_to_rooted_device
            else -> getUnRootedDirections()
        }
    }

    private fun getUnRootedDirections(): Int {
        val binkCore = BinkCore(requireContext())
        val pack = requireActivity().packageName
        val key = binkCore.sessionConfig.apiKey
        val enc = binkCore.sessionConfig.encryptedKey

        val key2 = BinkSecurityUtil.decrypt(enc)

        val key3 = LocalStoreUtils.getAppSharedPref(
                    LocalStoreUtils.KEY_TOKEN
                ) ?: EMPTY_STRING

        return when (context?.let { LocalStoreUtils.isLoggedIn(LocalStoreUtils.KEY_TOKEN) }) {
            true -> R.id.global_to_home
            else -> R.id.splash_to_onboarding
        }
    }
}
