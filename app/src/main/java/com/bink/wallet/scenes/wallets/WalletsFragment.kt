package com.bink.wallet.scenes.wallets

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.MainViewModel
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.WalletsFragmentBinding
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletFragment
import com.bink.wallet.scenes.payment_card_wallet.PaymentCardWalletFragment
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
        val loyaltyWalletsFragment = LoyaltyWalletFragment()
        val paymentCardWalletFragment = PaymentCardWalletFragment()

        viewModel.fetchStoredMembershipPlans()
        viewModel.fetchMembershipCards()
        viewModel.fetchPaymentCards()


        arguments?.let {
            with(WalletsFragmentArgs.fromBundle(it)) {
                handleLoading(shouldRefresh)
            }
        }

        if (SharedPreferenceManager.isLoyaltySelected) {
            bottomNavigation.selectedItemId = R.id.loyalty_menu_item
            navigateToLoyaltyWallet()
        } else {
            bottomNavigation.selectedItemId = R.id.payment_menu_item
            navigateToPaymentCardWalletWallet()
        }


        viewModel.membershipPlanData.observeNonNull(this) { plans ->
            viewModel.membershipCardData.observeNonNull(this) { cards ->
                if (!SharedPreferenceManager.isLoyaltySelected) {
                    paymentCardWalletFragment.setData(cards, plans)
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

    private fun navigateToLoyaltyWallet() {
        findNavController().navigateIfAdded(this, WalletsFragmentDirections.homeToLoyaltyWallet())
    }

    private fun navigateToPaymentCardWalletWallet() {
        findNavController().navigateIfAdded(this, WalletsFragmentDirections.homeToPaymentWallet())
    }

    private fun handleLoading(shouldRefresh: Boolean) {
        if (shouldRefresh) {
            mainViewModel.startLoading()
        } else {
            mainViewModel.stopLoading()
        }
    }
}
