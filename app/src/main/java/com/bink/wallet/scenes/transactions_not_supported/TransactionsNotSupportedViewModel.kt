package com.bink.wallet.scenes.transactions_not_supported

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.utils.enums.LoginStatus

class TransactionsNotSupportedViewModel : BaseViewModel() {
    var membershipCard = MutableLiveData<MembershipCard>()
    var logInType = MutableLiveData<LoginStatus>()
}
