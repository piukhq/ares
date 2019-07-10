package com.bink.wallet.scenes.login

interface LoginPresentationLogic
{
    fun presentSomething(response: Login.Something.Response)
}

class LoginPresenter : LoginPresentationLogic {
    var activity: LoginDisplayLogic? = null

    // Do something

    override fun presentSomething(response: Login.Something.Response)
    {
        val viewModel = Login.Something.ViewModel()
        activity?.displaySomething(viewModel)
    }
}
