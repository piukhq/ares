package com.bink.wallet.scenes.login

interface LoginPresentationLogic
{
    fun presentSomething(response: LoginModels.Login.Response)
}

class LoginPresenter : LoginPresentationLogic {
    var fragment: LoginDisplayLogic? = null

    override fun presentSomething(response: LoginModels.Login.Response)
    {
        val viewModel = LoginModels.Login.ViewModel()
        fragment?.displaySomething(viewModel)
    }
}
