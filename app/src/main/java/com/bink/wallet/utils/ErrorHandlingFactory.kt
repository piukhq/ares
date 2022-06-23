package com.bink.wallet.utils

import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException


fun convertToBaseException(throwable: Throwable): BaseException =
    when (throwable) {
        is BaseException -> throwable

        is IOException -> BaseException.toNetworkError(throwable)

        is HttpException -> {
            val response = throwable.response()
            val httpCode = throwable.code()

            if (response?.errorBody() == null) {
                BaseException.toHttpError(
                    httpCode = httpCode,
                    response = response
                )
            }

            val serverErrorResponseBody = try {
                response?.errorBody()?.string() ?: EMPTY_STRING
            } catch (e: Exception) {
                e.toString()
            }

            val serverErrorResponse =
                try {
                    Gson().fromJson(serverErrorResponseBody, ServerErrorResponse::class.java)
                } catch (e: Exception) {
                    ServerErrorResponse()
                }

            if (serverErrorResponse != null) {
                BaseException.toServerError(
                    serverErrorResponse = serverErrorResponse,
                    httpCode = httpCode
                )
            } else {
                BaseException.toHttpError(
                    response = response,
                    httpCode = httpCode
                )
            }
        }

        else -> BaseException.toUnexpectedError(throwable)
    }

class BaseException(
    val errorType: ErrorType,
    val serverErrorResponse: ServerErrorResponse? = null,
    val response: Response<*>? = null,
    cause: Throwable? = null
) : RuntimeException(cause?.message, cause) {

    override val message: String?
        get() = when (errorType) {
            ErrorType.HTTP -> response?.message()

            ErrorType.NETWORK -> cause?.message

            ErrorType.SERVER -> serverErrorResponse?.errors?.getOrNull(0)

            ErrorType.UNEXPECTED -> cause?.message
        }

    companion object {
        fun toHttpError(response: Response<*>?, httpCode: Int) =
            BaseException(
                errorType = ErrorType.HTTP,
                response = response
            )

        fun toNetworkError(cause: Throwable) =
            BaseException(
                errorType = ErrorType.NETWORK,
                cause = cause
            )

        fun toServerError(serverErrorResponse: ServerErrorResponse, httpCode: Int) =
            BaseException(
                errorType = ErrorType.SERVER,
                serverErrorResponse = serverErrorResponse
            )

        fun toUnexpectedError(cause: Throwable) =
            BaseException(
                errorType = ErrorType.UNEXPECTED,
                cause = cause
            )
    }
}


enum class ErrorType {
    NETWORK,
    HTTP,
    SERVER,
    UNEXPECTED
}

data class ServerErrorResponse(
    val message: String? = null,
    val errors: List<String>? = null
)
