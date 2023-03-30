package com.bink.wallet.scenes.whats_new

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.WhatsNewFragmentBinding
import com.bink.wallet.theme.AppTheme
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class WhatsNewFragment :
    BaseFragment<WhatsNewViewModel, WhatsNewFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: WhatsNewViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.whats_new_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            viewModel.whatsNew.value = WhatsNewFragmentArgs.fromBundle(it).whatsNew
        }

        binding.composeView.setContent {
            AppTheme(viewModel.theme.value) {
                WhatsNew()
            }
        }
    }

    @Composable
    private fun WhatsNew() {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.margin_padding_size_medium))) {

        }
    }


}