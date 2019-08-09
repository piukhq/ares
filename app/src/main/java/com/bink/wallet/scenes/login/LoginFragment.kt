package com.bink.wallet.scenes.login

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.LoginFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment<LoginViewModel, LoginFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.login_fragment
    override val viewModel: LoginViewModel by viewModel()

    private val loginData = MutableLiveData<LoginBody>()
    private val viewModel: LoginViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.loginData.observe(this, Observer {
            findNavController().navigate(R.id.login_to_loyalty)
        })
    }
}
