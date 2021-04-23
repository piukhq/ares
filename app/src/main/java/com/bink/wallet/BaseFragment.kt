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
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.*
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletFragmentDirections
import com.bink.wallet.scenes.payment_card_wallet.PaymentCardWalletFragmentDirections
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_JOURNEY_KEY
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_LOYALTY_PLAN_KEY
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_LOYALTY_REASON_CODE_KEY
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_LOYALTY_STATUS_KEY
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_SCANNED_CARD_KEY
import com.bink.wallet.utils.FirebaseEvents.ADD_PAYMENT_CARD_PAYMENT_STATUS_NEW_KEY
import com.bink.wallet.utils.FirebaseEvents.ANALYTICS_CALL_TO_ACTION_TYPE
import com.bink.wallet.utils.FirebaseEvents.ANALYTICS_IDENTIFIER
import com.bink.wallet.utils.FirebaseEvents.ATTEMPTED_EVENT_KEY
import com.bink.wallet.utils.FirebaseEvents.DYNAMIC_ACTION_NAME
import com.bink.wallet.utils.FirebaseEvents.FAILED_EVENT_NO_DATA
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_ACCOUNT_IS_NEW_KEY
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_CLIENT_ACCOUNT_ID_KEY
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_ERROR_CODE
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_ERROR_MESSAGE
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_PAYMENT_SCHEME_KEY
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_REQUEST_REVIEW_TRIGGER
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_STATUS_KEY
import com.bink.wallet.utils.FirebaseEvents.ONBOARDING_SUCCESS_KEY
import com.bink.wallet.utils.FirebaseEvents.PLL_LINK_ID_KEY
import com.bink.wallet.utils.FirebaseEvents.PLL_LOYALTY_ID_KEY
import com.bink.wallet.utils.FirebaseEvents.PLL_PAYMENT_ID_KEY
import com.bink.wallet.utils.FirebaseEvents.PLL_STATE_KEY
import com.bink.wallet.utils.enums.BuildTypes
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.bink.wallet.utils.toolbar.ToolbarManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.sentry.core.Sentry
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.util.*
import kotlin.collections.HashMap
import io.sentry.core.protocol.User as SentryUser

abstract class BaseFragment<VM : BaseViewModel, DB : ViewDataBinding> : Fragment() {

    @get:LayoutRes
    abstract val layoutRes: Int

    abstract val viewModel: VM

    open fun createDynamicAction(dynamicActionLocation: DynamicActionLocation, dynamicAction: DynamicAction) {}

    open var binding: DB? = null

    open lateinit var bottomNavigation: BottomNavigationView

    open val windowFullscreenHandler: WindowFullscreenHandler by inject {
        parametersOf(
            requireActivity()
        )
    }

    open var membershipCardsForBrands: Array<MembershipCard>? = null
    open var membershipPlansForBrands: Array<MembershipPlan>? = null

    private var addOnDestinationChangedListener: NavController.OnDestinationChangedListener? = null

    private lateinit var keyboardHiddenListener: ViewTreeObserver.OnGlobalLayoutListener

