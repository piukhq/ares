package com.bink.wallet.scenes.loyalty_details

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.DataStoreSourceImpl
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_card.Voucher
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.ThemeHelper
import com.bink.wallet.utils.enums.VoucherStates
import kotlinx.coroutines.launch

class LoyaltyCardRewardsHistoryViewModel(private val dataStoreSource: DataStoreSourceImpl) :
    BaseViewModel() {
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var membershipCard = MutableLiveData<MembershipCard>()
    private val _theme = mutableStateOf(ThemeHelper.SYSTEM)
    val theme: MutableState<String>
        get() = _theme

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

    fun getSelectedTheme() {
        viewModelScope.launch {
            dataStoreSource.getCurrentlySelectedTheme().collect {
                _theme.value = it
            }
        }
    }

}