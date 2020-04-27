package com.bink.wallet.scenes.wallets

import android.os.Bundle
import android.util.Log
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

    interface Listener {

        companion object {
            val NULL = object : Listener {
                override fun onOpenAddScreen() {
                    //
                }

                override fun onOpenPaymentCards() {
                    //
                }

                override fun onOpenLoyalty() {
                    //
                }
            }
        }

        fun onOpenLoyalty()

        fun onOpenAddScreen()

        fun onOpenPaymentCards()

    }

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
//        val loyaltyWalletsFragment = LoyaltyWalletFragment()
//        val paymentCardWalletFragment = PaymentCardWalletFragment()

        (activity as MainActivity).showBar()


        viewModel.fetchStoredMembershipPlans()
        viewModel.fetchMembershipCards()
        viewModel.fetchPaymentCards()

        requireActivity().apply {
            setActionBar(binding.toolbar)
            actionBar?.setDisplayShowTitleEnabled(false)
        }

        arguments?.let {
            with(WalletsFragmentArgs.fromBundle(it)) {
                handleLoading(shouldRefresh)
            }
        }

        (activity as MainActivity).setListener(object : Listener {
            override fun onOpenAddScreen() {
                toAddCardScreen()
            }

            override fun onOpenPaymentCards() {
                toPaymentCardsScreen()
            }

            override fun onOpenLoyalty() {
                toLoyaltyWalletScreen()
            }
        })

        //TODO: Replace fragmentManager with navigation to keep consistency of the application. (AB20-186)
        if (SharedPreferenceManager.isLoyaltySelected) {
            toLoyaltyWalletScreen()
        } else {
            toPaymentCardsScreen()
        }

//        binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.loyalty_menu_item -> {
//                    SharedPreferenceManager.isLoyaltySelected = true
////                    replaceFragment(paymentCardWalletFragment, loyaltyWalletsFragment)
//
//
////                    val action =
////                        WalletsFragmentDirections.paymentWalletToDetails(
////                            it,
////                            plans.toTypedArray(),
////                            cards.toTypedArray()
////                        )
////                    findNavController().navigateIfAdded(
////                        this@PaymentCardWalletFragment,
////                        action
////                    )
//
//
//                    toLoyaltyWalletScreen()
//                }
//                R.id.add_menu_item -> {
//                    viewModel.membershipPlanData.value?.let {
//                        val directions =
//                            it.toTypedArray().let { plans ->
//                                WalletsFragmentDirections.homeToAdd(
//                                    plans
//                                )
//                            }
//                        directions.let { findNavController().navigateIfAdded(this, it) }
//                    }
//                }
//                R.id.payment_menu_item -> {
//                    SharedPreferenceManager.isLoyaltySelected = false
////                    replaceFragment(loyaltyWalletsFragment, paymentCardWalletFragment)
//                    if (viewModel.membershipCardData.value != null &&
//                        viewModel.membershipPlanData.value != null
//                    ) {
////                        paymentCardWalletFragment.setData(
////                            viewModel.membershipCardData.value!!,
////                            viewModel.membershipPlanData.value!!
////                        )
//                    }
//                }
//
//            }
//            true
//        }

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

        binding.settingsButton.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.settings_screen)
        }
        initSharedMembershipPlanObserver()
    }

    override fun onDestroyView() {
        arguments?.clear()
        Log.e("ConnorDebug", "ondestroyview")
        super.onDestroyView()
    }

    override fun onPause() {
        Log.e("ConnorDebug", "onPause")
        super.onPause()
    }

    private fun initSharedMembershipPlanObserver() {
        mainViewModel.membershipPlanDatabaseLiveData.observeNonNull(this) {
            viewModel.fetchStoredMembershipPlans()
        }
    }

//    private fun replaceFragment(removedFragment: Fragment, addedFragment: Fragment) {
//        fragmentManager?.beginTransaction()?.remove(removedFragment)
//            ?.commit()
//        fragmentManager?.beginTransaction()?.replace(
//            R.id.wallet_content,
//            addedFragment
//        )?.commit()
//    }

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

    private fun toAddCardScreen() {
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
