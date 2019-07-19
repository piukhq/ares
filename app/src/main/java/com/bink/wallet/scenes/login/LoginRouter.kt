package com.bink.wallet.scenes.login

import androidx.navigation.NavController
import com.bink.wallet.R

//const val NAME = "NAME"
//const val ADDRESS = "ADDRESS"

interface LoginRoutingLogic {
    fun routeToHome()
}

interface LoginDataPassing {
    var dataStore: LoginDataStore?
}

interface ILoginRouter : LoginRoutingLogic, LoginDataPassing {
}


class LoginRouter : ILoginRouter {

    var fragment: LoginFragment? = null
    override var dataStore: LoginDataStore? = null
    var navGraph: NavController? = null

    override fun routeToHome() {
//        navGraph?.navigate(R.id.splash_to_home)
    }
}
