package com.bink.wallet.scenes.payment_card_wallet

import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.PaymentCardWalletFragmentBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.wallets.WalletsFragmentDirections
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentCardWalletFragment :
    BaseFragment<PaymentCardWalletViewModel, PaymentCardWalletFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.payment_card_wallet_fragment

    override val viewModel: PaymentCardWalletViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.fetchLocalMembershipPlans()
        viewModel.fetchLocalMembershipCards()
        viewModel.fetchLocalPaymentCards()

        runBlocking {
            viewModel.getPaymentCards()
            binding.progressSpinner.visibility = View.VISIBLE
        }

        viewModel.localMembershipPlanData.observeNonNull(this) { plans ->
            viewModel.localMembershipCardData.observeNonNull(this) { cards ->
                viewModel.paymentCards.observeNonNull(this) { paymentCards ->
                    binding.progressSpinner.visibility = View.GONE
                    binding.paymentCardRecycler.apply {
                        layoutManager = GridLayoutManager(context, 1)
                        adapter =
                            PaymentCardWalletAdapter(
                                paymentCards,
                                onClickListener = {
                                    val action =
                                        WalletsFragmentDirections.paymentWalletToDetails(
                                            it,
                                            plans.toTypedArray(),
                                            cards.toTypedArray()
                                        )
                                    findNavController().navigateIfAdded(
                                        this@PaymentCardWalletFragment,
                                        action
                                    )
                                })
                    }
                }
            }
        }

    }

    fun setData(
        membershipCards: List<MembershipCard>,
        membershipPlans: List<MembershipPlan>
    ) {
        viewModel.run {
            localMembershipCardData.value = membershipCards
            localMembershipPlanData.value = membershipPlans
        }
    }
}
