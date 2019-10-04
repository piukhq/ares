package com.bink.wallet.data

import androidx.room.TypeConverter
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.Account
import com.bink.wallet.model.response.payment_card.BankCard
import com.bink.wallet.model.response.payment_card.Image
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PaymentCardConverters {
    @TypeConverter
    fun fromBankCard(value: BankCard?): String? {
        val gson = Gson()
        val type = object : TypeToken<BankCard?>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toBankCard(value: String): BankCard? {
        val gson = Gson()
        val type = object : TypeToken<BankCard>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromImageList(value: List<Image?>?): String? {
        val gson = Gson()
        val type = object : TypeToken<List<Image?>?>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toImageList(value: String): List<Image?>? {
        val gson = Gson()
        val type = object : TypeToken<List<Image>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromAccount(value: Account?): String? {
        val gson = Gson()
        val type = object : TypeToken<Account?>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toAccount(value: String): Account? {
        val gson = Gson()
        val type = object : TypeToken<Account>() {}.type
        return gson.fromJson(value, type)
    }
}