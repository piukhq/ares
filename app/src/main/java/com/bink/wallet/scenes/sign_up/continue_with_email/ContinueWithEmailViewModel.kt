package com.bink.wallet.scenes.sign_up.continue_with_email

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.MagicLinkBody
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.MAGIC_LINK_BUNDLE_ID
import com.bink.wallet.utils.MAGIC_LINK_LOCALE
import com.bink.wallet.utils.MAGIC_LINK_SLUG
import com.bink.wallet.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class ContinueWithEmailViewModel(val loginRepository: LoginRepository) : BaseViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _isSuccessful = SingleLiveEvent<Boolean>()
    val isSuccessful: SingleLiveEvent<Boolean>
        get() = _isSuccessful

    private val _magicLinkError = MutableLiveData<Exception>()
    val magicLinkError: LiveData<Exception>
        get() = _magicLinkError

    fun postMagicLink(email: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                loginRepository.sendMagicLink(MagicLinkBody(email, MAGIC_LINK_SLUG, MAGIC_LINK_LOCALE, MAGIC_LINK_BUNDLE_ID))
                _isLoading.value = false
                _isSuccessful.value = true
            } catch (e: Exception) {
                _magicLinkError.value = e
                _isLoading.value = false
                _isSuccessful.value = false

            }
        }
    }

}