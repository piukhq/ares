package com.bink.wallet.modal.generic

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewTreeObserver
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.GenericModalFragmentBinding
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.FirebaseUtils.INFORMATION_MODAL_VIEW
import com.bink.wallet.utils.FirebaseUtils.getFirebaseIdentifier
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logScreenView(INFORMATION_MODAL_VIEW, this)
    }

    private val scrollChangeListener = ViewTreeObserver.OnScrollChangedListener {
        val scrollBounds = Rect()
        with(binding) {
            screenScrollView.getHitRect(scrollBounds)
            if (title.getLocalVisibleRect(scrollBounds)) {
                titleToolbar.text = EMPTY_STRING
            } else {
                titleToolbar.text = binding.title.text
            }
        }
    }

    override val viewModel: BaseModalViewModel by viewModel()
    override val layoutRes: Int get() = R.layout.generic_modal_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                onNavigationButtonClicked()
            }
            close.setOnClickListener {
                onNavigationButtonClicked()
            }
            firstButton.setOnClickListener {
                onFirstButtonClicked()

                logEvent(
                    getFirebaseIdentifier(
                        INFORMATION_MODAL_VIEW,
                        firstButton.text.toString()
                    )
                )
            }
            secondButton.setOnClickListener {
                onSecondButtonClicked()

                logEvent(
                    getFirebaseIdentifier(
                        INFORMATION_MODAL_VIEW,
                        secondButton.text.toString()
                    )
                )
            }
        }
        with(viewModel) {
            destinationLiveData.observeNonNull(this@GenericModalFragment) {
                goTo(it)
            }
            toolbarIconLiveData.observeNonNull(this@GenericModalFragment) {
                binding.toolbar.setNavigationIcon(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.title.viewTreeObserver.addOnScrollChangedListener(
            scrollChangeListener
        )
    }

    override fun onStop() {
        binding.title.viewTreeObserver.removeOnScrollChangedListener(
            scrollChangeListener
        )
        super.onStop()
    }

    private fun goTo(destination: Int) {
        findNavController().navigateIfAdded(this, destination)
    }

    @Suppress("DEPRECATION")
    protected fun setupUi(parameters: GenericModalParameters) {
        with(binding) {
            if (!parameters.isCloseModal) {
                close.visibility = View.GONE
                if (parameters.topBarIconId != 0) {
                    toolbar.setNavigationIcon(parameters.topBarIconId)
                }
            }
            title.text = parameters.title
            description.text =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(parameters.description, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    Html.fromHtml(parameters.description)
                }
            description.movementMethod = LinkMovementMethod.getInstance()

            descriptionSecondPart.text =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(parameters.description2, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    Html.fromHtml(parameters.description2)
                }
            descriptionSecondPart.movementMethod = LinkMovementMethod.getInstance()

            if (parameters.firstButtonText.isNotEmpty()) {
                firstButton.visibility = View.VISIBLE
                firstButton.text = parameters.firstButtonText
            } else {
                firstButton.visibility = View.GONE
            }
            if (parameters.secondButtonText.isNotEmpty()) {
                secondButton.visibility = View.VISIBLE
                secondButton.text = parameters.secondButtonText
            } else {
                secondButton.visibility = View.GONE
            }
        }
    }

    protected fun onNavigationButtonClicked() {
        requireActivity().onBackPressed()
    }

    protected open fun onFirstButtonClicked() {}

    protected open fun onSecondButtonClicked() {}
}
