package com.bink.wallet.utils

import java.util.concurrent.TimeUnit

class DateTimeUtils {

    companion object {

        const val ONE_HOUR = 1

        fun hasAnHourElapsed(time: Long): Boolean {
            val currentTime = System.currentTimeMillis()
            //yesterday 1582097420000
            val difference = currentTime - 1582097420000
            val hour = TimeUnit.MILLISECONDS.toHours(difference)
            return hour > ONE_HOUR
        }
    }
}