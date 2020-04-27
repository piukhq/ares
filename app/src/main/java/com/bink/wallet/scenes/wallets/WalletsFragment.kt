package com.bink.wallet.scenes.wallets

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.MainActivity
import com.bink.wallet.MainViewModel
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.WalletsFragmentBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class WalletsFragment : BaseFragment<WalletsViewModel, WalletsFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val viewModel: WalletsViewModel by viewModel()
    val mainViewModel: MainViewModel by sharedViewModel()

    override val layoutRes: Int
        get() = R.layout.wallets_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).showHomeViews()


        viewModel.fetchStoredMembershipPlans()
        viewModel.fetchMembershipCards()
        viewModel.fetchPaymentCards()

        arguments?.let {
            with(WalletsFragmentArgs.fromBundle(it)) {
                handleLoading(shouldRefresh)
            }
        }

        if (SharedPreferenceManager.isLoyaltySelected) {
            toLoyaltyWalletScreen()
        } else {
            toPaymentCardsScreen()
        }

        viewModel.paymentCards.observeNonNull(this) {
            SharedPreferenceManager.isPaymentEmpty = it.isNullOrEmpty()
        }

        viewModel.membershipPlanData.observeNonNull(this) { plans ->
            viewModel.membershipCardData.observeNonNull(this) { cards ->
                if (!SharedPreferenceManager.isLoyaltySelected) {
//                    paymentCardWalletFragment.setData(cards, plans)
                }
            }
        }

        initSharedMembershipPlanObserver()
    }

    override fun onDestroyView() {
        arguments?.clear()
        super.onDestroyView()
    }

    private fun initSharedMembershipPlanObserver() {
        mainViewModel.membershipPlanDatabaseLiveData.observeNonNull(this) {
            viewModel.fetchStoredMembershipPlans()
        }
    }

    private fun handleLoading(shouldRefresh: Boolean) {
        if (shouldRefresh) {
            mainViewModel.startLoading()
        } else {
            mainViewModel.stopLoading()
        }
    }

    private fun toLoyaltyWalletScreen() {
        findNavController().navigateIfAdded(
            this@WalletsFragment,
            WalletsFragmentDirections.homeToLoyaltyWallet()
        )
    }

    private fun toPaymentCardsScreen() {
        var membershipPlansArray = emptyArray<MembershipPlan>()
        var membershipCardsArray = emptyArray<MembershipCard>()

        viewModel.membershipPlanData.value?.let { safeMembershipPlans ->
            membershipPlansArray = safeMembershipPlans.toTypedArray()
        }

        viewModel.membershipCardData.value?.let { safePaymentCards ->
            membershipCardsArray = safePaymentCards.toTypedArray()
        }

        findNavController().navigateIfAdded(
            this@WalletsFragment,
            WalletsFragmentDirections.homeToPaymentCardWallet(
                membershipPlansArray,
                membershipCardsArray
            )
        )
    }
}
