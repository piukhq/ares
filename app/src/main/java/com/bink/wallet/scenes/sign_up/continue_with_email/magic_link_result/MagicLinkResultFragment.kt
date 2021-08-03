package com.bink.wallet.scenes.sign_up.continue_with_email.magic_link_result

import android.os.Bundle
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.MagicLinkResultFragmentBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class MagicLinkResultFragment : BaseFragment<MagicLinkResultViewModel, MagicLinkResultFragmentBinding>() {

    override val layoutRes = R.layout.magic_link_result_fragment

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: MagicLinkResultViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            val token = MagicLinkResultFragmentArgs.fromBundle(bundle).token
        }

    }

}