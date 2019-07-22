package com.bink.wallet.scenes.loyalty_wallet

interface LoyaltyWalletBusinessLogic {
    fun doSomething(request: LoyaltyWalletModels.Something.Request)
}

interface LoyaltyWalletDataStore {
//    var name: String
//    var address: String

}

class LoyaltyWalletInteractor : LoyaltyWalletBusinessLogic, LoyaltyWalletDataStore {
    var presenter: LoyaltyWalletPresentationLogic? = null
    var worker: LoyaltyWalletWorker? = null
//    override var name: String = ""
//    override var address: String = ""

    override fun doSomething(request: LoyaltyWalletModels.Something.Request) {
        worker = LoyaltyWalletWorker()
        worker?.doSomeWork()

        val response = LoyaltyWalletModels.Something.Response()
        presenter?.presentSomething(response)
    }

}