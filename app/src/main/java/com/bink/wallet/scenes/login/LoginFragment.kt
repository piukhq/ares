package com.bink.wallet.scenes.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bink.wallet.R
import com.bink.wallet.network.ApiService
import org.koin.android.ext.android.inject

interface LoginDisplayLogic {
    fun displaySomething(viewModel: LoginModels.Login.ViewModel)
}

class LoginFragment : Fragment(), LoginDisplayLogic {

    lateinit var interactor: LoginBusinessLogic
    lateinit var router: ILoginRouter

    private val apiService: ApiService by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setup()
//        Log.e("", viewModel.movie.toString())
//        viewModel = ViewModelProviders.of(this).get(MovieDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

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
        interactor.apiService = apiService
        presenter.fragment = fragment
        router.fragment = fragment
        router.dataStore = interactor
        router.navGraph = findNavController()
        router.routeToHome()

        doSomething()
    }

    // Routing

    // View lifecycle

    // Do something

    fun doSomething()
    {
        val request = LoginModels.Login.Request()
        interactor.doSomething(request)
    }

    override  fun displaySomething(viewModel: LoginModels.Login.ViewModel) {
    }
}
