package com.bink.wallet.scenes.sign_up.continue_with_email.magic_link_result

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.R
import com.bink.wallet.model.MagicLinkToken
import com.bink.wallet.model.auth.User
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.settings.UserRepository
import com.bink.wallet.utils.JWTUtils
import com.bink.wallet.utils.LocalStoreUtils
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class MagicLinkResultViewModel(val loginRepository: LoginRepository, val userRepository: UserRepository) : BaseViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    private val _email = MutableLiveData<String>()
    val email: LiveData<String>
        get() = _email

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    fun postMagicLinkToken(context: Context, token: String) {
        _isLoading.value = true
        val magicLinkToken = MagicLinkToken(token)
        viewModelScope.launch {
            try {
                val responseToken = loginRepository.sendMagicLinkToken(magicLinkToken)
                logInUser(context, responseToken.access_token)
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    private fun logInUser(context: Context, token: String) {
        LocalStoreUtils.setAppSharedPref(
            LocalStoreUtils.KEY_TOKEN,
            context.getString(R.string.token_api_v1, token)
        )

        try {
            JWTUtils.decode(token)?.let { tokenJson ->
                val emailFromJson = JSONObject(tokenJson).getString("user_id")
                _email.value = emailFromJson
            }
        } catch (e: JSONException) {
            _isLoading.value = false
        }

        viewModelScope.launch {
            try {
                val user = userRepository.getUserDetails()
                _user.value = user
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

}