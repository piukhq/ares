package com.bink.wallet.scenes.loyalty_wallet;

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

interface LoyaltyWalletDisplayLogic {
    fun displaySomething(viewModel: LoyaltyWalletModels.Something.ViewModel)
}

class LoyaltyWalletFragment : Fragment(), LoyaltyWalletDisplayLogic {

    lateinit var interactor: LoyaltyWalletBusinessLogic
    lateinit var router: ILoyaltyWalletRouter

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

    private fun setup() {
        // Setup the interactor, presenter, router and wire everything together

        val navController = findNavController()
        val fragment = this
        val interactor = LoyaltyWalletInteractor()
        val presenter = LoyaltyWalletPresenter()
        val router = LoyaltyWalletRouter()
        fragment.interactor = interactor
        fragment.router = router
        interactor.presenter = presenter
        presenter.fragment = fragment
        router.fragment = fragment
        router.dataStore = interactor
        router.navController = navController
    }

    // Do something

    fun doSomething() {
        val request = LoyaltyWalletModels.Something.Request()
        interactor?.doSomething(request)
    }

    override fun displaySomething(viewModel: LoyaltyWalletModels.Something.ViewModel) {

    }
}
