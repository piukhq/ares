package com.bink.wallet.scenes.payment_card_details

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.PaymentCardsDetailsFragmentBinding
import com.bink.wallet.model.payment_card.RebuildPaymentCard
import com.bink.wallet.model.response.payment_card.PaymentMembershipCard
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentCardsDetails :
    BaseFragment<PaymentCardsDetailsViewModel, PaymentCardsDetailsFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val viewModel: PaymentCardsDetailsViewModel by viewModel()

    override val layoutRes: Int
        get() = R.layout.payment_cards_details_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with (binding.toolbar) {
            setNavigationIcon(R.drawable.ic_close)
            setNavigationOnClickListener {
                goHome()
            }
        }

        val securityDialog = SecurityDialogs(requireContext())

        arguments?.let {
            val currentBundle = PaymentCardsDetailsArgs.fromBundle(it)

            with (viewModel) {
                paymentCard.value = currentBundle.paymentCard
                membershipCardData.value = currentBundle.membershipCards.toList()
                membershipPlanData.value = currentBundle.membershipPlans.toList()
            }
        }

        binding.paymentCardDetail = viewModel.paymentCard.value

        binding.footerSecurity.setOnClickListener {
            securityDialog.openDialog(layoutInflater)
        }

        binding.footerDelete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val dialog: AlertDialog
            builder.setMessage(getString(R.string.delete_card_modal_body))
            builder.setNeutralButton(getString(R.string.no_text)) { _, _ -> }
            builder.setPositiveButton(getString(R.string.yes_text)) { _, _ ->
                if (verifyAvailableNetwork(requireActivity())) {
                    runBlocking {
                        viewModel.deletePaymentCard(viewModel.paymentCard.value?.id.toString())
                    }
                } else {
                    showNoInternetConnectionDialog()
                }
            }
            dialog = builder.create()
            dialog.show()
        }

        with (viewModel.paymentCard) {
            if (value != null &&
                value!!.card != null &&
                value!!.card!!.isExpired()) {
                with(binding.paymentHeader) {
                    cardExpired.visibility = View.VISIBLE
                    linkStatus.visibility = View.GONE
                    imageStatus.visibility = View.GONE
                }
            }
        }

        viewModel.membershipPlanData.observeNonNull(this) { plans ->
            viewModel.membershipCardData.observeNonNull(this) { cards ->
                binding.linkedCardsList.apply {
                    layoutManager = GridLayoutManager(context, 1)
        with (viewModel) {
            linkedPaymentCard.observeNonNull(this@PaymentCardsDetails) {
                val link = linkedPaymentCard.value!!
                val paymentMembershipCards = paymentCard.value?.membership_cards
                val item = paymentMembershipCards!!.firstOrNull {it.id == link.id.toString()}
                val cards: List<PaymentMembershipCard>
                if (item == null) {
                    cards = ArrayList(paymentCard.value!!.membership_cards)
                    cards.add(
                        PaymentMembershipCard(link.id.toString(), true)
                    )
                } else {
                    cards = ArrayList(paymentCard.value!!.membership_cards)
                    cards[paymentMembershipCards.indexOf(item)] =
                        PaymentMembershipCard(item.id, item.active_link!!.not())
                }
                paymentCard.value = RebuildPaymentCard.rebuild(paymentCard.value!!, cards)
                (binding.linkedCardsList.adapter as LinkedCardsAdapter).
                    updatePaymentCard(link)
            }
            membershipPlanData.observeNonNull(this@PaymentCardsDetails) { plans ->
                membershipCardData.observeNonNull(this@PaymentCardsDetails) { cards ->
                    binding.linkedCardsList.apply {
                        val pllCards = plans.filter { it.getCardType() == CardType.PLL }
                        val notLinkedPllCards = pllCards.filterNot { plan ->
                            cards.any {
                                plan.id == it.membership_plan
                            }
                        }
                        val linkableCards = cards.filter { card ->
                            pllCards.any {
                                it.id == card.membership_plan
                            }
                        }

                        layoutManager = GridLayoutManager(context, 1)

                        adapter = LinkedCardsAdapter(
                            linkableCards,
                            pllCards,
                            ArrayList(notLinkedPllCards),
                            ArrayList(paymentCard.value?.membership_cards!!),
                            onLinkStatusChange = { onLinkStatusChange(it) },
                            itemClickListener = {
                                val directions =
                                    PaymentCardsDetailsDirections.paymentDetailsToAddJoin(it)
                                findNavController().navigateIfAdded(
                                    this@PaymentCardsDetails,
                                    directions
                                )
                            }
                        )
                    }
                }
            }
        }

        viewModel.deleteRequest.observeNonNull(this) {
            findNavController().navigateIfAdded(this, R.id.global_to_home)
        }

        viewModel.deleteError.observeNonNull(this) {
            requireContext().displayModalPopup(
                "",
                getString(R.string.card_error_dialog)
            )
        }
    }

    private fun goHome() {
        findNavController().navigateIfAdded(
            this,
            R.id.global_to_home
        )
    }

    private fun onLinkStatusChange(currentItem: Pair<String?, Boolean>) {
        if (currentItem.first != null) {
            runBlocking {
                if (currentItem.second) {
                    viewModel.linkPaymentCard(
                        currentItem.first!!,
                        viewModel.paymentCard.value?.id.toString()
                    )
                } else {
                    viewModel.unlinkPaymentCard(
                        currentItem.first!!,
                        viewModel.paymentCard.value?.id.toString()
                    )
                }
            }
        }
    }
}
