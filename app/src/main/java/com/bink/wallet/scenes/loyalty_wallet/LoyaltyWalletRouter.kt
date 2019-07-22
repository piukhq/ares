package com.bink.wallet.scenes.loyalty_wallet

import androidx.navigation.NavController

interface LoyaltyWalletRoutingLogic {
    fun routeToSomewhere()
}

interface LoyaltyWalletDataPassing {
    var dataStore: LoyaltyWalletDataStore?
}

interface ILoyaltyWalletRouter : LoyaltyWalletRoutingLogic, LoyaltyWalletDataPassing {
}

class LoyaltyWalletRouter : ILoyaltyWalletRouter {

    var fragment: LoyaltyWalletFragment? = null
    override var dataStore: LoyaltyWalletDataStore? = null
    var navController: NavController? = null


    override fun routeToSomewhere() {
        //todo create graph action
        //        navController.navigate(R.id.nav_graph)
        // Navigate to the destination fragment, passing data from the dataStore
    }

}