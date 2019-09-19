package com.bink.wallet

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.bink.wallet.model.LoginData
import com.bink.wallet.network.ApiConstants
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.JwtCreator
import com.bink.wallet.utils.LocalStoreUtils
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.io.UnsupportedEncodingException
import kotlin.reflect.KProperty

class MainActivity : AppCompatActivity() {
    private var loginRepository: LoginRepository by inject { parametersOf(this) }

    companion object {
        init {
            System.loadLibrary("keys")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_main)
        storeSecret()
    }

    private external fun getNativeKey(url: String): String

    private fun storeSecret() {
        val data = Base64.decode(getNativeKey(ApiConstants.BASE_URL), Base64.URL_SAFE)
        try {
            val loginData = MutableLiveData<LoginData>()
            loginRepository.retrieveStoredLoginData(loginData)
            LocalStoreUtils.setAppSharedPref(LocalStoreUtils.KEY_SECRET, String(data), this)
            val currentToken = JwtCreator(loginRepository).createJwt(this)
            LocalStoreUtils.setAppSharedPref(LocalStoreUtils.KEY_JWT, currentToken, this)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        when {
            findNavController(R.id.main_fragment).currentDestination?.id == R.id.maximised_barcode_fragment -> {
                findNavController(R.id.main_fragment).popBackStack()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            findNavController(R.id.main_fragment).currentDestination?.id == R.id.pll_empty_fragment -> {
                //do nothing (back button action is prohibited here)
            }
            else -> super.onBackPressed()
        }
    }
}

private operator fun Any.setValue(mainActivity: MainActivity, property: KProperty<*>, loginRepository: LoginRepository) {

}
