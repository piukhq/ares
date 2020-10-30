package com.bink.wallet.scenes.payment_card_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.auth.User
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.loyalty_wallet.ZendeskRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import com.bink.wallet.scenes.settings.UserRepository
import com.bink.wallet.utils.LocalStoreUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class PaymentCardsDetailsViewModel(
    private var paymentWalletRepository: PaymentWalletRepository,
    private var loyaltyWalletRepository: LoyaltyWalletRepository,
    private var zendeskRepository: ZendeskRepository,
    private var userRepository: UserRepository
) :
    BaseViewModel() {

    val paymentCard = MutableLiveData<PaymentCard>()
    val membershipCardData: MutableLiveData<List<MembershipCard>> = MutableLiveData()
    val membershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()
    val linkedPaymentCard = MutableLiveData<PaymentCard>()
    val unlinkedRequestBody = MutableLiveData<ResponseBody>()
    val deleteRequest = MutableLiveData<ResponseBody>()

    private val _loadCardsError = MutableLiveData<Exception>()
    val loadCardsError: LiveData<Exception>
        get() = _loadCardsError

    private val _linkError = MutableLiveData<Pair<Exception, String>>()
    val linkError: LiveData<Pair<Exception, String>>
        get() = _linkError

    private val _unlinkError = MutableLiveData<Exception>()
    val unlinkError: LiveData<Exception>
        get() = _unlinkError

    private var _deleteError = MutableLiveData<Exception>()
    val deleteError: LiveData<Exception>
        get() = _deleteError
    private val _getCardError = MutableLiveData<Exception>()

    val getCardError: LiveData<Exception>
        get() = _getCardError

    private val _userResponse = MutableLiveData<User>()
    val userResponse: LiveData<User>
        get() = _userResponse

    fun linkPaymentCard(cardId: String, membershipPlanId: String) {
        val membershipCard = membershipCardData.value?.firstOrNull { card -> card.id == cardId }
        updatePaymentCard(cardId)
        membershipCard?.let { mCard ->
            paymentCard.value?.let { pCard ->
                paymentWalletRepository.linkPaymentCard(
                    mCard,
                    pCard,
                    _linkError,
                    paymentCard,
                    membershipPlanId
                )
            }

        }
    }

    fun unlinkPaymentCard(cardId: String, paymentCardId: String) {
        val membershipCard = membershipCardData.value?.firstOrNull { card -> card.id == cardId }
        membershipCard?.let { mCard ->
            paymentCard.value?.let { pCard ->
                paymentWalletRepository.unlinkPaymentCard(
                    pCard,
                    mCard,
                    _unlinkError,
                    unlinkedRequestBody,
                    paymentCard
                )
            }

        }

    }

    fun getPaymentCard(id: Int) {
        viewModelScope.launch {
            try {
                val card =
                    withContext(Dispatchers.IO) { paymentWalletRepository.getPaymentCard(id.toString()) }
                paymentCard.value = card
            } catch (e: Exception) {
                _getCardError.value = e
            }
        }
    }

    fun deletePaymentCard(paymentCardId: String) {
        paymentWalletRepository.deletePaymentCard(paymentCardId, deleteRequest, _deleteError)
    }

    fun getMembershipCards() {
        loyaltyWalletRepository.retrieveMembershipCards(membershipCardData, _loadCardsError)
    }

    fun storePaymentCard(card: PaymentCard) {
        paymentWalletRepository.storePaymentCard(card)
    }

    private fun updatePaymentCard(cardId: String) {
        paymentCard.value?.let {
            if (it.membership_cards.count { card -> card.id == cardId } < 1) {
                it.addPaymentCard(cardId)
            }
        }
    }

    fun shouldShowDetailsDialog() = zendeskRepository.shouldShowUserDetailsDialog()

    fun getEmail() = zendeskRepository.getUserEmail()

    fun getFirstName() = zendeskRepository.getUsersFirstName()

    fun getLastName() = zendeskRepository.getUsersLastName()

    fun putUserDetails(user: User) {
        val handler = CoroutineExceptionHandler { _, _ -> //Exception handler to prevent app crash

        }
        viewModelScope.launch(handler) {
            try {
                val returnedUser =
                    withContext(Dispatchers.IO) { userRepository.putUserDetails(user) }

                returnedUser.first_name?.let {
                    LocalStoreUtils.setAppSharedPref(
                        LocalStoreUtils.KEY_FIRST_NAME,
                        it
                    )
                }

                returnedUser.last_name?.let {
                    LocalStoreUtils.setAppSharedPref(
                        LocalStoreUtils.KEY_SECOND_NAME,
                        it
                    )
                }
                _userResponse.value = returnedUser
            } catch (e: Exception) {

            }
        }
    }

}
