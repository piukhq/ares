package com.bink.wallet.scenes.loyalty_details

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.LoyaltyCardRewardsHistoryBinding
import com.bink.wallet.utils.enums.VoucherStates
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
        arguments?.let {
            viewModel.membershipPlan.value =
                LoyaltyCardDetailsFragmentArgs.fromBundle(it).membershipPlan
            viewModel.membershipCard.value =
                LoyaltyCardDetailsFragmentArgs.fromBundle(it).membershipCard
        }

        binding.membershipPlan = viewModel.membershipPlan.value
        binding.executePendingBindings()
        setupVouchers()
    }

    private fun setupVouchers() {
        with (binding.recycler) {
            visibility = View.VISIBLE
            layoutManager = LinearLayoutManager(requireContext())
            viewModel.membershipCard.value?.vouchers?.filterNot {
                listOf(
                    VoucherStates.IN_PROGRESS.state,
                    VoucherStates.ISSUED.state
                ).contains(it.state)
            }?.let {
                adapter = LoyaltyCardDetailsVouchersAdapter(it)
            }
        }
    }
}