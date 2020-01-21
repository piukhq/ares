package com.bink.wallet.scenes.loyalty_details

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.MembershipPlanUtils
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.enums.LinkStatus
import com.bink.wallet.utils.enums.LoginStatus
import com.bink.wallet.utils.enums.MembershipCardStatus


class LoyaltyCardDetailsViewModel(private val repository: LoyaltyCardDetailsRepository) :
    BaseViewModel() {
    var tiles = MutableLiveData<List<String>>()
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var membershipCard = MutableLiveData<MembershipCard>()
    var paymentCards = MutableLiveData<List<PaymentCard>>()
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

    fun setAccountStatus() {
        membershipPlan.value?.let { plan ->
            membershipCard.value?.let { card ->
                accountStatus.value =
                    MembershipPlanUtils.getAccountStatus(plan, card)
            }
        }
    }

    fun setLinkStatus() {
        when (membershipPlan.value?.feature_set?.card_type) {
            CardType.PLL.type -> {
                when (membershipCard.value?.status?.state) {
                    MembershipCardStatus.AUTHORISED.status -> {
                        when {
                            paymentCards.value.isNullOrEmpty() ->
                                linkStatus.value = LinkStatus.STATUS_LINKABLE_NO_PAYMENT_CARDS
                            membershipCard.value?.payment_cards.isNullOrEmpty() ||
                                    !existLinkedPaymentCards() ->
                                linkStatus.value =
                                    LinkStatus.STATUS_LINKABLE_NO_PAYMENT_CARDS_LINKED
                            else ->
                                linkStatus.value = LinkStatus.STATUS_LINKED_TO_SOME_OR_ALL
                        }
                    }
                    MembershipCardStatus.UNAUTHORISED.status -> {
                        linkStatus.value = LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH
                    }
                    MembershipCardStatus.PENDING.status -> {
                        linkStatus.value = LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_PENDING
                    }
                    MembershipCardStatus.FAILED.status -> {
                        linkStatus.value = LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_PENDING_FAILED
                    }
                }
            }
            CardType.VIEW.type, CardType.STORE.type -> {
                linkStatus.value = LinkStatus.STATUS_UNLINKABLE
            }
        }
    }

    private fun existLinkedPaymentCards(): Boolean {
        membershipCard.value?.payment_cards?.forEach { card ->
            if (card.active_link == true) {
                return true
            }
        }
        return false
    }
}

