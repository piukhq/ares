package com.bink.wallet.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DateTimeUtils {

    companion object {

        private const val ONE_HOUR = 60
        private const val TWO_MINUTES = 2
        private const val TWO_HOURS = 120
        private const val TWELVE_HOURS = 720

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

        fun haveTweleveHoursElapsed(time: Long): Boolean {
            val difference = System.currentTimeMillis() - time
            val minutes = TimeUnit.MILLISECONDS.toMinutes(difference)
            return minutes > 720
        }

        fun dateTimeFormatTransactionTime(timeStamp: Long) =
            SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH).format(timeStamp * ONE_THOUSAND).toString()

        fun dateFormatTimeStamp(timeStamp: Long) =
            SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(timeStamp * ONE_THOUSAND).toString()
    }
}