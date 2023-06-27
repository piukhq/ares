package com.bink.wallet.scenes.prev_updates

import android.os.Bundle
import android.view.View
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.PrevUpdatesFragmentBinding
import com.bink.wallet.theme.AppTheme
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class PrevUpdatesFragment : BaseFragment<PrevUpdatesViewModel, PrevUpdatesFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: PrevUpdatesViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.prev_updates_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.setContent {
            AppTheme(viewModel.theme.value) {
            }
        }
    }
}