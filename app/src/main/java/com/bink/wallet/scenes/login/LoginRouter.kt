package com.bink.wallet.scenes.login

//const val NAME = "NAME"
//const val ADDRESS = "ADDRESS"

interface LoginRoutingLogic
{
    fun routeToSomewhere()
}

interface LoginDataPassing
{
    var dataStore: LoginDataStore?
}

interface ILoginRouter : LoginRoutingLogic, LoginDataPassing {
}


class LoginRouter : ILoginRouter {

    var fragment: LoginFragment? = null
    override var dataStore: LoginDataStore? = null

    // Routing

    override fun routeToSomewhere()
    {
        // Navigate to the destination fragment, passing data from the dataStore
    }
}
