package com.bink.wallet.scenes.loyalty_details

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.MembershipPlanUtils
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

    fun setAccountStatus() {
        if (membershipPlan.value != null && membershipCard.value != null)
            accountStatus.value =
                MembershipPlanUtils.getAccountStatus(membershipPlan.value!!, membershipCard.value!!)
    }
}