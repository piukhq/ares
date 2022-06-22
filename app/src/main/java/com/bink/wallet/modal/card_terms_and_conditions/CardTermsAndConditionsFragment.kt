package com.bink.wallet.modal.card_terms_and_conditions

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.modal.generic.GenericModalFragment
import com.bink.wallet.model.response.payment_card.Account
import com.bink.wallet.model.response.payment_card.BankCard
import com.bink.wallet.model.response.payment_card.Consent
import com.bink.wallet.model.response.payment_card.PaymentCardAdd
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.ADD_PAYMENT_CARD_REQUEST
import com.bink.wallet.utils.FirebaseEvents.ADD_PAYMENT_CARD_RESPONSE_FAILURE
import com.bink.wallet.utils.FirebaseEvents.ADD_PAYMENT_CARD_RESPONSE_SUCCESS
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_FALSE
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_TRUE
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException

class CardTermsAndConditionsFragment : GenericModalFragment() {
    override val viewModel: CardTermsAndConditionsViewModel by viewModel()
    override val layoutRes: Int get() = R.layout.generic_modal_fragment

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
                userBankCard = this.bankCard
                cardNumber = this.cardNo
            }
        }

        bindUi()

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
            //add-payment-card-response-success
            val accountIsNew = if (SharedPreferenceManager.addPaymentCardSuccessHttpCode == 201) FIREBASE_TRUE else FIREBASE_FALSE
            if (paymentCard.card?.provider != null && paymentCard.status != null){
                logEvent(ADD_PAYMENT_CARD_RESPONSE_SUCCESS,getAddPaymentCardResponseSuccessMap(paymentCard.id.toString(), paymentCard.card.provider,accountIsNew,paymentCard.status))
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
            //add-payment-card-response-fail
            userBankCard?.provider?.let { provider ->
                if(it is HttpException){
                    val httpException = it
                    logEvent(ADD_PAYMENT_CARD_RESPONSE_FAILURE, getAddPaymentCardFailMap(provider, httpException.code(), httpException.getErrorBody()))
                } else {
                    logEvent(ADD_PAYMENT_CARD_RESPONSE_FAILURE, getAddPaymentCardFailMap(provider, 0, "Unable to cast to Http Exception"))
                }
            }
        }

        viewModel.addCardRequestMade.observeNonNull(this) {
            //add-loyalty-card-request
            userBankCard?.provider?.let {
                logEvent(ADD_PAYMENT_CARD_REQUEST, getAddPaymentCardGenericMap(it))
            }
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
            findNavController().popBackStack()
        }
    }

    private fun bindUi() {
        binding.title.text = getString(R.string.terms_and_conditions_title)
        binding.description.text = getString(R.string.terms_and_conditions_text)

        val secondDescriptionPrefix =
            getString(R.string.terms_and_conditions_second_paragraph_prefix)
        val secondDescriptionMiddle =
            getString(R.string.terms_and_conditions_second_paragraph_privacy_policy)
        val secondDescriptionSuffix =
            getString(R.string.terms_and_conditions_second_paragraph_suffix)
        val ss =
            SpannableString(secondDescriptionPrefix + secondDescriptionMiddle + secondDescriptionSuffix)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                findNavController().navigateIfAdded(
                    this@CardTermsAndConditionsFragment,
                    CardTermsAndConditionsFragmentDirections.globalToWeb(
                        "https://bink.com/privacy-policy/"
                    ), R.id.card_terms_and_conditions
                )
            }
        }
        ss.setSpan(
            clickableSpan,
            secondDescriptionPrefix.length,
            secondDescriptionPrefix.length + secondDescriptionMiddle.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.descriptionSecondPart.text = ss
        binding.descriptionSecondPart.movementMethod = LinkMovementMethod.getInstance()
    }
}