package com.bink.wallet.scenes.registration

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddEmailFragmentBinding
import com.bink.wallet.utils.ImprovedTextWatcher
import com.bink.wallet.utils.emailRegex
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.facebook.AccessToken
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.regex.Pattern.matches

class AddEmailFragment : BaseFragment<AddEmailViewModel, AddEmailFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.add_email_fragment
    override val viewModel: AddEmailViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(null)
            .build()
    }

    private var accessToken: AccessToken? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            this@AddEmailFragment.accessToken = AddEmailFragmentArgs.fromBundle(it).accessToken
        }

        binding.email.addTextChangedListener(object: ImprovedTextWatcher{
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                 s?.let {
                    binding.continueButton.isEnabled = matches(emailRegex, it)
                }
            }

        })


        binding.continueButton.setOnClickListener {
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