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
import com.bink.wallet.utils.displayModalPopup
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
            findNavController().navigateIfAdded(
                this,
                R.id.card_terms_to_add
            )
        }
        viewModel.error.observeNonNull(this) {
            if (viewModel.error.value != null) {
                binding.progressSpinner.visibility = View.GONE
                binding.firstButton.isEnabled = true
                requireContext().displayModalPopup(
                    null,
                    getString(R.string.could_not_add_card)
                )
            }
        }
    }

    override fun onFirstButtonClicked() {
        binding.firstButton.isEnabled = false
        binding.progressSpinner.visibility = View.VISIBLE
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
        requireActivity().supportFragmentManager.popBackStack()
    }
}