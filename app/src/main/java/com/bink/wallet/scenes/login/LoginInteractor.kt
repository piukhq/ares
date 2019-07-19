package com.bink.wallet.scenes.login

import com.bink.wallet.network.ApiService


interface LoginBusinessLogic
{
    fun doSomething(request: LoginModels.Login.Request)
}

interface LoginDataStore
{
//    var name: String
//    var address: String
}

class LoginInteractor: LoginBusinessLogic, LoginDataStore {
    var presenter: LoginPresentationLogic? = null
    var worker: LoginWorker? = null
    var apiService: ApiService? = null

//    override var name: String = ""
//    override var address: String = ""

    // Do something

    override fun doSomething(request: LoginModels.Login.Request)
    {

        worker = LoginWorker()

        worker?.apiService = apiService

        worker?.doAuthenticationWork()
//
//        val response = LoginModels.Login.Response()
//        presenter?.presentSomething(response)
    }
}
