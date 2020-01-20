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
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.LocalStoreUtils
import com.crashlytics.android.Crashlytics
import com.facebook.login.LoginManager
import io.fabric.sdk.android.Fabric
import java.util.Locale
import kotlin.reflect.KProperty

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) != "mr") {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        setContentView(R.layout.activity_main)
        LocalStoreUtils.createEncryptedPrefs(applicationContext)
    }

    override fun onBackPressed() {
        val navId = findNavController(R.id.main_fragment).currentDestination?.id
        when (navId) {
            R.id.maximised_barcode_fragment -> {
                findNavController(R.id.main_fragment).popBackStack()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            R.id.pll_empty_fragment -> {
                //do nothing (back button action is prohibited here)
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
            R.id.card_terms_and_conditions -> {
                // do nothing (back button is prohibited here)
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
}

private operator fun Any.setValue(
    mainActivity: MainActivity,
    property: KProperty<*>,
    loginRepository: LoginRepository
) {

}


