package com.bink.wallet.scenes.loyalty_wallet;

public interface LoyaltyWalletPresentationLogic {
    fun presentSomething(response: LoyaltyWalletModels.Something.Response)
}

class LoyaltyWalletPresenter : LoyaltyWalletPresentationLogic {
    var fragment: LoyaltyWalletDisplayLogic? = null

    override fun presentSomething(response: LoyaltyWalletModels.Something.Response) {
        val viewModel = LoyaltyWalletModels.Something.ViewModel()
        fragment?.displaySomething(viewModel)
    }

}