    open fun init(inflater: LayoutInflater, container: ViewGroup) {
        binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)
    }

    open fun init() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        container?.let {
            init(inflater, container)
        }
        init()
        checkForDynamicActions()
        return binding?.root
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

        if (activity != null) {
            bottomNavigation = requireActivity().findViewById(R.id.bottom_navigation)

        }

        if (this.isAdded) {
            addOnDestinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.loyalty_fragment, R.id.payment_card_wallet -> {
                        bottomNavigation.visibility =
                            View.VISIBLE
                        setUpBottomNavListener()
                    }
                    else -> bottomNavigation.visibility = View.GONE
                }
            }
            addOnDestinationChangedListener?.let {
                findNavController().addOnDestinationChangedListener(it)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        addOnDestinationChangedListener?.let {
            findNavController().removeOnDestinationChangedListener(it)
        }
    }

    private fun checkForDynamicActions() {
        getDynamicActionScreenForFragment(this.javaClass.canonicalName ?: "")?.let { currentDynamicActionScreen ->
            var dynamicActionsList: ArrayList<DynamicAction>

            try {
                dynamicActionsList = Gson().fromJson(FirebaseRemoteConfig.getInstance().getString(REMOTE_CONFIG_DYNAMIC_ACTIONS), object : TypeToken<ArrayList<DynamicAction?>?>() {}.type)
            } catch (e: Exception) {
                return
            }

            for (dynamicAction in dynamicActionsList) {
                if (isDynamicActionInDate(dynamicAction)) {
                    dynamicAction.locations?.let { dynamicActionLocations ->

                        for (dynamicActionLocation in dynamicActionLocations) {
                            dynamicActionLocation.screen?.let { dynamicActionScreen ->

                                if (dynamicActionScreen == currentDynamicActionScreen) {
                                    createDynamicAction(dynamicActionLocation, dynamicAction)
                                }

                            }
                        }

                    }
                }
            }

        }
    }

    private fun getDynamicActionScreenForFragment(fragmentName: String): DynamicActionScreen? {
        val className = try {
            fragmentName.split(".").last()
        } catch (e: Exception) {
            return null
        }

        when (className) {
            "LoyaltyWalletFragment" -> return DynamicActionScreen.LOYALTY_WALLET
            "PaymentCardWalletFragment" -> return DynamicActionScreen.PAYMENT_WALLET
        }

        return null
    }

    fun getEmojiByUnicode(unicode: String?): String {
        try {
            if (unicode == null) return ""
            val trimmedUniCode = unicode.removeRange(0, 2)
            val longUniCode = trimmedUniCode.toLong(16)
            return String(Character.toChars(longUniCode.toInt()))
        } catch (e: Exception) {
            return ""
        }
    }

    fun bindEventToDynamicAction(view: View, dynamicActionLocation: DynamicActionLocation, dynamicAction: DynamicAction) {
        dynamicActionLocation.action?.let { action ->
            when (action) {
                DynamicActionHandler.SINGLE_TAP -> {
                    view.setOnClickListener {
                        dynamicAction.event?.let { event ->
                            launchDynamicActionEvent(dynamicAction.type, event, dynamicAction.name ?: "")
                        }
                    }
                }
            }
        }
    }

    private fun launchDynamicActionEvent(type: DynamicActionType?, event: DynamicActionEvent, dynamicActionName: String) {
        when (type) {
            DynamicActionType.XMAS -> {
                val directions = when (findNavController().currentDestination?.id) {
                    R.id.loyalty_fragment -> LoyaltyWalletFragmentDirections.loyaltyToDynamicAction(event)
                    R.id.payment_card_wallet -> PaymentCardWalletFragmentDirections.paymentToDynamicAction(event)
                    else -> null
                }
                directions?.let {
                    logEvent(FirebaseEvents.DYNAMIC_ACTION_TRIGGER_EVENT, getRequestReviewMap(dynamicActionName))
                    findNavController().navigateIfAdded(this, directions)
                }
            }
        }
    }

    private fun isDynamicActionInDate(dynamicAction: DynamicAction): Boolean {
        if (dynamicAction.start_date == null || dynamicAction.end_date == null) return false
        val currentTime = System.currentTimeMillis() / 1000
        //For testing
        //val currentTime = 1608537601
        return currentTime > dynamicAction.start_date && currentTime < dynamicAction.end_date
    }

    private fun setUpBottomNavListener() {
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.loyalty_menu_item -> {
                    SharedPreferenceManager.isLoyaltySelected = true
                    navigateToLoyaltyWallet()
                }
                R.id.add_menu_item -> {

                    if (membershipPlansForBrands != null && membershipCardsForBrands != null) {
                        val directions =
                            when (findNavController().currentDestination?.id) {
                                R.id.loyalty_fragment -> LoyaltyWalletFragmentDirections.loyaltyToBrowseBrands(membershipPlansForBrands!!, membershipCardsForBrands!!)
                                R.id.payment_card_wallet -> PaymentCardWalletFragmentDirections.paymentToBrowseBrands(membershipPlansForBrands!!, membershipCardsForBrands!!)
                                else -> null
                            }
                        directions?.let { findNavController().navigateIfAdded(this, directions) }
                    }
                }

                R.id.payment_menu_item -> {
                    SharedPreferenceManager.isLoyaltySelected = false
                    navigateToPaymentCardWalletWallet()
                }

            }
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolbarManager(builder()).prepareToolbar()
    }

    protected abstract fun builder(): FragmentToolbar

    protected fun setAnalyticsUserId(uid: String) {
        val user = SentryUser()
        user.id = uid
        Sentry.setUser(user)
        (requireActivity() as MainActivity).firebaseAnalytics.setUserId(uid)
    }

    protected fun logEvent(identifierValue: String) {
        logFirebaseEvent(identifierValue)
    }

    protected fun logEvent(name: String, parameters: Map<String, Any>) {
        val bundle = Bundle()

        for (entry: Map.Entry<String, Any> in parameters) {
            if (entry.value is Int) {
                bundle.putInt(entry.key, entry.value as Int)
            } else {
                bundle.putString(entry.key, entry.value.toString())

            }
        }

        (requireActivity() as MainActivity).firebaseAnalytics.logEvent(name, bundle)
    }

    protected fun failedEvent(eventName: String) {
        val bundle = Bundle()

        bundle.putString(ATTEMPTED_EVENT_KEY, eventName)
        (requireActivity() as MainActivity).firebaseAnalytics.logEvent(FAILED_EVENT_NO_DATA, bundle)

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

    protected fun getOnboardingStartMap(onBoardingJourneyValue: String): Map<String, String> {
        SharedPreferenceManager.firebaseEventUuid = UUID.randomUUID().toString()
        val map = HashMap<String, String>()
        map[FirebaseEvents.ONBOARDING_JOURNEY_KEY] = onBoardingJourneyValue
        map[FirebaseEvents.ONBOARDING_ID_KEY] = SharedPreferenceManager.firebaseEventUuid.toString()
        return map
    }

    protected fun getOnboardingEndMap(onBoardingSuccessValue: String): Map<String, String> {
        val map = HashMap<String, String>()
        map[FirebaseEvents.ONBOARDING_ID_KEY] = SharedPreferenceManager.firebaseEventUuid.toString()
        map[ONBOARDING_SUCCESS_KEY] = onBoardingSuccessValue
        return map
    }

    protected fun getOnboardingGenericMap(): Map<String, String> {
        val map = HashMap<String, String>()
        map[FirebaseEvents.ONBOARDING_ID_KEY] = SharedPreferenceManager.firebaseEventUuid.toString()
        return map
    }

    //This will handle both request and response failure
    protected fun getAddPaymentCardGenericMap(paymentSchemeValue: String): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[FIREBASE_PAYMENT_SCHEME_KEY] = getPaymentSchemeType(paymentSchemeValue)
        return map
    }

    protected fun getAddPaymentCardFailMap(paymentSchemeValue: String, error_code: Int, error_message: String): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[FIREBASE_PAYMENT_SCHEME_KEY] = getPaymentSchemeType(paymentSchemeValue)
        map[FIREBASE_ERROR_CODE] = error_code
        map[FIREBASE_ERROR_MESSAGE] = error_message
        return map
    }

    protected fun getAddPaymentCardResponseSuccessMap(
        paymentCardId: String,
        paymentSchemeValue: String,
        isAccountNew: String,
        paymentStatus: String
    ): Map<String, Any> {
        val map = HashMap<String, Any>()

        map[FIREBASE_CLIENT_ACCOUNT_ID_KEY] = paymentCardId
        map[FIREBASE_PAYMENT_SCHEME_KEY] = getPaymentSchemeType(paymentSchemeValue)
        map[FIREBASE_ACCOUNT_IS_NEW_KEY] = isAccountNew
        map[ADD_PAYMENT_CARD_PAYMENT_STATUS_NEW_KEY] = paymentStatus

        return map
    }


    protected fun getAddLoyaltyCardRequestMap(
        journeyValue: String,
        membershipPlanId: String,
        isAScannedCard: String
    ): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[ADD_LOYALTY_CARD_JOURNEY_KEY] = journeyValue
        map[ADD_LOYALTY_CARD_LOYALTY_PLAN_KEY] = membershipPlanId.toInt()
        map[ADD_LOYALTY_CARD_SCANNED_CARD_KEY] = isAScannedCard

        return map

    }

    protected fun getAddLoyaltyResponseSuccessMap(
        journeyValue: String,
        loyaltyCardId: String,
        loyaltyStatus: String,
        reasonCode: String,
        membershipPlanId: String,
        isAccountNew: String
    ): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[ADD_LOYALTY_CARD_JOURNEY_KEY] = journeyValue
        map[FIREBASE_CLIENT_ACCOUNT_ID_KEY] = loyaltyCardId
        map[FIREBASE_ACCOUNT_IS_NEW_KEY] = isAccountNew
        map[ADD_LOYALTY_CARD_LOYALTY_STATUS_KEY] = loyaltyStatus
        map[ADD_LOYALTY_CARD_LOYALTY_REASON_CODE_KEY] = reasonCode
        map[ADD_LOYALTY_CARD_LOYALTY_PLAN_KEY] = membershipPlanId.toInt()

        return map
    }

    protected fun getDeletePaymentCardGenericMap(
        paymentSchemeValue: String,
        paymentCardId: String
    ): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[FIREBASE_PAYMENT_SCHEME_KEY] = getPaymentSchemeType(paymentSchemeValue)
        map[FIREBASE_CLIENT_ACCOUNT_ID_KEY] = paymentCardId

        return map
    }

    protected fun getDeletePaymentCardFailedMap(
        paymentSchemeValue: String,
        paymentCardId: String,
        error_code: Int,
        error_message: String
    ): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[FIREBASE_PAYMENT_SCHEME_KEY] = getPaymentSchemeType(paymentSchemeValue)
        map[FIREBASE_CLIENT_ACCOUNT_ID_KEY] = paymentCardId
        map[FIREBASE_ERROR_CODE] = error_code
        map[FIREBASE_ERROR_MESSAGE] = error_message

        return map
    }

    protected fun getAddLoyaltyResponseFailureMap(
        journeyValue: String,
        membershipPlanId: String,
        error_code: Int,
        error_message: String
    ): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[ADD_LOYALTY_CARD_JOURNEY_KEY] = journeyValue
        map[ADD_LOYALTY_CARD_LOYALTY_PLAN_KEY] = membershipPlanId.toInt()
        map[FIREBASE_ERROR_CODE] = error_code
        map[FIREBASE_ERROR_MESSAGE] = error_message

        return map
    }

    protected fun getAddLoyaltyResponseFailureMap(
        journeyValue: String,
        membershipPlanId: String
    ): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[ADD_LOYALTY_CARD_JOURNEY_KEY] = journeyValue
        map[ADD_LOYALTY_CARD_LOYALTY_PLAN_KEY] = membershipPlanId.toInt()

        return map
    }

    protected fun getDeleteLoyaltyCardGenericMap(
        loyaltyPlan: String,
        loyaltyCardId: String
    ): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[ADD_LOYALTY_CARD_LOYALTY_PLAN_KEY] = loyaltyPlan.toInt()
        map[FIREBASE_CLIENT_ACCOUNT_ID_KEY] = loyaltyCardId

        return map
    }

    protected fun getDeleteLoyaltyCardFailMap(
        loyaltyPlan: String,
        loyaltyCardId: String,
        error_code: Int,
        error_message: String
    ): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[ADD_LOYALTY_CARD_LOYALTY_PLAN_KEY] = loyaltyPlan.toInt()
        map[FIREBASE_CLIENT_ACCOUNT_ID_KEY] = loyaltyCardId
        map[FIREBASE_ERROR_CODE] = error_code
        map[FIREBASE_ERROR_MESSAGE] = error_message

        return map
    }

    protected fun getRequestReviewMap(reviewTrigger: String): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[FIREBASE_REQUEST_REVIEW_TRIGGER] = reviewTrigger
        return map
    }

    protected fun getDynamicActionMap(dynamicActionName: String): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[DYNAMIC_ACTION_NAME] = dynamicActionName
        return map
    }

    companion object {
        fun getPaymentSchemeType(paymentScheme: String): Int {
            return when (paymentScheme) {
                "Visa" -> 0
                "MasterCard" -> 1
                //amex
                else -> 2
            }
        }

        fun getPaymentCardStatusMap(
            paymentSchemeValue: String,
            uuid: String,
            status: String
        ): Map<String, Any> {
            val map = HashMap<String, Any>()
            map[FIREBASE_PAYMENT_SCHEME_KEY] = getPaymentSchemeType(paymentSchemeValue)
            map[FIREBASE_CLIENT_ACCOUNT_ID_KEY] = uuid
            map[FIREBASE_STATUS_KEY] = status

            return map
        }

        fun getLoyaltyCardStatusMap(
            uuid: String,
            status: String,
            planId: String
        ): Map<String, Any> {
            val map = HashMap<String, Any>()
            map[FIREBASE_CLIENT_ACCOUNT_ID_KEY] = uuid
            map[FIREBASE_STATUS_KEY] = status
            map[ADD_LOYALTY_CARD_LOYALTY_PLAN_KEY] = planId.toInt()

            return map
        }

        fun getPllPatchMap(
            paymentUuid: String,
            loyaltyUuid: String,
            state: String
        ): Map<String, Any> {
            val map = HashMap<String, Any>()
            map[PLL_PAYMENT_ID_KEY] = paymentUuid
            map[PLL_LOYALTY_ID_KEY] = loyaltyUuid
            map[PLL_LINK_ID_KEY] = "$loyaltyUuid/$paymentUuid"
            map[PLL_STATE_KEY] = state

            return map
        }

        fun getPllDeleteMap(paymentUuid: String, loyaltyUuid: String): Map<String, Any> {
            val map = HashMap<String, Any>()
            map[PLL_PAYMENT_ID_KEY] = paymentUuid
            map[PLL_LOYALTY_ID_KEY] = loyaltyUuid
            map[PLL_LINK_ID_KEY] = "$loyaltyUuid/$paymentUuid"

            return map
        }

        fun getPllActiveMap(paymentUuid: String, loyaltyUuid: String): Map<String, Any> {
            val map = HashMap<String, Any>()
            map[PLL_LINK_ID_KEY] = "$loyaltyUuid/$paymentUuid"
            map[PLL_PAYMENT_ID_KEY] = paymentUuid
            map[PLL_LOYALTY_ID_KEY] = loyaltyUuid

            return map
        }

        fun logPllEvent(name: String, parameters: Map<String, Any>) {
            val bundle = Bundle()

            for (entry: Map.Entry<String, Any> in parameters) {
                if (entry.value is Int) {
                    bundle.putInt(entry.key, entry.value as Int)
                } else {
                    bundle.putString(entry.key, entry.value.toString())

                }
            }

            Firebase.analytics.logEvent(name, bundle)
        }

        fun logFailedEvent(eventName: String) {
            val bundle = Bundle()

            bundle.putString(ATTEMPTED_EVENT_KEY, eventName)
            Firebase.analytics.logEvent(FAILED_EVENT_NO_DATA, bundle)

        }

    }

    private fun navigateToLoyaltyWallet() {
        if (this.isAdded) {
            if (findNavController().currentDestination?.id == R.id.payment_card_wallet) {
                findNavController().navigateIfAdded(
                    this,
                    PaymentCardWalletFragmentDirections.paymentToLoyaltyWallet()
                )
            }
        }
    }

    private fun navigateToPaymentCardWalletWallet() {
        if (this.isAdded) {
            if (findNavController().currentDestination?.id == R.id.loyalty_fragment) {
                findNavController().navigateIfAdded(
                    this,
                    LoyaltyWalletFragmentDirections.loyaltyToPaymentWallet()
                )
            }
        }
    }

}