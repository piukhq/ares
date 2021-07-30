package com.bink.wallet.scenes.sign_up.continue_with_email

import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.ContinueWithEmailFragmentBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContinueWithEmailFragment: BaseFragment<ContinueWithEmailViewModel, ContinueWithEmailFragmentBinding>() {

    override val layoutRes = R.layout.continue_with_email_fragment

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: ContinueWithEmailViewModel by viewModel()

}