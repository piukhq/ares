package com.bink.wallet

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.wallets.WalletsViewModel
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.observeNonNull
import com.crashlytics.android.Crashlytics
import com.facebook.login.LoginManager
import io.fabric.sdk.android.Fabric
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KProperty

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    companion object {
        const val TOKEN_REFRESHED_EVENT = "REFRESH_LOYALTY"
        const val MINUTES_REFRESH = 2L
        const val HOURS_REFRESH = 1L
    }

    private var jobHourly: Job = Job()
    private var jobWallets: Job = Job()


    val viewModel: WalletsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) != "mr") {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        setContentView(R.layout.activity_main)
        LocalStoreUtils.createEncryptedPrefs(applicationContext)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //Clear the Activity's bundle of the subsidiary fragments' bundles.
        outState.clear()
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

    override fun onDestroy() {
        cancelHourlyCoroutine()
        cancelWalletCoroutine()
        super.onDestroy()
    }

    private fun runHourlyCoroutine() {
        jobHourly = startCoroutine(false)
    }

    fun cancelHourlyCoroutine() {
        jobHourly.cancel()
    }

    fun resetHourlyCoroutine() {
        cancelHourlyCoroutine()
        runHourlyCoroutine()
    }

    private fun runWalletCoroutine() {
        jobWallets = startCoroutine(true)
    }

    fun cancelWalletCoroutine() {
        jobWallets.cancel()
    }

    fun resetWalletCoroutine() {
        cancelWalletCoroutine()
        runWalletCoroutine()
    }

    suspend fun fetchData() {
        viewModel.fetchMembershipPlans()
        viewModel.fetchMembershipCards()
        viewModel.fetchPaymentCards()
    }

    private suspend fun createHourlyObservers() {
        withContext(Dispatchers.Main) {
            viewModel.membershipCardData.observeNonNull(this@MainActivity) {
                viewModel.membershipPlanData.observeNonNull(this@MainActivity) {
                    LocalBroadcastManager.getInstance(this@MainActivity)
                        .sendBroadcast(Intent(TOKEN_REFRESHED_EVENT))
                    viewModel.membershipCardData.removeObservers(this@MainActivity)
                    viewModel.membershipPlanData.removeObservers(this@MainActivity)
                }
            }
        }
    }

    private suspend fun createWalletsObservers() {
        withContext(Dispatchers.Main) {
            viewModel.membershipCardData.observeNonNull(this@MainActivity) {
                viewModel.paymentCards.observeNonNull(this@MainActivity) {
                    LocalBroadcastManager.getInstance(this@MainActivity)
                        .sendBroadcast(Intent(TOKEN_REFRESHED_EVENT))
                    viewModel.membershipCardData.removeObservers(this@MainActivity)
                    viewModel.paymentCards.removeObservers(this@MainActivity)
                }
            }
        }
    }

    private fun startCoroutine(isWalletCoroutine: Boolean): Job {
        return launch(Dispatchers.IO) {
            withContext(Dispatchers.Default) {

                while (true) {
                    if (isWalletCoroutine) {
                        delay(TimeUnit.MINUTES.toMillis(MINUTES_REFRESH))
                        withContext(Dispatchers.Main) {
                            if (UtilFunctions.isNetworkAvailable(this@MainActivity) &&
                                !LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_TOKEN).isNullOrEmpty()
                            ) {
                                createWalletsObservers()
                                viewModel.fetchPaymentCards()
                                viewModel.fetchMembershipCards()
                            }
                        }
                    } else {
                        delay(TimeUnit.HOURS.toMillis(HOURS_REFRESH))
                        withContext(Dispatchers.Main) {
                            if (UtilFunctions.isNetworkAvailable(this@MainActivity) &&
                                !LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_TOKEN).isNullOrEmpty()
                            ) {
                                createHourlyObservers()
                                viewModel.fetchMembershipPlans()
                                viewModel.fetchMembershipCards()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        if (UtilFunctions.isNetworkAvailable(this@MainActivity) &&
            !LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_TOKEN).isNullOrEmpty()
        ) {
            runBlocking {
                fetchData()
            }
        }
        resetHourlyCoroutine()
        super.onResume()
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


