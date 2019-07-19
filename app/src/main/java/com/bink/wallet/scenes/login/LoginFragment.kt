package com.bink.wallet.scenes.login

import androidx.fragment.app.Fragment

interface LoginDisplayLogic {
    fun displaySomething(viewModel: Login.Something.ViewModel)
}

class LoginFragment : Fragment(), LoginDisplayLogic {

    lateinit var interactor: LoginBusinessLogic
    lateinit var router: ILoginRouter

    init {
        setup()
    }

    // Object lifecycle

    override fun onStart() {
        super.onStart()
        // Called when the Fragment is visible to the user.
    }

    override fun onStop() {
        super.onStop()
        // Called when the Fragment is no longer started.
    }

    override fun onPause() {
        super.onPause()
        // Called when the Fragment is no longer resumed.
    }

    override fun onResume() {
        super.onResume()
        // Called when the fragment is visible to the user and actively running.
    }

    // Setup

    private fun setup()
    {
        // Setup the interactor, presenter, router and wire everything together

        val fragment = this
        val interactor = LoginInteractor()
        val presenter = LoginPresenter()
        val router = LoginRouter()
        fragment.interactor = interactor
        fragment.router = router
        interactor.presenter = presenter
        presenter.fragment = fragment
        router.fragment = fragment
        router.dataStore = interactor
    }

    // Routing

    // View lifecycle

    // Do something

    fun doSomething()
    {
        val request = Login.Something.Request()
        interactor?.doSomething(request)
    }

    override  fun displaySomething(viewModel: Login.Something.ViewModel) {
    }
}
