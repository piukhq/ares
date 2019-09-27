package com.bink.wallet.scenes.loyalty_wallet

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.BarcodeWrapper

class BarcodeViewModel : BaseViewModel() {
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var membershipCard = MutableLiveData<MembershipCard>()
    var barcode = MutableLiveData<BarcodeWrapper>()
    var isBarcodeAvailable = MutableLiveData<Boolean>()
}