package com.bink.wallet.data

import androidx.room.TypeConverter
import com.bink.wallet.scenes.browse_brands.model.Account
import com.bink.wallet.scenes.browse_brands.model.Balances
import com.bink.wallet.scenes.browse_brands.model.FeatureSet
import com.bink.wallet.scenes.browse_brands.model.Images
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MembershipPlanConverters {
    @TypeConverter
    fun fromBalanceList(value: List<Balances>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Balances>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toBalanceList(value: String): List<Balances> {
        val gson = Gson()
        val type = object : TypeToken<List<Balances>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromImagesList(value: List<Images>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Images>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toImagesList(value: String): List<Images> {
        val gson = Gson()
        val type = object : TypeToken<List<Images>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromFeatureSet(value: FeatureSet): String {
        val gson = Gson()
        val type = object : TypeToken<FeatureSet>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toFeatureSet(value: String): FeatureSet {
        val gson = Gson()
        val type = object : TypeToken<FeatureSet>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromAccount(value: Account): String {
        val gson = Gson()
        val type = object : TypeToken<Account>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toAccount(value: String): Account {
        val gson = Gson()
        val type = object : TypeToken<Account>() {}.type
        return gson.fromJson(value, type)
    }
}