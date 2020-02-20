package com.bink.wallet.utils

import com.bink.wallet.network.ApiError
import com.google.gson.Gson
import retrofit2.HttpException

class ApiErrorUtils {
    companion object {
        fun getApiErrorMessage(httpException: HttpException): String {
            return try {
                val apiError = Gson().fromJson(
                    httpException.response()?.errorBody()?.string()?.let { it },
                    ApiError::class.java
                )
                apiError.non_field_errors[0]
            } catch (e: Exception) {
                ""
            }
        }
    }
}