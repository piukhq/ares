package com.bink.wallet

import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.bink.wallet.utils.LocalStoreUtils
import java.io.UnsupportedEncodingException
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

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

    private external fun getNativeKey(): String

    private fun storeSecret() {
        val data = Base64.decode(getNativeKey(), Base64.URL_SAFE)
        try {
            LocalStoreUtils.setAppSecret(String(data), this)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }
}
