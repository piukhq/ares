package com.bink.wallet.scenes.dynamic_actions

import androidx.fragment.app.Fragment
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.auth.User
import com.bink.wallet.scenes.loyalty_wallet.ZendeskRepository
import com.bink.wallet.scenes.settings.UserRepository
import com.bink.wallet.utils.LocalStoreUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DynamicActionViewModel constructor(
    var zendeskRepository: ZendeskRepository,
    var userRepository: UserRepository
) : BaseViewModel() {

    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main)

    fun launchZendesk(fragment: Fragment, callbackUser: (User) -> Unit) {
        zendeskRepository.launchZendesk(fragment, callbackUser)
    }

    fun putUserDetails(user: User) {
        val handler = CoroutineExceptionHandler { _, _ -> //Exception handler to prevent app crash

        }
        scope.launch(handler) {
            try {
                withContext(Dispatchers.IO) { userRepository.putUserDetails(user) }
            } catch (e: Exception) {

            }
        }
    }

}