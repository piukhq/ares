package com.bink.wallet.scenes.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bink.wallet.R
import com.bink.wallet.databinding.OnboardingPageFragmentBinding

class OnboardingPageFragment: Fragment() {

    companion object {
        private const val EXTRA_IMAGE = "extraImage"
        private const val EXTRA_TITLE = "extraTitle"
        private const val EXTRA_DESCRIPTION = "extraDescription"

        fun newInstance(imageId: Int, title: String, description: String): OnboardingPageFragment {
            val fragment = OnboardingPageFragment()
            val bundle = Bundle()
            with(bundle){
                putInt(EXTRA_IMAGE, imageId)
                putString(EXTRA_TITLE, title)
                putString(EXTRA_DESCRIPTION, description)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<OnboardingPageFragmentBinding>(inflater,R.layout.onboarding_page_fragment, container, false)
        arguments?.let { bundle ->
            binding.pageTitle.text = bundle.getString(EXTRA_TITLE)
            binding.pageDescription.text = bundle.getString(EXTRA_DESCRIPTION)
            binding.pageImage.setImageResource(bundle.getInt(EXTRA_IMAGE))
        }
        return binding.root
    }
}