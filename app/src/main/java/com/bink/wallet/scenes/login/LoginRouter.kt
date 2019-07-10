package com.bink.wallet.scenes.login

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity

//const val NAME = "NAME"
//const val ADDRESS = "ADDRESS"

interface LoginRoutingLogic
{
    fun routeToSomewhere(nextScreenIntent: Intent)
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

    // Routing

    override fun routeToSomewhere(nextScreenIntent: Intent)
    {
        nextScreenIntent.apply {

            // Pass data to the destination activity

//            putExtra(NAME, dataStore?.name)
//            putExtra(ADDRESS, dataStore?.address)
        }

        startActivity(activity as Context, nextScreenIntent, null)
    }
}
