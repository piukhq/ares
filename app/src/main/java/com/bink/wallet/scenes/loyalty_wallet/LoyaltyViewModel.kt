package com.bink.wallet.scenes.loyalty_wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bink.wallet.BaseViewModel
import com.bink.wallet.scenes.loyalty_wallet.model.MembershipCard

class LoyaltyViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository) : BaseViewModel() {

    var membershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()
    var deleteCard: MutableLiveData<String> = MutableLiveData()

    fun deleteCard(id: String?) {
        loyaltyWalletRepository.deleteMembershipCard(id, deleteCard)
    }

    fun fetchMembershipCards(){
        loyaltyWalletRepository.retrieveMembershipCards(membershipCardData)
    }
}
