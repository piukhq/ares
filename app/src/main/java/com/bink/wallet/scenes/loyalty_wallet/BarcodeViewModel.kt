package com.bink.wallet.scenes.loyalty_wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.BarcodeWrapper

class BarcodeViewModel : BaseViewModel() {
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var barcode = MutableLiveData<BarcodeWrapper>()
    var isBarcodeAvailable = MutableLiveData<Boolean>()
    var isMaximized = MutableLiveData<Boolean>()
}