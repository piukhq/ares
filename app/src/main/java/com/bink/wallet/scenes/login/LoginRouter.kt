package com.bink.wallet.scenes.login

import android.content.Intent

//const val NAME = "NAME"
//const val ADDRESS = "ADDRESS"

interface LoginRoutingLogic
{
    //fun routeToSomewhere(segue: UIStoryboardSegue?)
}

interface LoginDataPassing
{
    var dataStore: LoginDataStore?
}

interface ILoginRouter : LoginRoutingLogic, LoginDataPassing {
}


class LoginRouter : ILoginRouter {

    var activity: LoginActivity? = null
    override var dataStore: LoginDataStore? = null

    // MARK: Routing

    fun routeToSomewhere(nextScreenIntent: Intent)
    {
//        val nextScreenIntent = nextScreenIntent.apply {
//
//            // Pass data to the destination activity
//
//            putExtra(NAME, dataStore.name)
//            putExtra(ADDRESS, dataStore.address)
//        }
//
//        startActivity(activity as Context, nextScreenIntent, null)
    }
}
