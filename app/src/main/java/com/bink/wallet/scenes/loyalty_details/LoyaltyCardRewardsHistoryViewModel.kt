package com.bink.wallet.scenes.loyalty_details

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.enums.VoucherStates

class LoyaltyCardRewardsHistoryViewModel :
    BaseViewModel() {
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var membershipCard = MutableLiveData<MembershipCard>()

    fun getFilteredVouchers(): List<Voucher>? {
        membershipCard.value?.vouchers?.filterNot {
            listOf(
                VoucherStates.IN_PROGRESS.state,
                VoucherStates.ISSUED.state
            ).contains(it.state)
        }?.sortedByDescending {
            if ((it.date_redeemed ?: 0L) != 0L) {
                it.date_redeemed
            } else {
                it.expiry_date
            }
        }?.let { vouchers ->
            return vouchers
        }

        return null
    }

}