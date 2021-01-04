package com.bink.wallet.utils

import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.WalletOrder
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object WalletOrderingUtil {

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