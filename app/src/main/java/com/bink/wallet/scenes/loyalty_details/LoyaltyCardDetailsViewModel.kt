package com.bink.wallet.scenes.loyalty_details

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.MembershipPlanUtils
import com.bink.wallet.utils.enums.LinkStatus
import com.bink.wallet.utils.enums.LoginStatus
import com.bink.wallet.utils.enums.MembershipCardStatus


class LoyaltyCardDetailsViewModel(private val repository: LoyaltyCardDetailsRepository) :
    BaseViewModel() {
    var tiles = MutableLiveData<List<String>>()
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var membershipCard = MutableLiveData<MembershipCard>()
    var paymentCards = MutableLiveData<List<PaymentCard>>()
    var localPaymentCards = MutableLiveData<List<PaymentCard>>()
    var localPaymentFetchError = MutableLiveData<Throwable>()
    var updatedMembershipCard = MutableLiveData<MembershipCard>()
    var deletedCard = MutableLiveData<String>()
    var deleteError = MutableLiveData<Throwable>()
    var accountStatus = MutableLiveData<LoginStatus>()
    var linkStatus = MutableLiveData<LinkStatus>()

    suspend fun deleteCard(id: String?) {
        repository.deleteMembershipCard(id, deletedCard, deleteError)
    }

    suspend fun updateMembershipCard() {
        membershipCard.value?.id?.let {
            repository.refreshMembershipCard(
                it,
                updatedMembershipCard
            )
        }
    }

    suspend fun fetchPaymentCards() {
        repository.getPaymentCards(paymentCards)
    }

    fun fetchLocalPaymentCards() {
        repository.getLocalPaymentCards(localPaymentCards, localPaymentFetchError)
    }

    fun setAccountStatus() {
        membershipPlan.value?.let { plan ->
            membershipCard.value?.let { card ->
                accountStatus.value =
                    MembershipPlanUtils.getAccountStatus(plan, card)
            }
        }
    }

    fun setLinkStatus() {
        membershipPlan.value?.let { membershipPlan ->
            membershipCard.value?.let { membershipCard ->
                localPaymentCards.value?.let { paymentCards ->
                    linkStatus.value = MembershipPlanUtils.getLinkStatus(
                        membershipPlan,
                        membershipCard,
                        paymentCards.toMutableList()
                    )

                }
            }
        }
    }
}

