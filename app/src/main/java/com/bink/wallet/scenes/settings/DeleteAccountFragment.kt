package com.bink.wallet.scenes.settings

import android.os.Bundle
import android.view.View
import android.widget.Toolbar
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.DeleteAccountFragmentBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeleteAccountFragment : BaseFragment<DeleteAccountViewModel, DeleteAccountFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(Toolbar(requireContext()))
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.delete_account_fragment

    override val viewModel: DeleteAccountViewModel by viewModel()

    private val nunitoSans = FontFamily(
        Font(R.font.nunito_sans, FontWeight.Normal),
        Font(R.font.nunito_sans_extrabold, FontWeight.ExtraBold),
        Font(R.font.nunito_sans_light, FontWeight.Light)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.setContent {
            Surface(color = MaterialTheme.colors.background) {

            }
        }


    }
}