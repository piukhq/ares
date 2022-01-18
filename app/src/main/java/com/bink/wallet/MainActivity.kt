package com.bink.wallet

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.AppConfiguration
import com.bink.wallet.model.isNewVersionAvailable
import com.bink.wallet.model.skipVersion
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.SPLASH_VIEW
import com.bink.wallet.utils.FirebaseEvents.UPDATE_ACTION
import com.bink.wallet.utils.FirebaseEvents.UPDATE_KEY
import com.bink.wallet.utils.FirebaseEvents.UPDATE_LATER
import com.bink.wallet.utils.FirebaseEvents.UPDATE_OPEN_STORE
import com.bink.wallet.utils.FirebaseEvents.UPDATE_SKIP
import com.bink.wallet.utils.enums.BuildTypes
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
import com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.reflect.KProperty
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModel()
    lateinit var firebaseAnalytics: FirebaseAnalytics
    private var isFirstLaunch = true
    private lateinit var appUpdateManager: AppUpdateManager
    var newIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        logUserPropertiesAtStartUp()

        SentryAndroid.init(
            this
        ) { options: SentryAndroidOptions ->
            options.environment =
                if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) == BuildTypes.RELEASE.type) "prod" else "beta"
            options.setDebug(BuildConfig.DEBUG)
            options.release = "${BuildConfig.APPLICATION_ID}@${BuildConfig.VERSION_NAME}+${BuildConfig.VERSION_CODE}"
        }

        if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) != BuildTypes.MR.type) {
            if (BuildConfig.SECURE_FLAGS) {
                window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }

        setContentView(R.layout.activity_main)
        LocalStoreUtils.createEncryptedPrefs(applicationContext)

        checkForUpdates()

        intent.data?.let {
            newIntent = intent
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

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    IMMEDIATE,
                    this,
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        newIntent = intent
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
            R.id.payment_card_wallet,
            R.id.loyalty_fragment,
            R.id.rooted_screen -> {
                finish()
            }
            R.id.magic_link_result_fragment,
            R.id.pll_empty_fragment -> {
                //do nothing (back button action is prohibited here)
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

    private fun checkForUpdates() {
        try {
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            RemoteConfigUtil().appConfig?.let { appConfiguration ->

                appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                            IMMEDIATE
                        ) && appConfiguration.isNewVersionAvailable()
                    ) {

                        lateinit var dialog: AlertDialog
                        val builder = AlertDialog.Builder(this)

                        builder.setTitle(getString(R.string.update_title))
                        builder.setMessage(getString(R.string.update_body))
                        val dialogClickListener = DialogInterface.OnClickListener { _, button ->
                            when (button) {
                                DialogInterface.BUTTON_POSITIVE -> {
                                    appUpdateManager.startUpdateFlowForResult(
                                        appUpdateInfo,
                                        IMMEDIATE,
                                        this,
                                        UPDATE_REQUEST_CODE
                                    )
                                    firebaseAnalytics.logEvent(
                                        UPDATE_ACTION,
                                        Bundle().apply { putString(UPDATE_KEY, UPDATE_OPEN_STORE) })
                                }
                                DialogInterface.BUTTON_NEGATIVE -> {
                                    firebaseAnalytics.logEvent(
                                        UPDATE_ACTION,
                                        Bundle().apply { putString(UPDATE_KEY, UPDATE_LATER) })
                                }
                                DialogInterface.BUTTON_NEUTRAL -> {
                                    appConfiguration.skipVersion()
                                    firebaseAnalytics.logEvent(
                                        UPDATE_ACTION,
                                        Bundle().apply { putString(UPDATE_KEY, UPDATE_SKIP) })
                                }
                            }
                        }

                        builder.setPositiveButton(
                            getString(R.string.update_start_update),
                            dialogClickListener
                        )
                        builder.setNeutralButton(
                            getString(R.string.update_skip_version),
                            dialogClickListener
                        )
                        builder.setNegativeButton(
                            getString(R.string.update_maybe_later),
                            dialogClickListener
                        )
                        dialog = builder.create()
                        dialog.setCancelable(false)
                        dialog.show()

                    }
                }
            }

        } catch (e: Exception) {
            return
        }
    }
}

private operator fun Any.setValue(
    mainActivity: MainActivity,
    property: KProperty<*>,
    loginRepository: LoginRepository
) {
}




