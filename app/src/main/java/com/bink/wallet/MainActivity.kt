package com.bink.wallet

import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
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

    private external fun getNativeKey(): String

    private fun storeSecret() {
        val data = Base64.decode(getNativeKey(), Base64.URL_SAFE)
        try {
            LocalStoreUtils.setAppSharedPref(LocalStoreUtils.KEY_SECRET, String(data), this)
            val currentToken = JwtCreator.createJwt(this)
            LocalStoreUtils.setAppSharedPref(LocalStoreUtils.KEY_JWT, currentToken, this)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }
}
