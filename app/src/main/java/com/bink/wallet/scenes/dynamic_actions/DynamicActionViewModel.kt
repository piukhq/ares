package com.bink.wallet.scenes.dynamic_actions

import com.bink.wallet.BaseViewModel
import com.bink.wallet.scenes.settings.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class DynamicActionViewModel constructor(
    var userRepository: UserRepository
) : BaseViewModel() {

}