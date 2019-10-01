package com.bink.wallet.modal.generic

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.GenericModalFragmentBinding
import com.bink.wallet.utils.navigateIfAdded
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

    private val scrollChangeListener = ViewTreeObserver.OnScrollChangedListener {
        val scrollBounds = Rect()
        binding.screenScrollView.getHitRect(scrollBounds)
        if (binding.title.getLocalVisibleRect(scrollBounds)) {
            binding.toolbar.title = ""
        } else {
            binding.toolbar.title = binding.title.text
        }
    }

    override val viewModel: BaseModalViewModel by viewModel()
    override val layoutRes: Int get() = R.layout.generic_modal_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            onNavigationButtonClicked()
        }
        binding.firstButton.setOnClickListener {
            onFirstButtonClicked()
        }
        binding.secondButton.setOnClickListener {
            onSecondButtonClicked()
        }

        if (viewModel.destinationLiveData.hasActiveObservers()) {
            viewModel.destinationLiveData.removeObservers(this)
        } else {
            viewModel.destinationLiveData.observe(this, Observer {
                goTo(it)
            })
        }
        if (viewModel.toolbarIconLiveData.hasActiveObservers()) {
            viewModel.toolbarIconLiveData.removeObservers(this)
        } else {
            viewModel.toolbarIconLiveData.observe(this, Observer {
                binding.toolbar.setNavigationIcon(it)
            })
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

    protected fun setupUi(parameters: GenericModalParameters) {
        binding.toolbar.setNavigationIcon(parameters.topBarIconId)
        binding.title.text = parameters.title
        binding.description.text = parameters.description
        if (parameters.firstButtonText.isNotEmpty()) {
            binding.firstButton.visibility = View.VISIBLE
            binding.firstButton.text = parameters.firstButtonText
        } else {
            binding.firstButton.visibility = View.GONE
        }
        if (parameters.secondButtonText.isNotEmpty()) {
            binding.secondButton.visibility = View.VISIBLE
            binding.secondButton.text = parameters.secondButtonText
        } else {
            binding.secondButton.visibility = View.GONE
        }
    }

    protected fun onNavigationButtonClicked() {
        activity?.onBackPressed()
    }

    protected open fun onFirstButtonClicked() {
        viewModel.onFirstButtonClicked()
    }

    protected fun onSecondButtonClicked() {
        viewModel.onSecondButtonClicked()
    }
}
