package com.bink.wallet.scenes.loyalty_details

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.MembershipPlanUtils
import com.bink.wallet.utils.enums.CardStatus
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.enums.LinkStatus
import com.bink.wallet.utils.enums.LoginStatus


class LoyaltyCardDetailsViewModel(private val repository: LoyaltyCardDetailsRepository) :
    BaseViewModel() {
    var tiles = MutableLiveData<List<String>>()
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var membershipCard = MutableLiveData<MembershipCard>()
    var paymentCards = MutableLiveData<List<PaymentCard>>()
    var updatedMembershipCard = MutableLiveData<MembershipCard>()
    var deletedCard = MutableLiveData<String>()
    var deleteError = MutableLiveData<String>()
    var accountStatus = MutableLiveData<LoginStatus>()
    var linkStatus = MutableLiveData<LinkStatus>()
    var errorCodes = MutableLiveData<String>()

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
        accountStatus.value =
            MembershipPlanUtils.getAccountStatus(membershipPlan.value!!, membershipCard.value!!)
    }

    fun setLinkStatus() {
        when (membershipPlan.value?.feature_set?.card_type) {
            CardType.PLL.type -> {
                when (membershipCard.value?.status?.state) {
                    CardStatus.AUTHORISED.status -> {
                        when {
                            paymentCards.value.isNullOrEmpty() -> linkStatus.value =
                                LinkStatus.STATUS_LINKABLE_NO_PAYMENT_CARDS
                            membershipCard.value?.payment_cards.isNullOrEmpty()
                                    || !existLinkedPaymentCards() -> linkStatus.value =
                                LinkStatus.STATUS_LINKABLE_NO_PAYMENT_CARDS_LINKED
                            else -> linkStatus.value = LinkStatus.STATUS_LINKED_TO_SOME_OR_ALL
                        }
                    }
                    CardStatus.UNAUTHORISED.status -> {
                        linkStatus.value = LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH
                    }
                    CardStatus.PENDING.status -> {
                        linkStatus.value = LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_PENDING
                    }
                    CardStatus.FAILED.status -> {
                        linkStatus.value = LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_PENDING_FAILED
                    }
                }
            }
            CardType.VIEW.type, CardType.STORE.type -> {
                linkStatus.value = LinkStatus.STATUS_UNLINKABLE
            }
        }
        errorCodes.value = null
        membershipCard.value?.status?.reason_codes?.forEach { errorCodes.value+= "$it " }
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

