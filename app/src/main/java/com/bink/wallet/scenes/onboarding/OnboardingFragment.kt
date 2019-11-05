package com.bink.wallet.scenes.onboarding

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.OnboardingFragmentBinding
import com.bink.wallet.scenes.onboarding.OnboardingPagerAdapter.Companion.FIRST_PAGE_INDEX
import com.bink.wallet.scenes.onboarding.OnboardingPagerAdapter.Companion.ONBOARDING_PAGES_NUMBER
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class OnboardingFragment : BaseFragment<OnboardingViewModel, OnboardingFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.onboarding_fragment
    override val viewModel: OnboardingViewModel by viewModel()
    var timer = Timer()
    val icons = ArrayList<ImageView>()
    val pageEntries = ArrayList<OnboardingPageFragment>()
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .build()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        pageEntries.add(
            OnboardingPageFragment.newInstance(
                1,
                PAGE_1,
                R.drawable.logo_page_1,
                getString(R.string.page_1_title) + " #1",
                getString(R.string.page_1_description)
            )
        )
        pageEntries.add(
            OnboardingPageFragment.newInstance(
                2,
                PAGE_2,
                R.drawable.onb_2,
                getString(R.string.page_2_title) + " #2",
                getString(R.string.page_2_description)
            )
        )
        pageEntries.add(
            OnboardingPageFragment.newInstance(
                3,
                PAGE_3,
                R.drawable.onb_3,
                getString(R.string.page_3_title) + " #3",
                getString(R.string.page_3_description)
            )
        )
        val adapter = fragmentManager?.let { OnboardingPagerAdapter(it) }
        with(adapter!!) {
            for (page in pageEntries) {
                addFragment(page)
            }
            binding.pager.adapter = this
            binding.pager.setCurrentItem(1, false)
        }

        with (binding.pagerIndicatorContainer) {
            for (i in 0 until pageEntries.size) {
                val icon = ImageView(context)
                with (icon) {
                    layoutParams = LinearLayout.LayoutParams(
                        resources.getDimension(R.dimen.margin_padding_size_quite_small).toInt(),
                        resources.getDimension(R.dimen.margin_padding_size_quite_small).toInt()).also {
                        it.marginEnd = resources.getDimension(R.dimen.margin_padding_size_really_small).toInt()
                        it.marginStart = resources.getDimension(R.dimen.margin_padding_size_really_small).toInt()
                    }
                    setImageResource(
                        if (i == 0)
                            R.drawable.ic_circle_dark
                        else
                            R.drawable.ic_circle_pale
                    )
                    tag = "icon_$i"
                }
                icons.add(icon)
                addView(icon)
            }
        }
        viewModel.pageId.observeNonNull(this) {pageId ->
            for (icon in icons) {
                icon.setImageResource(
                    if (icon.tag == "icon_$pageId")
                        R.drawable.ic_circle_dark
                    else
                        R.drawable.ic_circle_pale
                )
            }
        }

        binding.logInEmail.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.onboarding_to_home)
        }

        binding.continueWithFacebook.setOnClickListener {
            requireContext().displayModalPopup(
                getString(R.string.missing_destination_dialog_title),
                getString(R.string.not_implemented_yet_text)
            )
        }

        binding.signUpWithEmail.setOnClickListener {
            requireContext().displayModalPopup(
                getString(R.string.missing_destination_dialog_title),
                getString(R.string.not_implemented_yet_text)
            )
        }

        binding.pager.addOnPageChangeListener(object :
            OnboardingPagerAdapter.CircularViewPagerHandler(binding.pager, viewModel.pageId) {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                scrollPagesAutomatically(binding.pager)

                binding.back.visibility =
                    if (position == 0) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }

                timer.cancel()
                timer = Timer()
                scrollPagesAutomatically(binding.pager)
            }
        })

        binding.back.setOnClickListener {
            binding.pager.arrowScroll(View.FOCUS_LEFT)
        }

        scrollPagesAutomatically(binding.pager)
    }

    private fun scrollPagesAutomatically(pager: ViewPager) {
/* NOTE: Disabling the timer for the moment as it threw an error:
   Cannot setMaxLifecycle for Fragment not attached to FragmentManager

   That needs to be looked at, plus the dots below,
   but getting the infinite scroller was the first step
 */
//        var currentPage = pager.currentItem
//        val pagerHandler = Handler()
//        val update = Runnable {
//            pager.setCurrentItem(currentPage++, true)
//        }
//        timer.schedule(object : TimerTask() {
//            override fun run() {
//                pagerHandler.post(update)
//            }
//        }, 0, ONBOARDING_SCROLL_DURATION_SECONDS)
    }
}