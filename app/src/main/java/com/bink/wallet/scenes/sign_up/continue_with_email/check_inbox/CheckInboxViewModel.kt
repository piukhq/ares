package com.bink.wallet.scenes.sign_up.continue_with_email.check_inbox

import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.BuildConfig
import com.bink.wallet.model.MagicLinkBody
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.MAGIC_LINK_BUNDLE_ID
import com.bink.wallet.utils.MAGIC_LINK_DEBUG_SLUG
import com.bink.wallet.utils.MAGIC_LINK_LOCALE
import com.bink.wallet.utils.MAGIC_LINK_PROD_SLUG
import com.bink.wallet.utils.enums.BuildTypes
import kotlinx.coroutines.launch
import java.util.*

class CheckInboxViewModel(val loginRepository: LoginRepository) : BaseViewModel() {

    fun rePostMagicLink(email: String) {
        val slug = if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) != BuildTypes.RELEASE.type) MAGIC_LINK_DEBUG_SLUG else MAGIC_LINK_PROD_SLUG

        viewModelScope.launch {
            try {
                loginRepository.sendMagicLink(MagicLinkBody(email, slug, MAGIC_LINK_LOCALE, MAGIC_LINK_BUNDLE_ID))
            } catch (e: Exception) {
            }
        }
    }

}