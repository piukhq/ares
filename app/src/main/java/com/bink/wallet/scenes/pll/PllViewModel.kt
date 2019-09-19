package com.bink.wallet.scenes.pll

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard

class PllViewModel: BaseViewModel() {
    var membershipCard = MutableLiveData<MembershipCard>()
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var paymentCards = MutableLiveData<PaymentCard>()
}