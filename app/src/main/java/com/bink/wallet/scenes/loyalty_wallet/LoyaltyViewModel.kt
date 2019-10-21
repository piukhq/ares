package com.bink.wallet.scenes.loyalty_wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan

class LoyaltyViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository) :
    BaseViewModel() {

    var membershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()
    var deleteCard: MutableLiveData<String> = MutableLiveData()
    var membershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()
    var localMembershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()
    var localMembershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()

    suspend fun deleteCard(id: String?) {
        loyaltyWalletRepository.deleteMembershipCard(id, deleteCard)
    }

    fun fetchMembershipCards() {
        loyaltyWalletRepository.retrieveMembershipCards(membershipCardData)
    }

    suspend fun fetchMembershipPlans() {
        loyaltyWalletRepository.retrieveMembershipPlans(membershipPlanData)
    }

    fun fetchLocalMembershipCards() {
        loyaltyWalletRepository.retrieveStoredMembershipCards(localMembershipCardData)
    }

    fun fetchLocalMembershipPlans() {
        loyaltyWalletRepository.retrieveStoredMembershipPlans(localMembershipPlanData)
    }
}
