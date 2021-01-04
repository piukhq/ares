package com.bink.wallet.utils

import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.WalletOrder
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object WalletOrderingUtil {

    fun getSavedWalletOrder(loyaltyCards: ArrayList<Any>): ArrayList<Any> {
        /**
         * We need to keep a reference of all un added cards, this is in the case that the user adds a new loyalty card to their wallet.
         * If this happens, the saved list wont have the new id saved, and therefore wont know where to add it, so we just add it on the bottom.
         */
        val rearrangedCards = ArrayList<Any>()
        val unassignedCards = ArrayList<Any>()
        unassignedCards.addAll(loyaltyCards)
        val allSavedWalletOrders = getSavedWalletOrders()

        for (i in 0 until allSavedWalletOrders.size) {
            if (allSavedWalletOrders[i].userId.equals(getUserEmail())) {

                for (x in 0 until allSavedWalletOrders[i].loyaltyCardIds.size) {
                    for (loyaltyCard in loyaltyCards) {
                        if (getLoyaltyCardId(loyaltyCard) == (allSavedWalletOrders[i].loyaltyCardIds[x])) {
                            rearrangedCards.add(loyaltyCard)
                            unassignedCards.remove(loyaltyCard)
                        }
                    }
                }

            }
        }

        rearrangedCards.addAll(unassignedCards)
        return rearrangedCards
    }

    fun saveLoyaltyWalletOrder(loyaltyCards: ArrayList<Any>) {
        /**
         * Check to see if the current user has saved a custom order, if they have then override their order
         * If they haven't, then make a new WalletOrder and save it.
         */
        val loyaltyCardIds = ArrayList<Long>()
        val allSavedWalletOrders = getSavedWalletOrders()
        val gson = Gson()

        for (loyaltyCard in loyaltyCards) {
            getLoyaltyCardId(loyaltyCard)?.let { loyaltyCardId ->
                loyaltyCardIds.add(loyaltyCardId)
            }
        }

        for (i in 0 until allSavedWalletOrders.size) {
            if (allSavedWalletOrders[i].userId.equals(getUserEmail())) {

                allSavedWalletOrders[i].loyaltyCardIds = loyaltyCardIds
                SharedPreferenceManager.loyaltyWalletOrder = gson.toJson(allSavedWalletOrders)

                return
            }
        }

        allSavedWalletOrders.add(WalletOrder(getUserEmail(), loyaltyCardIds))
        SharedPreferenceManager.loyaltyWalletOrder = gson.toJson(allSavedWalletOrders)
    }

    private fun getSavedWalletOrders(): ArrayList<WalletOrder> {
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<WalletOrder?>?>() {}.type

        var previousLoyaltyWalletOrders: ArrayList<WalletOrder> = arrayListOf()

        SharedPreferenceManager.loyaltyWalletOrder?.let {
            previousLoyaltyWalletOrders = gson.fromJson(it, type)
        }

        return previousLoyaltyWalletOrders
    }

    private fun getLoyaltyCardId(loyaltyCard: Any): Long? =
        when (loyaltyCard) {
            is MembershipCard -> loyaltyCard.id.toLong()
            is MembershipPlan -> loyaltyCard.id.toLong()
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