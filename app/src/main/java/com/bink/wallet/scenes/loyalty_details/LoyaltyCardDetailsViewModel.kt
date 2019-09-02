package com.bink.wallet.scenes.loyalty_details

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan


class LoyaltyCardDetailsViewModel(private val repository: LoyaltyCardDetailsRepository) :
    BaseViewModel() {
    var tiles = MutableLiveData<List<String>>()
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var membershipCard = MutableLiveData<MembershipCard>()
    var deletedCard = MutableLiveData<String>()
    var deleteError = MutableLiveData<String>()
    var accountStatus = MutableLiveData<Double>()

    companion object {
        const val STATUS_LOGGED_IN_HISTORY_AVAILABLE = 1.1
        const val STATUS_LOGGED_IN_HISTORY_UNAVAILABLE = 1.2
        const val STATUS_NOT_LOGGED_IN_HISTORY_UNAVAILABLE = 1.3
        const val STATUS_NOT_LOGGED_IN_HISTORY_AVAILABLE = 1.4
        const val STATUS_LOGIN_UNAVAILABLE = 1.5
        const val STATUS_LOGIN_FAILED = 1.6
        const val STATUS_LOGIN_PENDING = 1.7
        const val STATUS_SIGN_UP_FAILED= 1.8
        const val STATUS_SIGN_UP_PENDING = 1.9
        const val STATUS_REGISTER_GHOST_CARD_FAILED = 1.10
        const val STATUS_REGISTER_GHOST_CARD_PENDING = 1.11
        const val STATUS_1_12 = 1.12

        const val STATUS_AUTHORISED = "authorised"
        const val STATUS_PENDING = "pending"
        const val STATUS_FAILED = "failed"
        const val STATUS_UNAUTHORISED = "unauthorised"
    }
    suspend fun deleteCard(id: String?) {
        repository.deleteMembershipCard(id, deletedCard, deleteError)
    }

    fun setAccountStatus() {
        if(membershipPlan.value?.feature_set?.has_points == true || membershipPlan.value?.feature_set?.transactions_available == true) {
            when(membershipCard.value?.status?.state){
                STATUS_AUTHORISED -> {
                    if (membershipPlan.value?.feature_set?.transactions_available == true) {
                        accountStatus.value = STATUS_LOGGED_IN_HISTORY_AVAILABLE
                    } else {
                        accountStatus.value = STATUS_LOGGED_IN_HISTORY_UNAVAILABLE
                    }
                }
                STATUS_UNAUTHORISED -> {
                    if (membershipPlan.value?.feature_set?.transactions_available == true) {
                        accountStatus.value = STATUS_NOT_LOGGED_IN_HISTORY_AVAILABLE
                    } else {
                        accountStatus.value = STATUS_NOT_LOGGED_IN_HISTORY_UNAVAILABLE
                    }
                }
                STATUS_PENDING -> {
                    when (membershipCard.value?.status?.reason_codes){
                        listOf("X200") -> accountStatus.value = STATUS_SIGN_UP_PENDING
                        listOf("X000", "X301") -> accountStatus.value = STATUS_LOGIN_PENDING
                    }
                }

                STATUS_FAILED -> {
                    when (membershipCard.value?.status?.reason_codes) {
                        listOf("X201") -> accountStatus.value = STATUS_SIGN_UP_FAILED
                        listOf("X101","X102","X103","X104","X302","X303","X304") -> STATUS_LOGIN_FAILED
                    }
                }
            }
        } else {
            accountStatus.value = STATUS_LOGIN_UNAVAILABLE
        }
    }

}