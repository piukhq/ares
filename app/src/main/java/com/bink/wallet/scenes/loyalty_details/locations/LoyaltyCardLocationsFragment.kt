package com.bink.wallet.scenes.loyalty_details.locations

import android.os.Bundle
import android.view.View
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.LoyaltyCardLocationFragmentBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoyaltyCardLocationsFragment : BaseFragment<LoyaltyCardLocationsViewModel, LoyaltyCardLocationFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.loyalty_card_location_fragment
    override val viewModel: LoyaltyCardLocationsViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}