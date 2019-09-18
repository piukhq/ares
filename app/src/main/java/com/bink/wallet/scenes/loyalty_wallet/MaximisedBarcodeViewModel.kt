package com.bink.wallet.scenes.loyalty_wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.BarcodeWrapper

class MaximisedBarcodeViewModel : BaseViewModel() {
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var barcodeWrapper = MutableLiveData<BarcodeWrapper>()
}