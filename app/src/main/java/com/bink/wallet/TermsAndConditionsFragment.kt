package com.bink.wallet

import android.graphics.Rect
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.navigation.fragment.findNavController
import com.bink.wallet.databinding.TermsAndConditionsFragmentBinding
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.android.synthetic.main.terms_and_conditions_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 */
class TermsAndConditionsFragment :
    BaseFragment<TermsAndConditionsViewModel, TermsAndConditionsFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    private val scrollChangeListener = ViewTreeObserver.OnScrollChangedListener {
        val scrollBounds = Rect()
        screen_scroll_view.getHitRect(scrollBounds)
        if (terms_and_conditions_title.getLocalVisibleRect(scrollBounds)) {
            binding.toolbar.title = ""
        } else {
            binding.toolbar.title = getString(R.string.terms_and_conditions_title)
        }
    }

    override val viewModel: TermsAndConditionsViewModel by viewModel()
    override val layoutRes: Int get() = R.layout.terms_and_conditions_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.ic_close)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        terms_and_conditions_title.viewTreeObserver.addOnScrollChangedListener(scrollChangeListener)
        accept_button.setOnClickListener {

        }
        decline_button.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.terms_to_home)
        }
    }

    override fun onStop() {
        terms_and_conditions_title.viewTreeObserver.removeOnScrollChangedListener(
            scrollChangeListener
        )

        super.onStop()
    }
}
