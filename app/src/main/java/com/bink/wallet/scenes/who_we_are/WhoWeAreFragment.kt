package com.bink.wallet.scenes.who_we_are

import android.os.Bundle
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.WhoWeAreFragmentBinding
import com.bink.wallet.utils.FirebaseEvents
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class WhoWeAreFragment : BaseFragment<WhoWeAreViewModel, WhoWeAreFragmentBinding>() {

    override val viewModel: WhoWeAreViewModel by viewModel()

    override val layoutRes = R.layout.who_we_are_fragment

    override fun onResume() {
        super.onResume()
        logScreenView(FirebaseEvents.WHO_ARE_WE)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.nameList.adapter = WhoWeAreAdapter(ArrayList<String>().also {
            it.add("Paul Batty")
            it.add("Enoch Hankombo")
            it.add("Joshua Best")
            it.add("Susanne King")
            it.add("Srikalyani Kotha")
            it.add("Marius Lobontiu")
            it.add("Connor McFadden")
            it.add("Carmen Muntean")
            it.add("Teodora Popescu")
            it.add("Mara Savan")
            it.add("Karl Sigiscar")
            it.add("Max Woodhams")
        })
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }
}