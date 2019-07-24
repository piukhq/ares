package com.bink.wallet.scenes.login

import com.bink.wallet.network.ApiService


interface LoginBusinessLogic {
    fun doSomething(request: LoginModels.Login.Request)
}

interface LoginDataStore {
//    var name: String
//    var address: String
}

class LoginInteractor(apiService: ApiService) : LoginBusinessLogic, LoginDataStore {
    var presenter: LoginPresentationLogic? = null
    var worker: LoginWorker? = null
    var apiService: ApiService? = null

//    override var name: String = ""
//    override var address: String = ""

    // Do something

    override fun doSomething(request: LoginModels.Login.Request) {

        worker = LoginWorker(apiService!!, this)

        worker?.doAuthenticationWork(
            LoginBody(
                1517549941,
                "launchpad@bink.com"
            )
        )
//
//        val response = LoginModels.Login.Response()
//        presenter?.presentSomething(response)
    }

    fun successfulResponse(loginBody: LoginBody) {
//        TODO "Do something with the response"
    }

    fun showErrorMessage(errorMessage: String) {
//        TODO "Show error message"
    }
}
