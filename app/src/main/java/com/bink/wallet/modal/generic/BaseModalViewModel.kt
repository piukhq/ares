package com.bink.wallet.modal.generic

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel

/**
 */
open class BaseModalViewModel : BaseViewModel() {
    var destinationLiveData: MutableLiveData<Int> = MutableLiveData()
    var toolbarIconLiveData: MutableLiveData<Int> = MutableLiveData()
}