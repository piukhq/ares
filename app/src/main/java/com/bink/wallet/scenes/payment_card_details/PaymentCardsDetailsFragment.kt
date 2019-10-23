package com.bink.wallet.scenes.payment_card_details

import android.app.AlertDialog
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.PaymentCardsDetailsFragmentBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentCardsDetailsFragment :
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

        binding.toolbar.setNavigationIcon(R.drawable.ic_close)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateIfAdded(this, R.id.global_to_home)
        }

        val securityDialog = SecurityDialogs(requireContext())

        arguments?.let {
            val currentBundle = PaymentCardsDetailsFragmentArgs.fromBundle(it)

            viewModel.paymentCard.value = currentBundle.paymentCard
            viewModel.membershipCardData.value = currentBundle.membershipCards.toList()
            viewModel.membershipPlanData.value = currentBundle.membershipPlans.toList()
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

        viewModel.membershipPlanData.observeNonNull(this) { plans ->
            viewModel.membershipCardData.observeNonNull(this) { cards ->
                binding.linkedCardsList.apply {
                    layoutManager = GridLayoutManager(context, 1)

                    if (viewModel.paymentCard.value?.membership_cards!!.isNotEmpty()) {
                        adapter = LinkedCardsAdapter(
                            cards,
                            plans,
                            viewModel.paymentCard.value?.membership_cards!!,
                            onLinkStatusChange = ::onLinkStatusChange,
                            onItemSelected = ::onItemSelected
                        )
                    } else {
                        adapter = SuggestedCardsAdapter(
                            plans.filter { it.getCardType() == CardType.PLL },
                            itemClickListener = {
                                val directions =
                                    PaymentCardsDetailsFragmentDirections.paymentDetailsToAddJoin(it)
                                findNavController().navigateIfAdded(
                                    this@PaymentCardsDetailsFragment,
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

    private fun onLinkStatusChange(currentItem: Pair<String?, Boolean>) {
        runBlocking {
            if (currentItem.first != null && currentItem.second) {
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

    private fun onItemSelected(membershipPlan: MembershipPlan, membershipCard: MembershipCard) {
        val directions = PaymentCardsDetailsFragmentDirections.paymentDetailsToLoyaltyCardDetail(
            membershipPlan,
            membershipCard
        )
        directions.let { _ -> findNavController().navigateIfAdded(this, directions) }
    }
}
