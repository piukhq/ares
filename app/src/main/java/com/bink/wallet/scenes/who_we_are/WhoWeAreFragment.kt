package com.bink.wallet.scenes.who_we_are

import android.os.Bundle
import android.view.View
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.WhoWeAreFragmentBinding
import com.bink.wallet.utils.FirebaseEvents
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class WhoWeAreFragment : BaseFragment<WhoWeAreViewModel, WhoWeAreFragmentBinding>() {

    override val viewModel: WhoWeAreViewModel by viewModel()

    override val layoutRes = R.layout.who_we_are_fragment

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.nameList.observeNonNull(this) { nameList ->
            binding.nameList.adapter = WhoWeAreAdapter(nameList)
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.populateNames(resources)
        logScreenView(FirebaseEvents.WHO_ARE_WE)
    }


}