package com.bink.wallet.scenes.onboarding

import com.bink.wallet.modal.generic.BaseModalViewModel
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository

class OnboardingViewModel(
    var loyaltyWalletRepository: LoyaltyWalletRepository,
    var paymentWalletRepository: PaymentWalletRepository
) : BaseModalViewModel() {

    fun clearWallets() {
        loyaltyWalletRepository.clearMembershipCards()
        paymentWalletRepository.clearPaymentCards()
    }

}