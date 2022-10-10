package com.bink.wallet.utils

import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    const val LIGHT_MODE = "Light"
    const val DARK_MODE = "Dark"
    const val BATTERY_SAVER_MODE = "battery"
    const val SYSTEM = "System"

    fun applyTheme(theme: String) {
        when (theme) {
            LIGHT_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            DARK_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            BATTERY_SAVER_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        }
    }

}