package com.bink.wallet.scenes.loyalty_details

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.LoyaltyCardRewardsHistoryBinding
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.utils.enums.VoucherStates
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoyaltyCardRewardsHistoryFragment :
    BaseFragment<LoyaltyCardRewardsHistoryViewModel, LoyaltyCardRewardsHistoryBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding?.toolbar)
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

        binding?.membershipPlan = viewModel.membershipPlan.value
        binding?.executePendingBindings()
        setupVouchers()
    }

    private fun setupVouchers() {
        with(binding?.recycler) {
            this?.visibility = View.VISIBLE
            this?.layoutManager = LinearLayoutManager(requireContext())
            this?.isNestedScrollingEnabled = true
            viewModel.membershipCard.value?.vouchers?.filterNot {
                listOf(
                    VoucherStates.IN_PROGRESS.state,
                    VoucherStates.ISSUED.state
                ).contains(it.state)
            }?.sortedByDescending {
                if ((it.date_redeemed ?: 0L) != 0L) {
                    it.date_redeemed
                } else {
                    it.expiry_date
                }
            }?.let { vouchers ->
                this?.adapter = VouchersAdapter(
                    vouchers
                ).apply {
                    setOnVoucherClickListener { voucher ->
                        viewVoucherDetails(voucher)
                    }
                }
            }
        }
    }

    private fun viewVoucherDetails(voucher: Voucher) {
        val directions = viewModel.membershipPlan.value?.let { membershipPlan ->
            LoyaltyCardRewardsHistoryFragmentDirections.historyToVoucher(
                membershipPlan, voucher
            )
        }
        if (directions != null) {
            findNavController().navigateIfAdded(this, directions)
        }
    }
}