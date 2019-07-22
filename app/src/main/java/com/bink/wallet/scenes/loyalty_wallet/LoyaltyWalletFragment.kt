package com.bink.wallet.scenes.loyalty_wallet;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bink.wallet.R

interface LoyaltyWalletDisplayLogic {
    fun displaySomething(viewModel: LoyaltyWalletModels.Something.ViewModel)
}

class LoyaltyWalletFragment : Fragment(), LoyaltyWalletDisplayLogic {

    lateinit var interactor: LoyaltyWalletBusinessLogic
    lateinit var router: ILoyaltyWalletRouter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_loyalty_wallet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setup()
    }

    // Object lifecycle

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
