package com.bink.wallet.scenes.settings

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.LoginData

class SettingsViewModel  constructor(var settingsRepository: SettingsRepository) :
    BaseViewModel() {

    var loginData = MutableLiveData<LoginData>()


}