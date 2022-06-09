package com.bink.wallet.scenes.loyalty_wallet.barcode

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel

class BarcodeViewModel : BaseViewModel() {
    var shouldShowLabel = MutableLiveData<Boolean>()
}