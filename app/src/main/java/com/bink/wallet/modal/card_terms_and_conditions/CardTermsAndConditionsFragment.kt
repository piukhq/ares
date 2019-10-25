package com.bink.wallet.modal.card_terms_and_conditions

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.modal.generic.GenericModalFragment
import com.bink.wallet.model.response.payment_card.Account
import com.bink.wallet.model.response.payment_card.BankCard
import com.bink.wallet.model.response.payment_card.Consent
import com.bink.wallet.model.response.payment_card.PaymentCardAdd
import com.bink.wallet.scenes.add_payment_card.AddPaymentCardFragmentDirections
import com.bink.wallet.utils.navigateIfAdded
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            CardTermsAndConditionsFragmentArgs.fromBundle(bundle).apply {
                setupUi(this.genericModalParameters)
                userBankCard = this.bankCard
            }
        }

        // pre-load the cards on screen open, so we have them ready after card is added
        viewModel.fetchLocalMembershipCards()
        viewModel.fetchLocalMembershipPlans()

        viewModel.paymentCard.observeNonNull(this) {
            val action =
                AddPaymentCardFragmentDirections.addPaymentToDetails(
                    it,
                    viewModel.localMembershipPlanData.value!!.toTypedArray(),
                    viewModel.localMembershipCardData.value!!.toTypedArray()
                )
            findNavController().navigateIfAdded(
                this,
                action
            )
        }
    }

    override fun onFirstButtonClicked() {
        super.onFirstButtonClicked()
        viewModel.sendAddCard(
            PaymentCardAdd(
                userBankCard!!,
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
            )
        )
    }

    override fun onSecondButtonClicked() {
        super.onSecondButtonClicked()
        requireActivity().onBackPressed()
    }
}