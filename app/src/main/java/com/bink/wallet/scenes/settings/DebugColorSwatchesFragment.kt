package com.bink.wallet.scenes.settings

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentDebugColorSwatchesBinding
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class DebugColorSwatchesFragment :
    BaseFragment<DebugColorSwatchesViewModel, FragmentDebugColorSwatchesBinding>() {

    override val layoutRes: Int
        get() = R.layout.fragment_debug_color_swatches
    override val viewModel: DebugColorSwatchesViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.membershipPlans.observeNonNull(this) {
            val adapter = DebugColorAdapter()
            adapter.plans = it

            val layoutManager = LinearLayoutManager(requireContext())

            binding.rvColors.layoutManager = layoutManager
            binding.rvColors.adapter = adapter
        }
        viewModel.getLocalMembershipCards()
    }
}