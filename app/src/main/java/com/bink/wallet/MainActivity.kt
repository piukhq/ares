package com.bink.wallet

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.bink.wallet.databinding.FragmentMaximisedBarcodeBinding
import com.bink.wallet.network.ApiConstants
import com.bink.wallet.utils.JwtCreator
import com.bink.wallet.utils.LocalStoreUtils
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import java.io.UnsupportedEncodingException

class MainActivity : AppCompatActivity() {

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
        val key = getNativeKey(ApiConstants.BASE_URL)
        val data = Base64.decode(getNativeKey(ApiConstants.BASE_URL), Base64.URL_SAFE)
        try {
            LocalStoreUtils.setAppSharedPref(LocalStoreUtils.KEY_SECRET, String(data), this)
            val currentToken = JwtCreator.createJwt(this)
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
