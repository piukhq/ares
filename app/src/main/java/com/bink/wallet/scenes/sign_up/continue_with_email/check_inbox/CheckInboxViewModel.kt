package com.bink.wallet.scenes.sign_up.continue_with_email.check_inbox

import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.MagicLinkBody
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.MAGIC_LINK_BUNDLE_ID
import com.bink.wallet.utils.MAGIC_LINK_LOCALE
import com.bink.wallet.utils.MAGIC_LINK_SLUG
import kotlinx.coroutines.launch

class CheckInboxViewModel(val loginRepository: LoginRepository) : BaseViewModel() {

    fun rePostMagicLink(email: String) {
        viewModelScope.launch {
            try {
                loginRepository.sendMagicLink(MagicLinkBody(email, MAGIC_LINK_SLUG, MAGIC_LINK_LOCALE, MAGIC_LINK_BUNDLE_ID))
            } catch (e: Exception) {
            }
        }
    }

}