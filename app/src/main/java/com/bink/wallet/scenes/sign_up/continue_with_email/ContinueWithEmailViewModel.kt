package com.bink.wallet.scenes.sign_up.continue_with_email

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.BuildConfig
import com.bink.wallet.model.MagicLinkBody
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.enums.BuildTypes
import java.util.*

class ContinueWithEmailViewModel(val loginRepository: LoginRepository) : BaseViewModel() {

    val isLoading = MutableLiveData<Boolean>()

    private val _magicLinkError = MutableLiveData<Exception>()
    val magicLinkError: LiveData<Exception>
        get() = _magicLinkError

    private val _magicLinkSuccess = MutableLiveData<Any>()
    val magicLinkSuccess: LiveData<Any>
        get() = _magicLinkSuccess

    fun postMagicLink(email: String) {
        val slug = if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) != BuildTypes.RELEASE.type) "iceland-bonus-card-mock" else "iceland-bonus-card"

        loginRepository.sendMagicLink(
            MagicLinkBody(email, slug, "en_GB", "com.bink.wallet"),
            _magicLinkSuccess,
            _magicLinkError
        )
    }

}