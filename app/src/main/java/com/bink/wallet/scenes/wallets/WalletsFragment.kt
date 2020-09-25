package com.bink.wallet.scenes.wallets

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.MainViewModel
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.WalletsFragmentBinding
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletFragment
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

        //TODO: Replace fragmentManager with navigation to keep consistency of the application. (AB20-186)
        if (SharedPreferenceManager.isLoyaltySelected) {
            binding.bottomNavigation.selectedItemId = R.id.loyalty_menu_item
            fragmentManager?.beginTransaction()?.add(R.id.wallet_content, loyaltyWalletsFragment)
                ?.commit()
        } else {
            binding.bottomNavigation.selectedItemId = R.id.payment_menu_item
            fragmentManager?.beginTransaction()?.add(R.id.wallet_content, paymentCardWalletFragment)
                ?.commit()
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.loyalty_menu_item -> {
                    SharedPreferenceManager.isLoyaltySelected = true
                    replaceFragment(paymentCardWalletFragment, loyaltyWalletsFragment)
                }
                R.id.add_menu_item -> {
                    viewModel.membershipPlanData.value?.let {
                        val directions =
                            it.toTypedArray().let { plans ->
                                WalletsFragmentDirections.homeToAdd(
                                    plans
                                )
                            }
                        directions.let { findNavController().navigateIfAdded(this, it) }
                    }
                }
                R.id.payment_menu_item -> {
                    SharedPreferenceManager.isLoyaltySelected = false
                    replaceFragment(loyaltyWalletsFragment, paymentCardWalletFragment)
                    if (viewModel.membershipCardData.value != null &&
                        viewModel.membershipPlanData.value != null
                    ) {
                        paymentCardWalletFragment.setData(
                            viewModel.membershipCardData.value!!,
                            viewModel.membershipPlanData.value!!
                        )
                    }
                }

            }
            true
        }

        viewModel.paymentCards.observeNonNull(this) {
            SharedPreferenceManager.isPaymentEmpty = it.isNullOrEmpty()
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

    private fun replaceFragment(removedFragment: Fragment, addedFragment: Fragment) {
        fragmentManager?.beginTransaction()?.remove(removedFragment)
            ?.commit()
        fragmentManager?.beginTransaction()?.replace(
            R.id.wallet_content,
            addedFragment
        )?.commit()
    }

    private fun handleLoading(shouldRefresh: Boolean) {
        if (shouldRefresh) {
            mainViewModel.startLoading()
        } else {
            mainViewModel.stopLoading()
        }
    }
}
