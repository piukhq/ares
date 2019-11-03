package com.bink.wallet.scenes.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bink.wallet.R
import com.bink.wallet.databinding.OnboardingPageFragmentBinding
import com.bink.wallet.utils.PAGE_1
import com.bink.wallet.utils.toPixelFromDip

class OnboardingPageFragment : Fragment() {
    companion object {
        private const val EXTRA_PAGE_ID = "extraId"
        private const val EXTRA_IMAGE = "extraImage"
        private const val EXTRA_TITLE = "extraTitle"
        private const val EXTRA_DESCRIPTION = "extraDescription"
        private const val EXTRA_PAGE_TITLE = "extraPageTitle"
        private const val PAGE_IMAGE_HEIGHT_PX = 100f
        private const val PAGE_MARGIN_HEIGHT_PX = 16f

        fun newInstance(
            pageId: Int,
            pageTitle: String,
            imageId: Int,
            title: String,
            description: String
        ): OnboardingPageFragment {
            val fragment = OnboardingPageFragment()
            val bundle = Bundle()

            with(bundle) {
                putInt(EXTRA_PAGE_ID, pageId)
                putString(EXTRA_PAGE_TITLE, pageTitle)
                putInt(EXTRA_IMAGE, imageId)
                putString(EXTRA_TITLE, title)
                putString(EXTRA_DESCRIPTION, description)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    var pageId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<OnboardingPageFragmentBinding>(
            inflater,
            R.layout.onboarding_page_fragment,
            container,
            false
        )

        arguments?.let { bundle ->
            pageId = bundle.getInt(EXTRA_PAGE_ID)
            with(binding) {
                pageTitle.text = bundle.getString(EXTRA_TITLE)
                pageDescription.text = bundle.getString(EXTRA_DESCRIPTION)
                pageImage.setImageResource(bundle.getInt(EXTRA_IMAGE))
            }

            if (bundle.getString(EXTRA_PAGE_TITLE).equals(PAGE_1)) {
                binding.binkImage.visibility = View.VISIBLE
                val layoutParams = binding.pageImage.layoutParams
                layoutParams.height = requireContext().toPixelFromDip(PAGE_IMAGE_HEIGHT_PX).toInt()
                binding.pageImage.layoutParams = layoutParams

                val constraintSet = ConstraintSet()
                with(constraintSet) {
                    clone(binding.pageLayout)
                    connect(
                        R.id.page_image,
                        ConstraintSet.TOP,
                        R.id.bink_image,
                        ConstraintSet.BOTTOM,
                        requireContext().toPixelFromDip(PAGE_MARGIN_HEIGHT_PX).toInt()
                    )
                    applyTo(binding.pageLayout)
                }
            }
        }
        return binding.root
    }
}