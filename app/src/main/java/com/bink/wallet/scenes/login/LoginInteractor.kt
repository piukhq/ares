package com.bink.wallet.scenes.login


interface LoginBusinessLogic
{
    fun doSomething(request: Login.Something.Request)
}

interface LoginDataStore
{
    //var name: String
}

class LoginInteractor: LoginBusinessLogic, LoginDataStore {
    var presenter: LoginPresentationLogic? = null
    var worker: LoginWorker? = null
    //var name: String = ""

    // MARK: Do something

    override fun doSomething(request: Login.Something.Request)
    {
        worker = LoginWorker()
        worker?.doSomeWork()

        val response = Login.Something.Response()
        presenter?.presentSomething(response)
    }
}
