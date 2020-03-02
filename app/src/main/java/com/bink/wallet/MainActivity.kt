package com.bink.wallet

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.di.networkModule
import com.bink.wallet.di.provideRetrofit
import com.bink.wallet.di.qualifier.network.NetworkQualifiers
import com.bink.wallet.network.ApiConstants
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.FirebaseUserProperties
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.enums.ApiVersion
import com.bink.wallet.utils.enums.BuildTypes
import com.crashlytics.android.Crashlytics
import com.facebook.login.LoginManager
import com.google.firebase.analytics.FirebaseAnalytics
import io.fabric.sdk.android.Fabric
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.reflect.KProperty


class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModel()
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SharedPreferenceManager.storedApiUrl.isNullOrEmpty()) {
            SharedPreferenceManager.storedApiUrl = ApiConstants.BASE_URL
        }
        networkModule.apply {
            single(NetworkQualifiers.SpreedlyRetrofit) { provideRetrofit(get(NetworkQualifiers.SpreedlyOkHttp),
                SharedPreferenceManager.storedApiUrl.toString()
            ) }
            single(NetworkQualifiers.BinkRetrofit) { provideRetrofit(get(NetworkQualifiers.BinkOkHttp),
                SharedPreferenceManager.storedApiUrl.toString()
            ) }
        }
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        logUserPropertiesAtStartUp()

        Fabric.with(this, Crashlytics())

        if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) != BuildTypes.MR.type) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        setContentView(R.layout.activity_main)
        LocalStoreUtils.createEncryptedPrefs(applicationContext)
    }

    override fun onResume() {
        getMembershipPlans()
        super.onResume()
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
            R.id.pll_empty_fragment,
            R.id.card_terms_and_conditions -> {
                //do nothing (back button action is prohibited here)
            }
            else -> super.onBackPressed()
        }
    }

    fun restartApp() {
        val startActivity = Intent(this, MainActivity::class.java)
        val pendingIntentId = 123456
        val pendingIntent = PendingIntent.getActivity(
            this,
            pendingIntentId,
            startActivity,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val mgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)
        finish()
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
}

private operator fun Any.setValue(
    mainActivity: MainActivity,
    property: KProperty<*>,
    loginRepository: LoginRepository
) {

}


