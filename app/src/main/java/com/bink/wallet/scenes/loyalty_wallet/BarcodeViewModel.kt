package com.bink.wallet.scenes.loyalty_wallet

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.BarcodeWrapper

class BarcodeViewModel : BaseViewModel() {
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var membershipCard = MutableLiveData<MembershipCard>()
    var barcode = MutableLiveData<BarcodeWrapper>()

    val cardNumber = ObservableField<String>()
    val barcodeNumber = ObservableField<String>()

    var isBarcodeAvailable = ObservableBoolean(false)
    var isCardNumberAvailable = ObservableBoolean(false)
    var shouldShowLabel = MutableLiveData<Boolean>()

}