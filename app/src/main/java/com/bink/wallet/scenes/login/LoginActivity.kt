package com.bink.wallet.scenes.login

import android.app.Activity
import android.os.Bundle

interface LoginDisplayLogic {
    fun displaySomething(viewModel: Login.Something.ViewModel)
}

class LoginActivity : Activity(), LoginDisplayLogic {

    lateinit var interactor: LoginBusinessLogic
    lateinit var router: ILoginRouter

    init {
        setup()
    }

    // Object lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        // call the super class onCreate to complete the creation of activity like
        // the view hierarchy
        super.onCreate(savedInstanceState)

    }

    override fun onDestroy() {
        super.onDestroy()
        // The onDestroy() callback should release all resources that have not yet been released by earlier callbacks such as onStop().
    }

    override fun onStart() {
        super.onStart()
        // The onStart() call makes the activity visible to the user, as the app prepares for the activity to enter the foreground and become interactive.
    }

    override fun onStop() {
        super.onStop()
        // Called when the activity is no longer visible to the user.
    }

    override fun onPause() {
        super.onPause()
        // Called when the activity loses foreground state, is no longer focusable or before transition to stopped/hidden or destroyed state.
    }

    override fun onResume() {
        super.onResume()
        // Called when the activity will start interacting with the user.
    }

    // Setup

    private fun setup()
    {
        // Setup the interactor, presenter, router and wire everything together

        val activity = this
        val interactor = LoginInteractor()
        val presenter = LoginPresenter()
        val router = LoginRouter()
        activity.interactor = interactor
        activity.router = router
        interactor.presenter = presenter
        presenter.activity = activity
        router.activity = activity
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
