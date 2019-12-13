package com.bink.wallet.scenes.loyalty_details

import android.os.Bundle
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.LoyaltyCardRewardsHistoryBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoyaltyCardRewardsHistoryFragment :
    BaseFragment<LoyaltyCardRewardsHistoryViewModel, LoyaltyCardRewardsHistoryBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }
    override val viewModel: LoyaltyCardRewardsHistoryViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.loyalty_card_rewards_history

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }
}