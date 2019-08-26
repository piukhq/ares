package com.bink.wallet.scenes.loyalty_wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.membership_card.MembershipCard

class LoyaltyViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository) : BaseViewModel() {

    var membershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()
    var deleteCard: MutableLiveData<String> = MutableLiveData()
    var membershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()
    var localMembershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()
    var localMembershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()

    fun deleteCard(id: String?) {
        loyaltyWalletRepository.deleteMembershipCard(id, deleteCard)
    }

    fun fetchMembershipCards(){
        loyaltyWalletRepository.retrieveMembershipCards(membershipCardData)
    }

    fun fetchLocalMembershipCards() {
        loyaltyWalletRepository.retrieveStoredMembershipCards(localMembershipCardData)
    }

    fun fetchMembershipPlans() {
        loyaltyWalletRepository.retrieveMembershipPlans(membershipPlanData)
    }

    fun fetchLocalMembershipPlans() {
        loyaltyWalletRepository.retrieveStoredMembershipPlans(localMembershipPlanData)
    }
}
