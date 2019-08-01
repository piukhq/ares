package com.bink.wallet.scenes.loyalty_wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bink.wallet.scenes.loyalty_wallet.model.MembershipCard

class LoyaltyViewModel constructor(private val loyaltyWalletRepository: LoyaltyWalletRepository) : ViewModel() {

    var loginData: MutableLiveData<List<MembershipCard>> = loyaltyWalletRepository.retrieveMembershipCards()
    var deleteCard: MutableLiveData<Any> = MutableLiveData()

    fun deleteCard(){
        deleteCard = loyaltyWalletRepository.deleteMembershipCard()
    }
}
