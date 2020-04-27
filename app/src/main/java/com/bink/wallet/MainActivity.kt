package com.bink.wallet

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.FirebaseEvents.SPLASH_VIEW
import com.bink.wallet.utils.FirebaseUserProperties
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.enums.BuildTypes
import com.crashlytics.android.Crashlytics
import com.facebook.login.LoginManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import io.fabric.sdk.android.Fabric
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale
import kotlin.reflect.KProperty
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModel()
    lateinit var firebaseAnalytics: FirebaseAnalytics
    private var isFirstLaunch = true
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var btnSettings: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        logUserPropertiesAtStartUp()

        Fabric.with(this, Crashlytics())

        if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) != BuildTypes.MR.type) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        setContentView(R.layout.activity_main)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        toolbar = findViewById(R.id.toolbar)
        btnSettings = findViewById(R.id.settings_button)
        LocalStoreUtils.createEncryptedPrefs(applicationContext)
        initBottomBarNavigation()

        initToolbar()
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
                showHomeViews()
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

    fun showHomeViews() {
        bottomNavigationView.visibility = View.VISIBLE
        toolbar.visibility = View.VISIBLE
        btnSettings.visibility = View.VISIBLE
    }

    fun hideHomeViews() {
        bottomNavigationView.visibility = View.GONE
        toolbar.visibility = View.GONE
        btnSettings.visibility = View.GONE
    }

    private fun initToolbar() {
        setActionBar(toolbar)
        actionBar?.setDisplayShowTitleEnabled(false)

        btnSettings.setOnClickListener {
            findNavController(R.id.main_fragment).navigate(R.id.settings_screen)
            hideHomeViews()
        }
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
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.loyalty_menu_item -> {
                    if (canNavigate()) {
                        SharedPreferenceManager.isLoyaltySelected = true
                        findNavController(R.id.main_fragment).navigate(R.id.global_to_loyalty_wallet)
                    }
                }
                R.id.add_menu_item -> {
                    if (canNavigate()) {
                        findNavController(R.id.main_fragment).navigate(R.id.add_screen)
                        hideHomeViews()
                    }
                }
                R.id.payment_menu_item -> {
                    if (canNavigate()) {
                        findNavController(R.id.main_fragment).navigate(R.id.global_to_payment_wallet)
                        SharedPreferenceManager.isLoyaltySelected = false
                    }
                }

            }
            true

        }

        bottomNavigationView.selectedItemId =
            if (SharedPreferenceManager.isLoyaltySelected) R.id.loyalty_menu_item else R.id.payment_menu_item
    }

    private fun canNavigate(): Boolean {
        val currentId = findNavController(R.id.main_fragment).currentDestination?.id
        return currentId == R.id.loyalty_card_wallet
                || currentId == R.id.payment_card_wallet
                || currentId == R.id.home_wallet
    }
}

private operator fun Any.setValue(
    mainActivity: MainActivity,
    property: KProperty<*>,
    loginRepository: LoginRepository
) {

}


