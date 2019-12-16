package com.bink.wallet.scenes.loyalty_details

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.model.response.membership_plan.MembershipPlan

class VoucherDetailsViewModel : BaseViewModel() {
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var voucher = MutableLiveData<Voucher>()

}