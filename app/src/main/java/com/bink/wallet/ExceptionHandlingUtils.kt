package com.bink.wallet

import com.bink.wallet.utils.enums.HandledException
import retrofit2.HttpException

class ExceptionHandlingUtils {

    /**
     *   This method should be changed in the future to return a wrapper object that contains
     *   the error code, the error title and the error message for the dialog. This should be designed as a base
     *   method in the Base Fragment and then overriden where needed
     */
    
    companion object {
        fun onHttpException(exception: Exception): HandledException {
            return if (exception is HttpException) {
                when (exception.code()) {
                    500 -> HandledException.INTERNAL_SERVER_ERROR
                    503 -> HandledException.SERVICE_UNAVAILABLE
                    404 -> HandledException.NOT_FOUND
                    400 -> HandledException.BAD_REQUEST
                    else -> HandledException.INTERNAL_SERVER_ERROR
                }
            } else {
                HandledException.UNHANDLED
            }
        }
    }
}