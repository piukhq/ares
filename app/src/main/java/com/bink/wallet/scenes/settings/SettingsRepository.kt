package com.bink.wallet.scenes.settings

import android.util.Log
import com.bink.wallet.model.auth.User
import com.bink.wallet.network.ApiService
import com.bink.wallet.utils.LocalStoreUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsRepository(
    private val apiService: ApiService
) {

    fun putUserDetails(
        user: User
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.putUserDetailsAsync(user)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    Log.e("ConnorDebug","success:: " + response.first_name)

                    LocalStoreUtils.setAppSharedPref(
                        LocalStoreUtils.KEY_FIRST_NAME,
                        response.first_name
                    )

                    LocalStoreUtils.setAppSharedPref(
                        LocalStoreUtils.KEY_SECOND_NAME,
                        response.last_name
                    )
                } catch (e: Exception) {
                    // We don't care about any error
                    Log.e("ConnorDebug","error: " + e.localizedMessage)

                }
            }
        }
    }
}