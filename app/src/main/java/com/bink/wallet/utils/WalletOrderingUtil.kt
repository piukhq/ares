package com.bink.wallet.utils

import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.JoinCardItem
import com.bink.wallet.model.WalletOrder
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object  WalletOrderingUtil {

    fun getSavedPaymentCardWallet(paymentCards: ArrayList<Any>): ArrayList<Any> {
        val rearrangedCards = ArrayList<Any>()
        val unassignedCards = ArrayList<Any>()
        unassignedCards.addAll(paymentCards)
        val allSavedWalletOrders = getSavedPaymentWalletOrder()

        for (i in 0 until allSavedWalletOrders.size) {
            if (allSavedWalletOrders[i].userId.equals(getUserEmail())) {

                if (!allSavedWalletOrders[i].cardIds.isNullOrEmpty()) {
                    for (x in 0 until allSavedWalletOrders[i].cardIds.size) {
                        for (paymentCard in paymentCards) {
                            if (getPaymentCardId(paymentCard) == (allSavedWalletOrders[i].cardIds[x])) {
                                rearrangedCards.add(paymentCard)
                                unassignedCards.remove(paymentCard)
                            }
                        }
                    }
                }

            }
        }

        unassignedCards.addAll(rearrangedCards)
        unassignedCards.add(JoinCardItem())
        return unassignedCards
    }

    fun getSavedPaymentCardWalletForPll(paymentCards: List<PaymentCard>): ArrayList<PaymentCard> {
        val rearrangedCards = ArrayList<PaymentCard>()
        val unassignedCards = ArrayList<PaymentCard>()
        unassignedCards.addAll(paymentCards)
        val allSavedWalletOrders = getSavedPaymentWalletOrder()

        for (i in 0 until allSavedWalletOrders.size) {
            if (allSavedWalletOrders[i].userId.equals(getUserEmail())) {

                if (!allSavedWalletOrders[i].cardIds.isNullOrEmpty()) {
                    for (x in 0 until allSavedWalletOrders[i].cardIds.size) {
                        for (paymentCard in paymentCards) {
                            if (getPaymentCardId(paymentCard) == (allSavedWalletOrders[i].cardIds[x])) {
                                rearrangedCards.add(paymentCard)
                                unassignedCards.remove(paymentCard)
                            }
                        }
                    }
                }

            }
        }

        unassignedCards.addAll(rearrangedCards)
        return unassignedCards
    }

    fun getSavedLoyaltyCardWallet(loyaltyCards: ArrayList<Any>): ArrayList<Any> {
        val rearrangedCards = ArrayList<Any>()
        val unassignedCards = ArrayList<Any>()
        unassignedCards.addAll(loyaltyCards)
        val allSavedWalletOrders = getSavedLoyaltyWalletOrder()

        for (i in 0 until allSavedWalletOrders.size) {
            if (allSavedWalletOrders[i].userId.equals(getUserEmail())) {

                if (!allSavedWalletOrders[i].cardIds.isNullOrEmpty()) {
                    for (x in 0 until allSavedWalletOrders[i].cardIds.size) {
                        for (loyaltyCard in loyaltyCards) {
                            if (getLoyaltyCardId(loyaltyCard) == (allSavedWalletOrders[i].cardIds[x])) {
                                rearrangedCards.add(loyaltyCard)
                                unassignedCards.remove(loyaltyCard)
                            }
                        }
                    }
                }
            }
        }

        /**
         * This ensures that when a user deletes a card that comes with a plan, the plan goes underneath the other cards the user has in their wallet.
         * It then returns the array in the correct order: New cards -> Arranged Cards -> Membership/join plans
         */
        unassignedCards.addAll(rearrangedCards)

        val sortedMembershipCards = ArrayList<Any>()
        val sortedMembershipPlans = ArrayList<Any>()

        for(card in unassignedCards){
            if(card is MembershipCard) {
                sortedMembershipCards.add(card)
            } else {
                sortedMembershipPlans.add(card)
            }
        }

        sortedMembershipCards.addAll(sortedMembershipPlans)

        //Unsure why the below doesnt work. Without converting it to an Arraylist it will work. We ofc can't use a List for this.
        //val sortedCards = (unassignedCards.sortedByDescending { it is MembershipCard }) as ArrayList<Any>

        return sortedMembershipCards
    }

    fun setSavedPaymentCardWallet(paymentCards: ArrayList<Any>) {
        /**
         * Check to see if the current user has saved a custom order, if they have then override their order
         * If they haven't, then make a new WalletOrder and save it.
         */
        val paymentCardIds = ArrayList<Long>()
        val allSavedWalletOrders = getSavedPaymentWalletOrder()
        val gson = Gson()

        for (paymentCard in paymentCards) {
            getPaymentCardId(paymentCard)?.let { paymentCardId ->
                paymentCardIds.add(paymentCardId)
            }
        }

        for (i in 0 until allSavedWalletOrders.size) {
            if (allSavedWalletOrders[i].userId.equals(getUserEmail())) {

                allSavedWalletOrders[i].cardIds = paymentCardIds
                SharedPreferenceManager.paymentWalletOrder = gson.toJson(allSavedWalletOrders)

                return
            }
        }

        allSavedWalletOrders.add(WalletOrder(getUserEmail(), paymentCardIds))
        SharedPreferenceManager.paymentWalletOrder = gson.toJson(allSavedWalletOrders)
    }

    fun setSavedLoyaltyCardWallet(loyaltyCards: ArrayList<Any>) {
        val loyaltyCardIds = ArrayList<Long>()
        val allSavedWalletOrders = getSavedLoyaltyWalletOrder()
        val gson = Gson()

        for (loyaltyCard in loyaltyCards) {
            getLoyaltyCardId(loyaltyCard)?.let { loyaltyCardId ->
                loyaltyCardIds.add(loyaltyCardId)
            }
        }

        for (i in 0 until allSavedWalletOrders.size) {
            if (allSavedWalletOrders[i].userId.equals(getUserEmail())) {

                allSavedWalletOrders[i].cardIds = loyaltyCardIds
                SharedPreferenceManager.loyaltyWalletOrder = gson.toJson(allSavedWalletOrders)

                return
            }
        }

        allSavedWalletOrders.add(WalletOrder(getUserEmail(), loyaltyCardIds))
        SharedPreferenceManager.loyaltyWalletOrder = gson.toJson(allSavedWalletOrders)
    }


    private fun getSavedLoyaltyWalletOrder(): ArrayList<WalletOrder> {
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<WalletOrder?>?>() {}.type

        var previousLoyaltyWalletOrders: ArrayList<WalletOrder> = arrayListOf()

        SharedPreferenceManager.loyaltyWalletOrder?.let {
            previousLoyaltyWalletOrders = gson.fromJson(it, type)
        }

        return previousLoyaltyWalletOrders
    }

    private fun getSavedPaymentWalletOrder(): ArrayList<WalletOrder> {
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<WalletOrder?>?>() {}.type

        var previousPaymentWalletOrders: ArrayList<WalletOrder> = arrayListOf()

        SharedPreferenceManager.paymentWalletOrder?.let {
            previousPaymentWalletOrders = gson.fromJson(it, type)
        }

        return previousPaymentWalletOrders
    }

    private fun getLoyaltyCardId(loyaltyCard: Any): Long? =
        when (loyaltyCard) {
            is MembershipCard -> loyaltyCard.id.toLong()
            is MembershipPlan -> loyaltyCard.id.toLong()
            else -> null
        }

    private fun getPaymentCardId(paymentCard: Any): Long? =
        when (paymentCard) {
            is PaymentCard -> paymentCard.id?.toLong()
            else -> null
        }

    private fun getUserEmail(): String {
        var userEmail = ""
        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_EMAIL)?.let { safeEmail ->
            userEmail = safeEmail
        }

        return userEmail
    }

}