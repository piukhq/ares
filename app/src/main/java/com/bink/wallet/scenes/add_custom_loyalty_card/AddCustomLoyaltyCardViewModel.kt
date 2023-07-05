package com.bink.wallet.scenes.add_custom_loyalty_card

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.DataStoreSourceImpl
import com.bink.wallet.model.response.membership_card.Card
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.ColourPalette
import com.bink.wallet.utils.ThemeHelper
import kotlinx.coroutines.launch
import java.util.*

class AddCustomLoyaltyCardViewModel(
    private val dataStoreSource: DataStoreSourceImpl,
    private val loyaltyWalletRepository: LoyaltyWalletRepository
) : BaseViewModel() {

    private val _theme = mutableStateOf(ThemeHelper.SYSTEM)
    val theme: MutableState<String>
        get() = _theme

    private val _navigateToLcd = MutableLiveData<MembershipCard>()
    val navigateToLcd: LiveData<MembershipCard>
        get() = _navigateToLcd

    var cardNumber by mutableStateOf("")
        private set

    var storeName by mutableStateOf("")
        private set

    fun getSelectedTheme() {
        viewModelScope.launch {
            dataStoreSource.getCurrentlySelectedTheme().collect {
                _theme.value = it
            }
        }
    }

    fun createMembershipCard() {
        viewModelScope.launch {
            try {
                val membershipCard = generateCustomCard(cardNumber, storeName)
                loyaltyWalletRepository.addCustomCardToDatabase(membershipCard)
                _navigateToLcd.value = membershipCard
            } catch (e: Exception) {
            }

        }
    }

    fun updateCardNumber(number: String) {
        cardNumber = number
    }

    fun updateStoreName(store: String) {
        storeName = store
    }

    private fun generateCustomCard(cardNumber: String, storeName: String): MembershipCard {

        val card = Card(
            barcode = cardNumber,
            null,
            cardNumber,
            ColourPalette.getRandomColour(),
            null,
            storeName
        )

        return MembershipCard(
            id = generateCustomCardId(), "9999", null, null, card, null,
            null, null, null, null, UUID.randomUUID().toString(), null, true
        )
    }

    private fun generateCustomCardId(): String {
        val id = (2000..18000).random() + System.currentTimeMillis().toInt()
        return id.toString()
    }
}