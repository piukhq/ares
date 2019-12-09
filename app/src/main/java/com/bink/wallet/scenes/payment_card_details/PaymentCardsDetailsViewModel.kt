package com.bink.wallet.scenes.payment_card_details

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import okhttp3.ResponseBody

class PaymentCardsDetailsViewModel(private var paymentWalletRepository: PaymentWalletRepository) :
    BaseViewModel() {

    var paymentCard = MutableLiveData<PaymentCard>()
    var membershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()
    var membershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()
    var linkedPaymentCard = MutableLiveData<PaymentCard>()
    var unlinkedRequestBody = MutableLiveData<ResponseBody>()
    var deleteRequest = MutableLiveData<ResponseBody>()
    var linkError = MutableLiveData<Throwable>()
    var unlinkError = MutableLiveData<Throwable>()
    var deleteError = MutableLiveData<Throwable>()

    suspend fun linkPaymentCard(cardId: String, paymentCardId: String) {
        updatePaymentCard(cardId)

        paymentWalletRepository.linkPaymentCard(
            cardId,
            paymentCardId,
            linkedPaymentCard,
            linkError,
            paymentCard
        )
    }

    suspend fun unlinkPaymentCard(cardId: String, paymentCardId: String) {
        paymentWalletRepository.unlinkPaymentCard(
            paymentCardId,
            cardId,
            unlinkError,
            unlinkedRequestBody,
            paymentCard
        )
    }

    suspend fun deletePaymentCard(paymentCardId: String) {
        paymentWalletRepository.deletePaymentCard(paymentCardId, deleteRequest, deleteError)
    }

    private fun updatePaymentCard(cardId: String) {
        paymentCard.value?.let {
            if (it.membership_cards.count { card -> card.id == cardId } < 1) {
                it.addPaymentCard(cardId)
            }
        }
    }
}
