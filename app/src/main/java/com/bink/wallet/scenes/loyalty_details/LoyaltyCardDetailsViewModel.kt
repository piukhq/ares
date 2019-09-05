package com.bink.wallet.scenes.loyalty_details

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.enums.LoginStatus


class LoyaltyCardDetailsViewModel(private val repository: LoyaltyCardDetailsRepository) :
    BaseViewModel() {
    var tiles = MutableLiveData<List<String>>()
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var membershipCard = MutableLiveData<MembershipCard>()
    var updatedMembershipCard = MutableLiveData<MembershipCard>()
    var deletedCard = MutableLiveData<String>()
    var deleteError = MutableLiveData<String>()
    var accountStatus = MutableLiveData<LoginStatus>()

    companion object {
        const val STATUS_AUTHORISED = "authorised"
        const val STATUS_PENDING = "pending"
        const val STATUS_FAILED = "failed"
        const val STATUS_UNAUTHORISED = "unauthorised"
    }
    suspend fun deleteCard(id: String?) {
        repository.deleteMembershipCard(id, deletedCard, deleteError)
    }

    suspend fun updateMembershipCard(){
        membershipCard.value?.id?.let { repository.refreshMembershipCard(it, updatedMembershipCard) }
    }

    fun setAccountStatus() {
        if(membershipPlan.value?.feature_set?.has_points == true || membershipPlan.value?.feature_set?.transactions_available == true) {
            when(membershipCard.value?.status?.state){
                STATUS_AUTHORISED -> {
                    if (membershipPlan.value?.feature_set?.transactions_available == true) {
                        accountStatus.value = LoginStatus.STATUS_LOGGED_IN_HISTORY_AVAILABLE
                    } else {
                        accountStatus.value = LoginStatus.STATUS_LOGGED_IN_HISTORY_UNAVAILABLE
                    }
                }
                STATUS_UNAUTHORISED -> {
                    if (membershipPlan.value?.feature_set?.transactions_available == true) {
                        accountStatus.value = LoginStatus.STATUS_NOT_LOGGED_IN_HISTORY_AVAILABLE
                    } else {
                        accountStatus.value = LoginStatus.STATUS_NOT_LOGGED_IN_HISTORY_UNAVAILABLE
                    }
                }
                STATUS_PENDING -> {
                    if (membershipCard.value?.status?.reason_codes?.intersect(listOf("X200")) != null) {
                        accountStatus.value = LoginStatus.STATUS_SIGN_UP_PENDING
                    }
                    if (membershipCard.value?.status?.reason_codes?.intersect(
                            listOf(
                                "X100",
                                "X301"
                            )
                        ) != null
                    ) {
                        accountStatus.value = LoginStatus.STATUS_LOGIN_FAILED
                    }
                }

                STATUS_FAILED -> {
                    if (membershipCard.value?.status?.reason_codes?.intersect(listOf("X201")) != null) {
                        accountStatus.value = LoginStatus.STATUS_SIGN_UP_FAILED
                    }
                    if (membershipCard.value?.status?.reason_codes?.intersect(listOf("X202")) != null) {
                        accountStatus.value = LoginStatus.STATUS_CARD_ALREADY_EXISTS
                    }
                    if (membershipCard.value?.status?.reason_codes?.intersect(
                            listOf(
                                "X101",
                                "X102",
                                "X103",
                                "X104",
                                "X302",
                                "X303",
                                "X304"
                            )
                        ) != null
                    ) {
                        accountStatus.value = LoginStatus.STATUS_LOGIN_FAILED
                    }
                }
            }
        } else {
            accountStatus.value = LoginStatus.STATUS_LOGIN_UNAVAILABLE
        }
    }

}