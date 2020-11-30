package com.bink.wallet.scenes.dynamic_actions

import androidx.fragment.app.Fragment
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.auth.User
import com.bink.wallet.scenes.loyalty_wallet.ZendeskRepository

class DynamicActionViewModel  constructor(var zendeskRepository: ZendeskRepository): BaseViewModel() {

    fun launchZendesk(fragment: Fragment, callbackUser: (User) -> Unit) {
        zendeskRepository.launchZendesk(fragment, callbackUser)
    }

}