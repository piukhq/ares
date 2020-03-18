package com.bink.wallet.scenes.loyalty_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.MembershipPlanUtils
import com.bink.wallet.utils.enums.LinkStatus
import com.bink.wallet.utils.enums.LoginStatus


class LoyaltyCardDetailsViewModel(private val repository: LoyaltyCardDetailsRepository) :
    BaseViewModel() {
    // Data Region
    val tiles = MutableLiveData<List<String>>()
    val membershipPlan = MutableLiveData<MembershipPlan>()
    val membershipCard = MutableLiveData<MembershipCard>()
    private val paymentCards = MutableLiveData<List<PaymentCard>>()
    val localPaymentCards = MutableLiveData<List<PaymentCard>>()
    val deletedCard = MutableLiveData<String>()
    val updatedMembershipCard = MutableLiveData<MembershipCard>()
    val accountStatus = MutableLiveData<LoginStatus>()
    val linkStatus = MutableLiveData<LinkStatus>()

    // Error region
    private val _refreshError = MutableLiveData<Exception>()
    val refreshError: LiveData<Exception>
        get() = _refreshError
    private val _deleteError = MutableLiveData<Exception>()
    val deleteError: LiveData<Exception>
        get() = _deleteError
    private val _paymentCardsFetchError = MutableLiveData<Exception>()
    val paymentCardsFetchError: LiveData<Exception>
        get() = _paymentCardsFetchError
    private val _localPaymentStoreError = MutableLiveData<Exception>()
    val localPaymentStoreError: LiveData<Exception>
        get() = _localPaymentStoreError

    //Merger Region
    private val _paymentCardsMerger = MediatorLiveData<List<PaymentCard>>()
    val paymentCardsMerger: LiveData<List<PaymentCard>>
        get() = _paymentCardsMerger

    init {
        _paymentCardsMerger.addSource(paymentCards) {
            paymentCards.value?.let {
                _paymentCardsMerger.value = paymentCards.value
            }
        }
        _paymentCardsMerger.addSource(localPaymentCards) {
            localPaymentCards.value?.let {
                _paymentCardsMerger.value = localPaymentCards.value
            }
        }
    }
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
        repository.getPaymentCards(paymentCards, _localPaymentStoreError, _paymentCardsFetchError)
    }

    fun fetchLocalPaymentCards() {
        repository.getLocalPaymentCards(localPaymentCards, _paymentCardsFetchError)
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
                _paymentCardsMerger.value?.let { paymentCards ->
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

