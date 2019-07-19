package com.bink.wallet

import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.bink.wallet.network.ApiService
import com.bink.wallet.utils.LocalStoreUtils
import org.koin.android.ext.android.inject
import java.io.UnsupportedEncodingException


class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("keys")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        storeSecret()
    }

    private external fun getNativeKey(): String

    private fun storeSecret() {

        if (LocalStoreUtils.getAppSecret(this) == null) {

            val data = Base64.decode(getNativeKey(), Base64.DEFAULT)
            try {
                LocalStoreUtils.setAppSecret(String(data), this)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

        }
    }
}
