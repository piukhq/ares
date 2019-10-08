package com.bink.wallet.scenes.wallets

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.WalletsFragmentBinding
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletFragment
import com.bink.wallet.scenes.payment_card_wallet.PaymentCardWalletFragment
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel


class WalletsFragment : BaseFragment<WalletsViewModel, WalletsFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    private var TAG = WalletsFragment::class.simpleName

    override val viewModel: WalletsViewModel by viewModel()

    override val layoutRes: Int
        get() = R.layout.wallets_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val loyaltyWalletsFragment = LoyaltyWalletFragment()
        val paymentCardWalletFragment = PaymentCardWalletFragment()

        runBlocking {
            viewModel.fetchMembershipPlans()
        }

        activity?.let {
            it.setActionBar(binding.toolbar)
            it.actionBar?.setDisplayShowTitleEnabled(false)
        }

        //TODO: Replace fragmentManager with navigation to keep consistency of the application. (AB20-186)

        fragmentManager?.beginTransaction()?.add(R.id.wallet_content, loyaltyWalletsFragment)
            ?.commit()

        viewModel.membershipPlanData.observeNonNull(this) {
            binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.loyalty_menu_item -> {
                        fragmentManager?.beginTransaction()?.remove(paymentCardWalletFragment)
                            ?.commit()
                        fragmentManager?.beginTransaction()?.replace(
                            R.id.wallet_content,
                            loyaltyWalletsFragment
                        )?.commit()
                    }
                    R.id.add_menu_item -> {
                        val directions = it?.toTypedArray()?.let { plans ->
                            WalletsFragmentDirections.homeToAdd(
                                plans
                            )
                        }
                        directions?.let { findNavController().navigateIfAdded(this, it) }
                    }
                    R.id.payment_menu_item -> {
                        fragmentManager?.beginTransaction()?.remove(loyaltyWalletsFragment)
                            ?.commit()
                        fragmentManager?.beginTransaction()?.replace(
                            R.id.wallet_content,
                            paymentCardWalletFragment
                        )?.commit()
                    }

                }
                true
            }
        }
        binding.settingsButton.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.settings_screen)
        }
    }

}
