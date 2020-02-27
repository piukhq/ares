package com.bink.wallet.utils

import java.util.concurrent.TimeUnit

class DateTimeUtils {

    companion object {

        private const val ONE_HOUR = 1
        private const val TWO_MINUTES = 2

        fun hasAnHourElapsed(time: Long): Boolean {
            val currentTime = System.currentTimeMillis()
            val difference = currentTime - time
            val hour = TimeUnit.MILLISECONDS.toHours(difference)
            return hour > ONE_HOUR
        }

        fun haveTwoMinutesElapsed(time: Long): Boolean {
            val difference = System.currentTimeMillis() - time
            val minutes = TimeUnit.MILLISECONDS.toMinutes(difference)
            return minutes > TWO_MINUTES
        }
    }
}