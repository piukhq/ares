package com.bink.wallet.scenes.login

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.LoginFragmentBinding
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.verifyAvailableNetwork
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment<LoginViewModel, LoginFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.login_fragment
    override val viewModel: LoginViewModel by viewModel()

    private val loginData = MutableLiveData<LoginBody>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (verifyAvailableNetwork(activity!!)) {
            viewModel.authenticate()
        } else {
            showNoInternetConnectionDialog()
        }

        viewModel.loginData.observe(this, Observer {
            findNavController().navigateIfAdded(this, R.id.login_to_loyalty)
        })
    }

    private fun showNoInternetConnectionDialog() {
        android.app.AlertDialog.Builder(context)
            .setMessage(R.string.no_internet_connection_dialog_message)
            .setNeutralButton(R.string.ok) { _, _ -> }
            .create().show()
    }
}
