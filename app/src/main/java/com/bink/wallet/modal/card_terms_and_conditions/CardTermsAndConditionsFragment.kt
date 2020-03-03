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
import com.bink.wallet.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException
import java.net.SocketTimeoutException

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
        viewModel.error.observeNonNull(this) {
            binding.progressSpinner.visibility = View.GONE
            binding.firstButton.isEnabled = true
            handleNetworkError(it)
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

    private fun handleNetworkError(throwable: Throwable) {
        if (UtilFunctions.isNetworkAvailable(requireContext(), true)) {
            if (((throwable is HttpException) && throwable.code() >= ApiErrorUtils.SERVER_ERROR) || throwable is SocketTimeoutException) {
                requireContext().displayModalPopup(
                    requireContext().getString(R.string.error_server_down_title),
                    requireContext().getString(R.string.error_server_down_message)
                )
            }
        } else {
            context?.displayModalPopup(
                context?.getString(R.string.payment_card_error_title),
                context?.getString(R.string.payment_card_error_message)
            )
        }
    }
}