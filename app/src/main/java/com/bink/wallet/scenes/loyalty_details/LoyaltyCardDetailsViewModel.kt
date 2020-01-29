package com.bink.wallet.scenes.loyalty_details

import androidx.lifecycle.LiveData
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
    val tiles = MutableLiveData<List<String>>()
    val membershipPlan = MutableLiveData<MembershipPlan>()
    val membershipCard = MutableLiveData<MembershipCard>()
    val paymentCards = MutableLiveData<List<PaymentCard>>()
    val localPaymentCards = MutableLiveData<List<PaymentCard>>()
    private val _localPaymentFetchError = MutableLiveData<Throwable>()
    val localPaymentFetchError : LiveData<Throwable>
        get() = _localPaymentFetchError
    val updatedMembershipCard = MutableLiveData<MembershipCard>()
    private val _refreshError = MutableLiveData<Throwable>()
    val refreshError : LiveData<Throwable>
        get() = _refreshError
    val deletedCard = MutableLiveData<String>()
    private val _deleteError = MutableLiveData<Throwable>()
    val deleteError : LiveData<Throwable>
        get() = _deleteError
    val accountStatus = MutableLiveData<LoginStatus>()
    val linkStatus = MutableLiveData<LinkStatus>()

    suspend fun deleteCard(id: String?) {
        repository.deleteMembershipCard(id, deletedCard, _deleteError)
    }

    fun updateMembershipCard(bool: Boolean = false) {
        membershipCard.value?.id?.let {
            repository.refreshMembershipCard(
                it,
                updatedMembershipCard,
                _refreshError,
                bool
            )
        }
    }

    fun fetchPaymentCards() {
        repository.getPaymentCards(paymentCards)
    }

    fun fetchLocalPaymentCards() {
        repository.getLocalPaymentCards(localPaymentCards, _localPaymentFetchError)
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

