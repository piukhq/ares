package com.bink.wallet.scenes.polls

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentPollsBinding
import com.bink.wallet.theme.AppTheme
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class PollsFragment : BaseFragment<PollsViewModel, FragmentPollsBinding>() {

    private val args by navArgs<PollsFragmentArgs>()

    override val layoutRes: Int
        get() = R.layout.fragment_polls
    override val viewModel by viewModel<PollsViewModel>()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder().build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.poll.value = args.poll

        binding.composeView.setContent {
            AppTheme(viewModel.theme.value) {
            }
        }
    }


}