package com.bink.wallet.scenes.add_auth_enrol

import android.view.View
import android.view.ViewTreeObserver
import com.bink.wallet.databinding.BaseAddAuthFragmentBinding
import com.bink.wallet.scenes.add_auth_enrol.screens.BaseAddAuthFragment
import com.bink.wallet.utils.RecyclerViewHelper

class AuthAnimationHelper(
    val fragment: BaseAddAuthFragment,
    val binding: BaseAddAuthFragmentBinding,
    private val recyclerViewHelper: RecyclerViewHelper
) {

    private lateinit var layoutListener: ViewTreeObserver.OnGlobalLayoutListener
    lateinit var footerLayoutListener: ViewTreeObserver.OnGlobalLayoutListener
    private val quotientFooterComposed = 3

    fun enableGlobalListeners(
        onEndTransition: () -> Unit = {},
        onStartTransition: () -> Unit = {}
    ) {
        layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            fragment.handleKeyboardHiddenListener(binding.layout, onEndTransition)
            fragment.handleKeyboardVisibleListener(binding.layout, onStartTransition)
        }
        footerLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            recyclerViewHelper.handleFooterFadeEffect(
                mutableListOf(binding.footerComposed.progressBtnContainer),
                binding.authFields,
                binding.footerComposed.footerBottomGradient,
                true,
                quotientFooterComposed
            )
            if (binding.footerComposed.root.visibility == View.VISIBLE) {
                binding.footerComposed.root.bringToFront()
            }
        }
        binding.layout.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        binding.layout.viewTreeObserver.addOnGlobalLayoutListener(footerLayoutListener)
    }

    fun disableGlobalListeners() {
        binding.layout.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        binding.layout.viewTreeObserver.removeOnGlobalLayoutListener(footerLayoutListener)
    }
}