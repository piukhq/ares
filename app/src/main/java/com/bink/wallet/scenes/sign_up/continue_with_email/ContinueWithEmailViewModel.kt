package com.bink.wallet.scenes.sign_up.continue_with_email

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.BuildConfig
import com.bink.wallet.model.MagicLinkBody
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.MAGIC_LINK_BUNDLE_ID
import com.bink.wallet.utils.MAGIC_LINK_DEBUG_SLUG
import com.bink.wallet.utils.MAGIC_LINK_LOCALE
import com.bink.wallet.utils.MAGIC_LINK_PROD_SLUG
import com.bink.wallet.utils.enums.BuildTypes
import kotlinx.coroutines.*
import java.util.*

class ContinueWithEmailViewModel(val loginRepository: LoginRepository) : BaseViewModel() {

    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _magicLinkError = MutableLiveData<Exception>()
    val magicLinkError: LiveData<Exception>
        get() = _magicLinkError

    fun postMagicLink(email: String, isSent: () -> Unit) {
        _isLoading.value = true
        val slug = if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) != BuildTypes.RELEASE.type) MAGIC_LINK_DEBUG_SLUG else MAGIC_LINK_PROD_SLUG

        val handler = CoroutineExceptionHandler { _, _ -> }
        scope.launch(handler) {
            try {
                loginRepository.sendMagicLink(MagicLinkBody(email, slug, MAGIC_LINK_LOCALE, MAGIC_LINK_BUNDLE_ID))
                _isLoading.value = false
                isSent()
            } catch (e: Exception) {
                _magicLinkError.value = e
                _isLoading.value = false
            }
        }
    }

}