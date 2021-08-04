package com.bink.wallet.scenes.sign_up.continue_with_email.magic_link_result

import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.MagicLinkToken
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.logDebug
import kotlinx.coroutines.*

class MagicLinkResultViewModel(val loginRepository: LoginRepository) : BaseViewModel() {

    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main)

    fun postMagicLinkToken(token: String, isSent: () -> Unit) {
        val magicLinkToken = MagicLinkToken(token)
        val handler = CoroutineExceptionHandler { _, _ -> }
        scope.launch(handler) {
            try {
                logDebug("responseToken", "sent $token")
                val responseToken = loginRepository.sendMagicLinkToken(magicLinkToken)
                logDebug("responseToken", responseToken.access_token)
            } catch (e: Exception) {
                logDebug("responseToken", "$e")
            }
        }
    }

}