package com.bink.wallet.scenes.onboarding

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.OnboardingFragmentBinding
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingFragment : BaseFragment<OnboardingViewModel, OnboardingFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.onboarding_fragment
    override val viewModel: OnboardingViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = fragmentManager?.let { OnboardingPagerAdapter(it) }
        adapter?.addFragment(OnboardingPageFragment.newInstance(0, getString(R.string.page_1_title),getString(R.string.page_1_description)))
        adapter?.addFragment(OnboardingPageFragment.newInstance(0, getString(R.string.page_2_title),getString(R.string.page_2_description)))
        adapter?.addFragment(OnboardingPageFragment.newInstance(0, getString(R.string.page_3_title),getString(R.string.page_3_description)))
        binding.pager.adapter = adapter

        binding.logInEmail.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.onboarding_to_home)
        }
    }
}