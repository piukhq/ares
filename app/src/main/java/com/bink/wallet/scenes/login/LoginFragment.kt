package com.bink.wallet.scenes.login

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.LoginFragmentBinding
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.bink.wallet.utils.verifyAvailableNetwork
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment<LoginViewModel, LoginFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.login_fragment
    override val viewModel: LoginViewModel by viewModel()

    private val loginData = MutableLiveData<LoginBody>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.retrieveStoredLoginData()
        viewModel.loginData.observe(this, Observer {
            if (verifyAvailableNetwork(activity!!)) {
                viewModel.authenticate()
            } else {
                showNoInternetConnectionDialog()
            }
        })

        viewModel.loginBody.observe(this, Observer {
            findNavController().navigateIfAdded(this, R.id.login_to_loyalty)
        })
    }
}
