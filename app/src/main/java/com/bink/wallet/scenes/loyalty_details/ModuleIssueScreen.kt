package com.bink.wallet.scenes.loyalty_details

import android.os.Bundle
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentModuleIssueBinding
import com.bink.wallet.utils.enums.LinkStatus
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class ModuleIssueScreen: BaseFragment<ModuleIssueViewModel, FragmentModuleIssueBinding>() {
    override val layoutRes: Int
        get() = R.layout.fragment_module_issue
    override val viewModel: ModuleIssueViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        arguments?.let { bundle ->
            ModuleIssueScreenArgs.fromBundle(bundle).linkStatus.apply {
                when(this){
                    LinkStatus.STATUS_LINKABLE_GENERIC_ERROR -> {
                        binding.title.text = getString(R.string.title_2_4)
                        viewModel.firstTextLine.set(getString(R.string.text_2_4_1, LinkStatus.STATUS_LINKABLE_GENERIC_ERROR.name))
                        viewModel.secondTextLine.set(
                            getString(
                                R.string.text_2_4_2,
                                ModuleIssueScreenArgs.fromBundle(bundle).errorCodes
                            )
                        )
                    }
                    LinkStatus.STATUS_UNLINKABLE -> {
                        binding.title.text = getString(R.string.title_2_8)
                        viewModel.firstTextLine.set(getString(R.string.text_2_8_1))
                        viewModel.secondTextLine.set(getString(R.string.text_2_8_2))
                    }
                    else -> {}
                }
            }
        }

    }
}