package com.bink.wallet

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bink.wallet.utils.FirebaseEvents.ANALYTICS_CALL_TO_ACTION_TYPE
import com.bink.wallet.utils.FirebaseEvents.ANALYTICS_IDENTIFIER
import com.bink.wallet.utils.KEYBOARD_TO_SCREEN_HEIGHT_RATIO
import com.bink.wallet.utils.WindowFullscreenHandler
import com.bink.wallet.utils.enums.BuildTypes
import com.bink.wallet.utils.hideKeyboard
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.bink.wallet.utils.toolbar.ToolbarManager
import com.crashlytics.android.Crashlytics
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.util.*

abstract class BaseFragment<VM : BaseViewModel, DB : ViewDataBinding> : Fragment() {

    @get:LayoutRes
    abstract val layoutRes: Int

    abstract val viewModel: VM

    open lateinit var binding: DB

    open val windowFullscreenHandler: WindowFullscreenHandler by inject {
        parametersOf(
            requireActivity()
        )
    }

    private lateinit var keyboardHiddenListener: ViewTreeObserver.OnGlobalLayoutListener

    open fun init(inflater: LayoutInflater, container: ViewGroup) {
        binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)
    }

    open fun init() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        container?.let {
            init(inflater, container)
        }
        init()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (findNavController().currentDestination?.label != getString(R.string.root)) {
                        view?.hideKeyboard()
                        windowFullscreenHandler.toNormalScreen()
                        findNavController().popBackStack()
                    } else {
                        requireActivity().finish()
                    }
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolbarManager(builder()).prepareToolbar()
    }

    protected abstract fun builder(): FragmentToolbar

    protected fun setCrashlyticsUserId(uid: String) {
        Crashlytics.setUserIdentifier(uid)
    }

    protected fun setFirebaseAnalyticsUserId(uid: String){
        (requireActivity() as MainActivity).firebaseAnalytics.setUserId(uid)
    }

    protected fun logEvent(identifierValue: String) {
        logFirebaseEvent(identifierValue)
    }

    protected fun logEvent(name: String, parameters: Map<String, String>) {
        if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) == BuildTypes.RELEASE.type) {
            val bundle = Bundle()

            for (entry: Map.Entry<String, String> in parameters) {
                bundle.putString(entry.key, entry.value)
            }

            (requireActivity() as MainActivity).firebaseAnalytics.logEvent(name, bundle)
        }
    }

    protected fun logScreenView(screenName: String) {
        if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) == BuildTypes.RELEASE.type) {
            (requireActivity() as MainActivity).firebaseAnalytics.setCurrentScreen(
                requireActivity(),
                screenName,
                screenName
            )
        }
    }

    private fun logFirebaseEvent(identifierValue: String) {
        if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) == BuildTypes.RELEASE.type) {
            val bundle = Bundle()
            bundle.putString(ANALYTICS_IDENTIFIER, identifierValue)
            (requireActivity() as MainActivity).firebaseAnalytics.logEvent(
                ANALYTICS_CALL_TO_ACTION_TYPE,
                bundle
            )
        }
    }

    fun setupKeyboardHiddenListener(container: View, onLayoutChange: (() -> Unit)) {
        this.keyboardHiddenListener = ViewTreeObserver.OnGlobalLayoutListener {
            handleKeyboardHiddenListener(container, onLayoutChange)
        }
    }

    fun handleKeyboardHiddenListener(container: View, onLayoutChange: (() -> Unit)) {
        val rec = Rect()
        container.getWindowVisibleDisplayFrame(rec)
        val screenHeight = container.rootView.height
        val keypadHeight = screenHeight - rec.bottom
        if (keypadHeight <= screenHeight * KEYBOARD_TO_SCREEN_HEIGHT_RATIO) {
            onLayoutChange()
        }
    }

    fun handleKeyboardVisibleListener(container: View, onLayoutChange: (() -> Unit)) {
        val rec = Rect()
        container.getWindowVisibleDisplayFrame(rec)
        val screenHeight = container.rootView.height
        val keypadHeight = screenHeight - rec.bottom
        if (keypadHeight > screenHeight * KEYBOARD_TO_SCREEN_HEIGHT_RATIO) {
            onLayoutChange()
        }
    }

    fun registerKeyboardHiddenLayoutListener(container: View) {
        container.viewTreeObserver.addOnGlobalLayoutListener(keyboardHiddenListener)
    }

    fun removeKeyboardHiddenLayoutListener(container: View) {
        container.viewTreeObserver.removeOnGlobalLayoutListener(keyboardHiddenListener)
    }
}