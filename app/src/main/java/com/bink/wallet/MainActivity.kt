package com.bink.wallet

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.wallets.WalletsFragment
import com.bink.wallet.utils.FirebaseEvents.SPLASH_VIEW
import com.bink.wallet.utils.FirebaseUserProperties
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.enums.BuildTypes
import com.crashlytics.android.Crashlytics
import com.facebook.login.LoginManager
import com.google.firebase.analytics.FirebaseAnalytics
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.reflect.KProperty
import kotlin.system.exitProcess
import androidx.navigation.ui.NavigationUI
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.bink.wallet.scenes.wallets.WalletsFragmentDirections
import com.bink.wallet.utils.navigateIfAdded

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModel()
    lateinit var firebaseAnalytics: FirebaseAnalytics
    private var isFirstLaunch = true
    private var listener = WalletsFragment.Listener.NULL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        logUserPropertiesAtStartUp()

        Fabric.with(this, Crashlytics())

        if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) != BuildTypes.MR.type) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        setContentView(R.layout.activity_main)
        LocalStoreUtils.createEncryptedPrefs(applicationContext)
        initBottomBarNavigation()
//        setUpNavigation()

        //todo clean
        setActionBar(toolbar)
        actionBar?.setDisplayShowTitleEnabled(false)

        settings_button.setOnClickListener {
            findNavController(R.id.main_fragment).navigate(R.id.settings_screen)
            hideBar()
        }
    }

    override fun onResume() {
        getMembershipPlans()
        super.onResume()
        if (isFirstLaunch) {
            if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) == BuildTypes.RELEASE.type) {
                firebaseAnalytics.setCurrentScreen(
                    this,
                    SPLASH_VIEW,
                    SPLASH_VIEW
                )
            }
            isFirstLaunch = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //Clear the Activity's bundle of the subsidiary fragments' bundles.
        outState.clear()
    }

    override fun onBackPressed() {
        when (findNavController(R.id.main_fragment).currentDestination?.id) {
            R.id.maximised_barcode_fragment -> {
                findNavController(R.id.main_fragment).popBackStack()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            R.id.pll_fragment -> {
                if (!SharedPreferenceManager.isAddJourney) {
                    findNavController(R.id.main_fragment).popBackStack()
                }
            }
            R.id.loyalty_card_detail_fragment -> {
                findNavController(R.id.main_fragment).navigate(R.id.global_to_home)
            }
            R.id.home_wallet,
            R.id.onboarding_fragment,
            R.id.rooted_screen -> {
                finish()
            }
            R.id.add_email_fragment -> {
                LoginManager.getInstance().logOut()
                findNavController(R.id.main_fragment).navigate(R.id.add_email_to_onboarding)
            }
            R.id.accept_tcs_fragment -> {
                LoginManager.getInstance().logOut()
                findNavController(R.id.main_fragment).navigate(R.id.accept_to_onboarding)
            }
            R.id.pll_empty_fragment -> {
                //do nothing (back button action is prohibited here)
            }
            R.id.settings_screen -> {
                findNavController(R.id.main_fragment).popBackStack()
                showBar()
            }
            else -> super.onBackPressed()
        }
    }

    fun forceRunApp() {
        Handler().postDelayed({
            LocalStoreUtils.clearPreferences(this)
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            launchIntent?.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        or Intent.FLAG_ACTIVITY_NEW_TASK
            )
            finishAffinity()
            startActivity(launchIntent)
            exitProcess(0)
        }, 1000)
    }

    fun showBar() {
        bottom_navigation.visibility = View.VISIBLE
        toolbar.visibility = View.VISIBLE
        settings_button.visibility = View.VISIBLE
    }

    fun hideBar() {
        bottom_navigation.visibility = View.GONE
        toolbar.visibility = View.GONE
        settings_button.visibility = View.GONE
    }

    private fun getMembershipPlans() {
        mainViewModel.getMembershipPlans()
    }

    private fun logUserPropertiesAtStartUp() {
        with(FirebaseUserProperties) {
            setUserProperty(
                firebaseAnalytics,
                OS_VERSION,
                android.os.Build.VERSION.SDK_INT.toString()
            )
            setUserProperty(
                firebaseAnalytics,
                NETWORK_STRENGTH,
                retrieveNetworkStatus(this@MainActivity)
            )
            setUserProperty(firebaseAnalytics, DEVICE_ZOOM, retrieveZoomStatus(this@MainActivity))
            setUserProperty(firebaseAnalytics, BINK_VERSION, retrieveBinkVersion(this@MainActivity))
        }
    }

    private fun initBottomBarNavigation() {
        bottom_navigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.loyalty_menu_item -> {
                    SharedPreferenceManager.isLoyaltySelected = true
                    findNavController(R.id.main_fragment).navigate(R.id.global_to_loyalty_wallet)
                }
                R.id.add_menu_item -> {
                    listener.onOpenAddScreen()
                    hideBar()
//                    viewModel.membershipPlanData.value?.let {
//                        val directions =
//                            it.toTypedArray().let { plans ->
//                                WalletsFragmentDirections.homeToAdd(
//                                    plans
//                                )
//                            }
//                        directions.let { findNavController().navigateIfAdded(this, it) }
//                    }
                }
                R.id.payment_menu_item -> {
                    findNavController(R.id.main_fragment).navigate(R.id.global_to_payment_wallet)
                    SharedPreferenceManager.isLoyaltySelected = false
                }

            }
            true
        }
    }

    fun setListener(listener: WalletsFragment.Listener) {
        this.listener = listener
    }

    fun setUpNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.main_fragment) as NavHostFragment?
        NavigationUI.setupWithNavController(
            bottom_navigation,
            navHostFragment!!.navController
        )
    }
}

private operator fun Any.setValue(
    mainActivity: MainActivity,
    property: KProperty<*>,
    loginRepository: LoginRepository
) {

}


