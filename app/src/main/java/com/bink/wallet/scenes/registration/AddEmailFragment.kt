package com.bink.wallet.scenes.registration

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddEmailFragmentBinding
import com.bink.wallet.utils.SimplifiedTextWatcher
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import io.fabric.sdk.android.services.common.CommonUtils.hideKeyboard
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddEmailFragment : BaseFragment<AddEmailViewModel, AddEmailFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.add_email_fragment
    override val viewModel: AddEmailViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .build()
    }

    private var accessToken: AccessToken? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            this@AddEmailFragment.accessToken = AddEmailFragmentArgs.fromBundle(it).accessToken
        }

        binding.email.addTextChangedListener(object : SimplifiedTextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    val mailIsValid = Patterns.EMAIL_ADDRESS.matcher(it).matches()
                    if (!mailIsValid) {
                        binding.email.error = getString(R.string.invalid_email_format)
                    }
                    binding.continueButton.isEnabled = mailIsValid
                }
            }

        })

        binding.back.setOnClickListener {
            LoginManager.getInstance().logOut()
            hideKeyboard(requireContext(), binding.root)
            findNavController().navigateIfAdded(this, R.id.add_email_to_onboarding)
        }


        binding.continueButton.setOnClickListener {
            if (UtilFunctions.isNetworkAvailable(requireContext(), true)) {
                findNavController().navigateIfAdded(
                    this,
                    AddEmailFragmentDirections.addEmailToAcceptTerms(
                        accessToken,
                        binding.email.text.toString()
                    )
                )
            }
        }

    }
}