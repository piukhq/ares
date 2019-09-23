package com.bink.wallet

import android.graphics.Rect
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.navigation.fragment.findNavController
import com.bink.wallet.databinding.TermsAndConditionsFragmentBinding
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
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
        binding.screenScrollView.getHitRect(scrollBounds)
        if (binding.termsAndConditionsTitle.getLocalVisibleRect(scrollBounds)) {
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
        binding.acceptButton.setOnClickListener {
            //TODO: Create the payment card when structure and API calls are ready.
        }
        binding.declineButton.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.global_to_home)
        }
    }

    override fun onStart() {
        super.onStart()

        binding.termsAndConditionsTitle.viewTreeObserver.addOnScrollChangedListener(
            scrollChangeListener
        )
    }

    override fun onStop() {
        binding.termsAndConditionsTitle.viewTreeObserver.removeOnScrollChangedListener(
            scrollChangeListener
        )

        super.onStop()
    }
}
