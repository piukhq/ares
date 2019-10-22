package com.bink.wallet.scenes.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bink.wallet.R

class OnboardingPageFragment: Fragment() {

    companion object {
        fun newInstance(): OnboardingPageFragment {
            val fragment = OnboardingPageFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view: View? = inflater.inflate(R.layout.onboarding_page_fragment, container, false)
        return view
    }
}