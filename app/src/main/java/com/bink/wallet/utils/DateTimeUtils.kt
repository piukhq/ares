package com.bink.wallet.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DateTimeUtils {

    companion object {

        private const val ONE_HOUR = 60
        private const val TWO_MINUTES = 2

        fun hasAnHourElapsed(time: Long): Boolean {
            val currentTime = System.currentTimeMillis()
            val difference = currentTime - time
            val minutes = TimeUnit.MILLISECONDS.toMinutes(difference)
            return minutes > ONE_HOUR
        }

        fun haveTwoMinutesElapsed(time: Long): Boolean {
            val difference = System.currentTimeMillis() - time
            val minutes = TimeUnit.MILLISECONDS.toMinutes(difference)
            return minutes > TWO_MINUTES
        }

        fun dateTimeFormatTransactionTime(timeStamp: Long) =
            SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH).format(timeStamp * ONE_THOUSAND).toString()
    }
}