package com.bink.wallet.scenes.payment_card_details

import android.app.AlertDialog
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.PaymentCardsDetailsFragmentBinding
import com.bink.wallet.utils.SecurityDialog
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.*
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

    private val changedCards = HashMap<String, Boolean>()

    override val viewModel: PaymentCardsDetailsViewModel by viewModel()

    val securityDialog = SecurityDialog()

    override val layoutRes: Int
        get() = R.layout.payment_cards_details_fragment

    override fun onPause() {
        for (currentCard in changedCards) {
            runBlocking {
                if (currentCard.value) {
                    viewModel.linkPaymentCard(
                        currentCard.key,
                        viewModel.paymentCard.value?.id.toString()
                    )
                } else {
                    viewModel.unlinkPaymentCard(
                        viewModel.paymentCard.value?.id.toString(),
                        currentCard.key
                    )
                }
            }
        }
        super.onPause()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.setNavigationIcon(R.drawable.ic_close)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateIfAdded(this, R.id.global_to_home)
        }

        arguments?.let {
            val currentBundle = PaymentCardsDetailsArgs.fromBundle(it)

            viewModel.paymentCard.value = currentBundle.paymentCard
            viewModel.membershipCardData.value = currentBundle.membershipCards.toList()
            viewModel.membershipPlanData.value = currentBundle.membershipPlans.toList()
        }

        binding.paymentCardDetail = viewModel.paymentCard.value

        binding.footerSecurity.setOnClickListener {
            securityDialog.openDialog(requireContext(), layoutInflater)
        }

        binding.footerDelete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val dialog: AlertDialog?
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

                    if (viewModel.paymentCard.value?.membership_cards!!.isNotEmpty())
                        adapter = LinkedCardsAdapter(
                            cards,
                            plans,
                            viewModel.paymentCard.value?.membership_cards!!,
                            changedCards
                        )
                    else
                        adapter = SuggestedCardsAdapter(
                            plans.filter { it.getCardType() == CardType.PLL },
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

}
