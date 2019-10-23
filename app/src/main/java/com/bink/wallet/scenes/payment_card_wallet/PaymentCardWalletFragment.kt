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
import com.bink.wallet.model.response.payment_card.PaymentCard
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

        viewModel.paymentCardsLoadedCount.observeNonNull(this) { count ->
            when (count) {
                // loading the local payment cards, so we start the spinner
                -1 -> {
                    binding.progressSpinner.visibility = View.VISIBLE
                }
                // no cards loaded, so shift to the "add cards" display
                0 -> {
                    binding.progressSpinner.visibility = View.GONE
                    binding.paymentCardRecycler.visibility = View.GONE
                    binding.noPaymentCardsDisplay.visibility = View.VISIBLE
                }
                // user has cards, so show the adapter
                else -> {
                    binding.progressSpinner.visibility = View.GONE
                    binding.paymentCardRecycler.visibility = View.VISIBLE
                    binding.noPaymentCardsDisplay.visibility = View.GONE
                }
            }
        }
        viewModel.localMembershipPlanData.observeNonNull(this) { plans ->
            viewModel.localMembershipCardData.observeNonNull(this) { cards ->
                viewModel.paymentCards.observeNonNull(this) { paymentCards ->
                    setupRecycler(paymentCards, plans, cards)
                }
            }
        }
    }

    private fun setupRecycler(
        paymentCards: List<PaymentCard>,
        plans: List<MembershipPlan>,
        cards: List<MembershipCard>
    ) {
        viewModel.paymentCardsLoadedCount.value = paymentCards.size
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
