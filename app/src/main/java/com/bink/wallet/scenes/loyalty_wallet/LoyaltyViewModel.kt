package com.bink.wallet.scenes.loyalty_wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bink.wallet.BaseViewModel
import com.bink.wallet.scenes.browse_brands.BrowseBrandsRepository
import com.bink.wallet.scenes.browse_brands.model.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.model.MembershipCard

class LoyaltyViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository, private val browseBrandsRepository: BrowseBrandsRepository) : BaseViewModel() {

    var membershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()
    var deleteCard: MutableLiveData<String> = MutableLiveData()
    var membershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()

    fun deleteCard(id: String?) {
        loyaltyWalletRepository.deleteMembershipCard(id, deleteCard)
    }

    fun fetchMembershipCards(){
        loyaltyWalletRepository.retrieveMembershipCards(membershipCardData)
    }

    fun fetchMembershipPlans() {
        membershipPlanData = browseBrandsRepository.fetchMembershipPlans()
    }
}
