package com.bink.wallet.scenes.loyalty_details

import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentModuleIssueBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class ModuleIssueScreen: BaseFragment<ModuleIssueViewModel, FragmentModuleIssueBinding>() {
    override val layoutRes: Int
        get() = R.layout.fragment_module_issue
    override val viewModel: ModuleIssueViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }
}