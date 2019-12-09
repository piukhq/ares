package com.bink.wallet

import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PllViewModel

class MainViewModel(
    var loyaltyWalletRepository: LoyaltyWalletRepository,
    var pllViewModel: PllViewModel
) :
    BaseViewModel() {

    fun getMembershipPlans() {
        loyaltyWalletRepository.retrieveMembershipPlans()
    }

    fun getMembershipCards() {
        loyaltyWalletRepository.retrieveMembershipCards()
    }

}
