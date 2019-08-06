package com.bink.wallet.scenes.loyalty_wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bink.wallet.scenes.loyalty_wallet.model.MembershipCard

class LoyaltyViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository) : ViewModel() {

    var membershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()
    var deleteCard: MutableLiveData<Any> = MutableLiveData()

    fun deleteCard(){
        deleteCard = loyaltyWalletRepository.deleteMembershipCard()
    }

    fun fetchMembershipCards(){
        membershipCardData = loyaltyWalletRepository.retrieveMembershipCards()
    }
}
