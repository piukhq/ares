package com.bink.wallet.scenes.settings

import com.bink.wallet.model.DeleteRequest
import com.bink.wallet.model.auth.User
import com.bink.wallet.network.ApiService
import com.bink.wallet.utils.LocalStoreUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class UserRepository(private val apiService: ApiService) {

    suspend fun putUserDetails(user: User): User {
        val returnedUser = apiService.putUserDetailsAsync(user)

        returnedUser.first_name?.let {
            LocalStoreUtils.setAppSharedPref(
                LocalStoreUtils.KEY_FIRST_NAME,
                it
            )
        }

        returnedUser.last_name?.let {
            LocalStoreUtils.setAppSharedPref(
                LocalStoreUtils.KEY_SECOND_NAME,
                it
            )
        }

        return returnedUser
    }

    suspend fun getUserDetails(): User {
        val user = withContext(Dispatchers.IO) { apiService.getUserAsync() }

        user.first_name?.let { LocalStoreUtils.setAppSharedPref(LocalStoreUtils.KEY_FIRST_NAME, it) }
        user.last_name?.let { LocalStoreUtils.setAppSharedPref(LocalStoreUtils.KEY_SECOND_NAME, it) }
        user.uid.let { LocalStoreUtils.setAppSharedPref(LocalStoreUtils.KEY_UID, it) }

        return user
    }

    suspend fun deleteUser(deleteRequest: DeleteRequest): ResponseBody {
        return apiService.deleteUser(deleteRequest)
    }

}