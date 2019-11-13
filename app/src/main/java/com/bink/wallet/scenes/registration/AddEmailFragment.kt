package com.bink.wallet.scenes.registration

import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddEmailFragmentBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddEmailFragment: BaseFragment<AddEmailViewModel, AddEmailFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.add_email_fragment
    override val viewModel: AddEmailViewModel by viewModel()

    override fun builder(): FragmentToolbar {
         return FragmentToolbar.Builder()
            .with(null)
            .build()
    }
}