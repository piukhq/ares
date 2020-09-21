package com.bink.wallet.scenes.settings

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.model.auth.User
import com.bink.wallet.network.ApiService
import com.bink.wallet.utils.LocalStoreUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserRepository(
    private val apiService: ApiService
) {

    fun putUserDetails(
        user: User,
        userResponse: MutableLiveData<User>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.putUserDetailsAsync(user)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    response.first_name?.let {
                        LocalStoreUtils.setAppSharedPref(
                            LocalStoreUtils.KEY_FIRST_NAME,
                            it
                        )
                    }

                    response.last_name?.let {
                        LocalStoreUtils.setAppSharedPref(
                            LocalStoreUtils.KEY_SECOND_NAME,
                            it
                        )
                    }
                    userResponse.value = response
                } catch (e: Exception) {
                    // We don't care about any error
                }
            }
        }
    }

    fun getUserDetails(
        user: MutableLiveData<User>,
        userReturned: MutableLiveData<Boolean> = MutableLiveData()
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getUserAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    response.first_name?.let {
                        LocalStoreUtils.setAppSharedPref(
                            LocalStoreUtils.KEY_FIRST_NAME,
                            it
                        )
                    }

                    response.last_name?.let {
                        LocalStoreUtils.setAppSharedPref(
                            LocalStoreUtils.KEY_SECOND_NAME,
                            it
                        )
                    }
                    user.value = response
                    userReturned.value = true
                } catch (e: Exception) {
                    // We don't care about any error
                    userReturned.value = false
                }
            }
        }
    }
}