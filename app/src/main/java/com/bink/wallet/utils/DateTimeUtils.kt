package com.bink.wallet.utils

import java.util.concurrent.TimeUnit

class DateTimeUtils {

    companion object {

        private const val ONE_HOUR = 60

        fun hasAnHourElapsed(time: Long): Boolean {
            val currentTime = System.currentTimeMillis()
            val difference = currentTime - time
            val minutes = TimeUnit.MILLISECONDS.toMinutes(difference)
            return minutes > ONE_HOUR
        }
    }
}