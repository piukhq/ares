package com.bink.wallet.scenes.add_auth_enrol

import android.view.ViewTreeObserver
import com.bink.wallet.databinding.BaseAddAuthFragmentBinding
import com.bink.wallet.scenes.add_auth_enrol.screens.BaseAddAuthFragment

class AuthAnimationHelper(
    val fragment: BaseAddAuthFragment,
    val binding: BaseAddAuthFragmentBinding
) {

    private lateinit var layoutListener: ViewTreeObserver.OnGlobalLayoutListener
    lateinit var footerLayoutListener: ViewTreeObserver.OnGlobalLayoutListener

    fun enableGlobalListeners(
        onEndTransition: () -> Unit = {},
        onStartTransition: () -> Unit = {}
    ) {
        layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            fragment.handleKeyboardHiddenListener(binding.layout, onEndTransition)
            fragment.handleKeyboardVisibleListener(binding.layout, onStartTransition)
        }
        footerLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            fragment.handleFooterFadeEffect(
                mutableListOf(binding.footerSimple.addAuthCta),
                binding.authFields,
                binding.footerSimple.footerBottomGradient,
                true
            )
            fragment.handleFooterFadeEffect(
                mutableListOf(binding.footerComposed.noAccount, binding.footerComposed.addAuthCta),
                binding.authFields,
                binding.footerComposed.footerBottomGradient,
                true
            )
        }
        binding.layout.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        binding.layout.viewTreeObserver.addOnGlobalLayoutListener(footerLayoutListener)
    }

    fun disableGlobalListeners() {
        binding.layout.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        binding.layout.viewTreeObserver.removeOnGlobalLayoutListener(footerLayoutListener)
    }
}