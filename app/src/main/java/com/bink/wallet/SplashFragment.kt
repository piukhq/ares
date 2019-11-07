package com.bink.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        val rootBeer = RootBeer(context)
        findNavController().navigateIfAdded(
            this, when (rootBeer.isRooted) {
                true -> R.id.splash_to_rooted_device
                else -> R.id.splash_to_onboarding
            }
        )
    }
}
