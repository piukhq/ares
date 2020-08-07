package com.bink.wallet

import android.graphics.Rect
import android.os.Bundle
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
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_JOURNEY_KEY
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_LOYALTY_PLAN_KEY
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_LOYALTY_REASON_CODE_KEY
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_LOYALTY_STATUS_KEY
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_SCANNED_CARD_KEY
import com.bink.wallet.utils.FirebaseEvents.ADD_PAYMENT_CARD_PAYMENT_STATUS_NEW_KEY
import com.bink.wallet.utils.FirebaseEvents.ANALYTICS_CALL_TO_ACTION_TYPE
import com.bink.wallet.utils.FirebaseEvents.ANALYTICS_IDENTIFIER
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_ACCOUNT_IS_NEW_KEY
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_CLIENT_ACCOUNT_ID
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_CLIENT_ACCOUNT_ID_KEY
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_PAYMENT_SCHEME_KEY
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
import kotlin.collections.HashMap

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

    protected fun setFirebaseUserId(uid: String) {
        Crashlytics.setUserIdentifier(uid)
    }

    protected fun logEvent(identifierValue: String) {
        logFirebaseEvent(identifierValue)
    }

    protected fun logEvent(name: String, parameters: Map<String, String>) {
        val bundle = Bundle()

        for (entry: Map.Entry<String, String> in parameters) {
            bundle.putString(entry.key, entry.value)
        }

        (requireActivity() as MainActivity).firebaseAnalytics.logEvent(name, bundle)
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

    //This will handle both request and response failure
    protected fun getAddPaymentCardGenericMap(paymentSchemeValue: String): Map<String, String> {
        val map = HashMap<String, String>()
        map[FIREBASE_PAYMENT_SCHEME_KEY] = getPaymentSchemeType(paymentSchemeValue).toString()
        map[FIREBASE_CLIENT_ACCOUNT_ID_KEY] =
            SharedPreferenceManager.addPaymentCardRequestUuid.toString()
        return map
    }

    protected fun getAddPaymentCardResponseSuccessMap(
        paymentSchemeValue: String,
        isAccountNew: String,
        paymentStatus: String
    ): Map<String, String> {
        val map = HashMap<String, String>()

        map[FIREBASE_PAYMENT_SCHEME_KEY] = getPaymentSchemeType(paymentSchemeValue).toString()
        map[FIREBASE_CLIENT_ACCOUNT_ID_KEY] =

        SharedPreferenceManager.addPaymentCardRequestUuid.toString()
        map[FIREBASE_ACCOUNT_IS_NEW_KEY] = isAccountNew
        map[ADD_PAYMENT_CARD_PAYMENT_STATUS_NEW_KEY] = paymentStatus

        return map
    }

    private fun getPaymentSchemeType(paymentScheme: String): Int {
        return when (paymentScheme) {
            "Visa" -> 0
            "MasterCard" -> 1
            //amex
            else -> 2
        }
    }

    protected fun getAddLoyaltyCardRequestMap(
        journeyValue: String,
        membershipPlanId: String,
        isAScannedCard: String
    ): Map<String, String> {
        val map = HashMap<String, String>()
        map[ADD_LOYALTY_CARD_JOURNEY_KEY] = journeyValue
        map[FIREBASE_CLIENT_ACCOUNT_ID_KEY] =
            SharedPreferenceManager.addLoyaltyCardRequestUuid.toString()
        map[ADD_LOYALTY_CARD_LOYALTY_PLAN_KEY] = membershipPlanId
        map[ADD_LOYALTY_CARD_SCANNED_CARD_KEY] = isAScannedCard

        return map

    }

    protected fun getAddLoyaltyResponseSuccessMap(
        journeyValue: String,
        loyaltyStatus: String,
        reasonCode: String,
        membershipPlanId: String,
        isAccountNew: String
    ): Map<String, String> {
        val map = HashMap<String, String>()
        map[ADD_LOYALTY_CARD_JOURNEY_KEY] = journeyValue
        map[FIREBASE_CLIENT_ACCOUNT_ID_KEY] =
            SharedPreferenceManager.addLoyaltyCardRequestUuid.toString()
        map[FIREBASE_ACCOUNT_IS_NEW_KEY] = isAccountNew
        map[ADD_LOYALTY_CARD_LOYALTY_STATUS_KEY] = loyaltyStatus
        map[ADD_LOYALTY_CARD_LOYALTY_REASON_CODE_KEY] = reasonCode
        map[ADD_LOYALTY_CARD_LOYALTY_PLAN_KEY] = membershipPlanId

        return map
    }
        protected fun getDeletePaymentCardGenericMap(
            paymentSchemeValue: String,
            uuid: String
        ): Map<String, String> {
            val map = HashMap<String, String>()
            map[FIREBASE_PAYMENT_SCHEME_KEY] = getPaymentSchemeType(paymentSchemeValue).toString()
            map[FIREBASE_CLIENT_ACCOUNT_ID] = uuid

            return map
        }

        protected fun getAddLoyaltyResponseFailureMap(
            journeyValue: String,
            membershipPlanId: String
        ): Map<String, String> {
            val map = HashMap<String, String>()
            map[ADD_LOYALTY_CARD_JOURNEY_KEY] = journeyValue
            map[FIREBASE_CLIENT_ACCOUNT_ID_KEY] =
                SharedPreferenceManager.addLoyaltyCardRequestUuid.toString()
            map[ADD_LOYALTY_CARD_LOYALTY_PLAN_KEY] = membershipPlanId

            return map
        }

    }