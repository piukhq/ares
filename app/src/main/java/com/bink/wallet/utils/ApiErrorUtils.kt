package com.bink.wallet.utils

import com.bink.wallet.network.ApiError
import com.google.gson.Gson
import retrofit2.HttpException

class ApiErrorUtils {
    companion object {
        const val SERVER_ERROR = 500

        fun getApiErrorMessage(httpException: HttpException, defaultMessage: String): String {
            return try {
                val apiError = Gson().fromJson(
                    httpException.response()?.errorBody()?.string(),
                    ApiError::class.java
                )
                apiError.non_field_errors[0]
            } catch (e: Exception) {
                defaultMessage
            }
        }
    }
}