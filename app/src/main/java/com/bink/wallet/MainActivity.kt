package com.bink.wallet

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.verifyAvailableNetwork
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty

class MainActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        const val TOKEN_REFRESHED_EVENT = "REFRESH_LOYALTY"
    }

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) != "mr") {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        viewModel.membershipCardData.observeNonNull(this@MainActivity) {
            viewModel.membershipPlanData.observeNonNull(this@MainActivity) {
                LocalBroadcastManager.getInstance(this@MainActivity)
                    .sendBroadcast(Intent(TOKEN_REFRESHED_EVENT))
            }
        }

        setContentView(R.layout.activity_main)
        LocalStoreUtils.createEncryptedPrefs(applicationContext)
        runCoroutine()
    }

    override fun onBackPressed() {
        when (findNavController(R.id.main_fragment).currentDestination?.id) {
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
                findNavController(R.id.main_fragment).navigate(R.id.add_email_to_onboarding)
            }
            R.id.accept_tcs_fragment -> {
                findNavController(R.id.main_fragment).navigate(R.id.accept_to_onboarding)
            }
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        cancelCoroutine()
        super.onDestroy()
    }

    private fun runCoroutine() {
        job = startCoroutine()
    }

    private fun cancelCoroutine() {
        job.cancel()
    }

    fun resetCoroutine() {
        cancelCoroutine()
        runCoroutine()
    }

    private fun startCoroutine() = launch(Dispatchers.IO) {
        while (true) {
            delay(TimeUnit.HOURS.toMillis(1))
            withContext(Dispatchers.Main) {
                if (verifyAvailableNetwork(this@MainActivity)) {
                    viewModel.fetchMembershipPlans()
                    viewModel.fetchMembershipCards()
                }
            }
        }
    }

    override fun onResume() {
        if (verifyAvailableNetwork(this@MainActivity)) {
            viewModel.fetchMembershipPlans()
            viewModel.fetchMembershipCards()
        }
        resetCoroutine()
        super.onResume()
    }
}

private operator fun Any.setValue(
    mainActivity: MainActivity,
    property: KProperty<*>,
    loginRepository: LoginRepository
) {

}


