package com.bink.wallet.data

import androidx.room.TypeConverter
import com.bink.wallet.model.LoginData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoginDataConverters {
    @TypeConverter
    fun fromLoginData(value: LoginData?): String? {
        val gson = Gson()
        val type = object : TypeToken<LoginData?>() {}.type
        return gson.toJson(value, type)
    }
    @TypeConverter
    fun toLoginData(value: String?): LoginData? {
        val gson = Gson()
        val type = object : TypeToken<LoginData?>() {}.type
        return gson.fromJson(value, type)
    }
}