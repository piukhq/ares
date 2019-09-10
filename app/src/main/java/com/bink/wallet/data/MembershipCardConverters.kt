package com.bink.wallet.data

import androidx.room.TypeConverter
import com.bink.wallet.model.response.membership_card.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MembershipCardConverters {
    @TypeConverter
    fun fromCardBalanceList(value: List<CardBalance?>?): String? {
        val gson = Gson()
        val type = object : TypeToken<List<CardBalance?>?>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toCardBalanceList(value: String): List<CardBalance?>? {
        val gson = Gson()
        val type = object : TypeToken<List<CardBalance>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromPaymentCardList(value: List<String>?): String? {
        val gson = Gson()
        val type = object : TypeToken<List<String?>?>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toPaymentCardList(value: String): List<String?>? {
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }


    @TypeConverter
    fun fromCardImageList(value: List<CardImages?>?): String? {
        val gson = Gson()
        val type = object : TypeToken<List<CardImages>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toCardImageList(value: String): List<CardImages?>? {
        val gson = Gson()
        val type = object : TypeToken<List<CardImages?>?>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromCard(value: Card?): String? {
        val gson = Gson()
        val type = object : TypeToken<Card?>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toCard(value: String?): Card? {
        val gson = Gson()
        val type = object : TypeToken<Card?>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromCardStatus(value: CardStatus?): String? {
        val gson = Gson()
        val type = object : TypeToken<CardStatus?>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toCardStatus(value: String?): CardStatus? {
        val gson = Gson()
        val type = object : TypeToken<CardStatus?>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromMembershipTransactionList(value: List<MembershipTransactions?>?): String? {
        val gson = Gson()
        val type = object : TypeToken<List<MembershipTransactions?>?>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toMembershipTransactionList(value: String): List<MembershipTransactions?>? {
        val gson = Gson()
        val type = object : TypeToken<List<MembershipTransactions>>() {}.type
        return gson.fromJson(value, type)
    }
}