package com.bink.wallet.modal.card_terms_and_conditions

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.R
import com.bink.wallet.modal.generic.GenericModalFragment
import com.bink.wallet.model.response.payment_card.Account
import com.bink.wallet.model.response.payment_card.BankCard
import com.bink.wallet.model.response.payment_card.Consent
import com.bink.wallet.model.response.payment_card.PaymentCardAdd
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNetworkDrivenErrorNonNull
import com.bink.wallet.utils.observeNonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class CardTermsAndConditionsFragment : GenericModalFragment() {
    override val viewModel: CardTermsAndConditionsViewModel by viewModel()

    companion object {
        const val DEFAULT_CONSENT_TYPE = 0
        const val DEFAULT_LATITUDE = 0.0f
        const val DEFAULT_LONGITUDE = 0.0f
        const val DEFAULT_ACCOUNT_STATUS = 0
        const val DIVISOR_MILLISECONDS = 1000
    }

    var userBankCard: BankCard? = null
    var cardNumber: String = ""

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            CardTermsAndConditionsFragmentArgs.fromBundle(bundle).apply {
                setupUi(this.genericModalParameters)
                userBankCard = this.bankCard
                cardNumber = this.cardNo
            }
        }

        viewModel.fetchLocalMembershipCards()
        viewModel.fetchLocalMembershipPlans()

        viewModel.paymentCard.observeNonNull(this) { paymentCard ->
            viewModel.localMembershipPlanData.value?.let { plans ->
                viewModel.localMembershipCardData.value?.let { cards ->
                    findNavController().navigateIfAdded(
                        this,
                        CardTermsAndConditionsFragmentDirections.cardTermsToDetails(
                            paymentCard,
                            plans.toTypedArray(),
                            cards.toTypedArray()
                        )
                    )
                }
            }
        }

        viewModel.error.observeNetworkDrivenErrorNonNull(
            requireContext(),
            this,
            getString(R.string.payment_card_error_title),
            getString(R.string.payment_card_error_message),
            true
        ) {
            binding.progressSpinner.visibility = View.GONE
            binding.firstButton.isEnabled = true
        }
    }

    override fun onFirstButtonClicked() {
        if (UtilFunctions.isNetworkAvailable(requireContext(), true)) {
            binding.firstButton.isEnabled = false
            binding.progressSpinner.visibility = View.VISIBLE
            userBankCard?.let {
                viewModel.sendAddCard(
                    PaymentCardAdd(
                        it,
                        Account(
                            false,
                            DEFAULT_ACCOUNT_STATUS,
                            listOf(
                                Consent(
                                    DEFAULT_CONSENT_TYPE,
                                    DEFAULT_LATITUDE,
                                    DEFAULT_LONGITUDE,
                                    System.currentTimeMillis() / DIVISOR_MILLISECONDS
                                )
                            )
                        )
                    ), cardNumber
                )
            }
        }
    }

    override fun onSecondButtonClicked() {
        if (UtilFunctions.isNetworkAvailable(requireContext(), true)) {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}