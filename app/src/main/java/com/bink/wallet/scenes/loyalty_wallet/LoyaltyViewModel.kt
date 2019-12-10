package com.bink.wallet.scenes.loyalty_wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.BannerDisplay
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan

class LoyaltyViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository) :
    BaseViewModel() {

    val membershipCardData = MutableLiveData<List<MembershipCard>>()
    val deleteCard = MutableLiveData<String>()
    val membershipPlanData = MutableLiveData<List<MembershipPlan>>()
    val localMembershipPlanData = MutableLiveData<List<MembershipPlan>>()
    val localMembershipCardData = MutableLiveData<List<MembershipCard>>()
    val dismissedCardData: MutableLiveData<List<BannerDisplay>> = MutableLiveData()
    val addError = MutableLiveData<Throwable>()
    val fetchError = MutableLiveData<Throwable>()

    suspend fun deleteCard(id: String?) {
        loyaltyWalletRepository.deleteMembershipCard(id, deleteCard)
    }

    fun fetchMembershipCards() {
        loyaltyWalletRepository.retrieveMembershipCards(membershipCardData)
    }

    fun fetchMembershipPlans() {
        loyaltyWalletRepository.retrieveMembershipPlans(membershipPlanData)
    }

    fun fetchLocalMembershipCards() {
        loyaltyWalletRepository.retrieveStoredMembershipCards(localMembershipCardData)
    }

    fun fetchLocalMembershipPlans() {
        loyaltyWalletRepository.retrieveStoredMembershipPlans(localMembershipPlanData)
    }

    fun fetchDismissedCards() {
        loyaltyWalletRepository.retrieveDismissedCards(dismissedCardData, fetchError)
    }

    fun addPlanIdAsDismissed(id: String) {
        loyaltyWalletRepository.addBannerAsDismissed(id, addError)
    }
}
