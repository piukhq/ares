package com.bink.wallet.modal.generic

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.text.HtmlCompat
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.GenericModalFragmentBinding
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.FirebaseEvents.INFORMATION_MODAL_VIEW
import com.bink.wallet.utils.FirebaseEvents.getFirebaseIdentifier
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 */
open class GenericModalFragment :
    BaseFragment<BaseModalViewModel, GenericModalFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override fun onResume() {
        super.onResume()
        logScreenView(INFORMATION_MODAL_VIEW)
    }

    private val scrollChangeListener = ViewTreeObserver.OnScrollChangedListener {
        val scrollBounds = Rect()
        with(binding) {
            this?.screenScrollView?.getHitRect(scrollBounds)
            if (this?.title?.getLocalVisibleRect(scrollBounds) == true) {
                this?.titleToolbar?.text = EMPTY_STRING
            } else {
                this?.titleToolbar?.text = binding?.title?.text
            }
        }
    }

    override val viewModel: BaseModalViewModel by viewModel()
    override val layoutRes: Int get() = R.layout.generic_modal_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(binding) {
            this?.toolbar?.setNavigationOnClickListener {
                onNavigationButtonClicked()
            }
            this?.close?.setOnClickListener {
                onNavigationButtonClicked()
            }
            this?.firstButton?.setOnClickListener {
                onFirstButtonClicked()

                logEvent(
                    getFirebaseIdentifier(
                        INFORMATION_MODAL_VIEW,
                        firstButton.text.toString()
                    )
                )
            }
            this?.secondButton?.setOnClickListener {
                onSecondButtonClicked()

                logEvent(
                    getFirebaseIdentifier(
                        INFORMATION_MODAL_VIEW,
                        secondButton?.text.toString()
                    )
                )
            }
        }
        with(viewModel) {
            destinationLiveData.observeNonNull(this@GenericModalFragment) {
                goTo(it)
            }
            toolbarIconLiveData.observeNonNull(this@GenericModalFragment) {
                binding?.toolbar?.setNavigationIcon(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding?.title?.viewTreeObserver?.addOnScrollChangedListener(
            scrollChangeListener
        )
    }

    override fun onStop() {
        binding?.title?.viewTreeObserver?.removeOnScrollChangedListener(
            scrollChangeListener
        )
        super.onStop()
    }

    private fun goTo(destination: Int) {
        findNavController().navigateIfAdded(this, destination)
    }

    protected fun setupUi(parameters: GenericModalParameters) {
        with(binding) {
            if (!parameters.isCloseModal) {
                this?.close?.visibility = View.GONE
                if (parameters.topBarIconId != 0) {
                    this?.toolbar?.setNavigationIcon(parameters.topBarIconId)
                }
            }
            this?.title?.text = parameters.title

            if (parameters.description2.trim().isNotEmpty() && parameters.description.trim().isEmpty()){
                this?.description?.visibility = View.GONE
            }

            this?.description?.text =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(parameters.description, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    HtmlCompat.fromHtml(parameters.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                }
            this?.description?.movementMethod = LinkMovementMethod.getInstance()

            this?.descriptionSecondPart?.text =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(parameters.description2, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    HtmlCompat.fromHtml(parameters.description2, HtmlCompat.FROM_HTML_MODE_LEGACY)
                }
            this?.descriptionSecondPart?.movementMethod = LinkMovementMethod.getInstance()

            if (parameters.firstButtonText.isNotEmpty()) {
                this?.firstButton?.visibility = View.VISIBLE
                this?.firstButton?.text = parameters.firstButtonText
            } else {
                this?.firstButton?.visibility = View.GONE
            }
            if (parameters.secondButtonText.isNotEmpty()) {
                this?.secondButton?.visibility = View.VISIBLE
                this?.secondButton?.text = parameters.secondButtonText
            } else {
                this?.secondButton?.visibility = View.INVISIBLE
            }
        }
    }

    private fun onNavigationButtonClicked() {
        requireActivity().onBackPressed()
    }

    protected open fun onFirstButtonClicked() {}

    protected open fun onSecondButtonClicked() {}
}
