package com.bink.wallet.scenes.add_custom_loyalty_card

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.DataStoreSourceImpl
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.ThemeHelper
import kotlinx.coroutines.launch

class AddCustomLoyaltyCardViewModel(
    private val dataStoreSource: DataStoreSourceImpl,
    private val loyaltyWalletRepository: LoyaltyWalletRepository
) : BaseViewModel() {

    private val _theme = mutableStateOf(ThemeHelper.SYSTEM)
    val theme: MutableState<String>
        get() = _theme

    fun getSelectedTheme() {
        viewModelScope.launch {
            dataStoreSource.getCurrentlySelectedTheme().collect {
                _theme.value = it
            }
        }
    }

    fun createMembershipCard(membershipCard: MembershipCard) {
        loyaltyWalletRepository.storeMembershipCard(membershipCard)
    }
}