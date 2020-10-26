package com.bink.wallet.scenes.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BuildConfig
import com.bink.wallet.R
import com.bink.wallet.databinding.OnboardingPageFragmentBinding
import com.bink.wallet.utils.enums.BuildTypes
import com.bink.wallet.utils.navigateIfAdded
import java.util.*

class OnboardingPageFragment : Fragment() {

    companion object {
        private const val EXTRA_IMAGE = "extraImage"
        private const val EXTRA_TITLE = "extraTitle"
        private const val EXTRA_DESCRIPTION = "extraDescription"
        private const val EXTRA_PAGE_TITLE = "extraPageTitle"

        fun newInstance(
            pageTitle: String,
            imageId: Int,
            title: String,
            description: String
        ): OnboardingPageFragment {
            val fragment = OnboardingPageFragment()
            val bundle = Bundle()

            with(bundle) {
                putString(EXTRA_PAGE_TITLE, pageTitle)
                putInt(EXTRA_IMAGE, imageId)
                putString(EXTRA_TITLE, title)
                putString(EXTRA_DESCRIPTION, description)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    private var binding: OnboardingPageFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.onboarding_page_fragment,
            container,
            false
        )
        arguments?.let { bundle ->
            binding?.let {
                it.pageTitle.text = bundle.getString(EXTRA_TITLE)
                it.pageDescription.text = bundle.getString(EXTRA_DESCRIPTION)
                it.pageImage.setImageResource(bundle.getInt(EXTRA_IMAGE))
            }
        }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var clickCounter = 0
        binding?.pageImage?.setOnClickListener {
            clickCounter++
            if (clickCounter == 3) {
                it.setOnClickListener(null)
                openSettingsPage()
            }
        }
    }

    private fun openSettingsPage() {
        if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) != BuildTypes.RELEASE.type) {
            parentFragment?.let {
                findNavController().navigateIfAdded(
                    it,
                    OnboardingFragmentDirections.onboardingToDebug()
                )
            }
        }
    }
}