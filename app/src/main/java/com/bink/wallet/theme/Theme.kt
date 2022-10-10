package com.bink.wallet.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.bink.wallet.R
import com.bink.wallet.utils.ThemeHelper
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@SuppressLint("ConflictingOnColor")
private val lightThemeColours = lightColors(
    primary = BinkPrimary,
    background = White,
    surface = White,
    onSurface = Black
)

private val darkThemeColours = darkColors(
    primary = BinkPrimary,
    background = DarkThemeBackGround,
    surface = DarkThemeColourSurface,
    onSurface = White
)

@Composable
fun AppTheme(
    theme: String,
    content: @Composable () -> Unit
) {

    val color = when (theme) {
        ThemeHelper.DARK_MODE -> true
        ThemeHelper.LIGHT_MODE -> false
        else -> isSystemInDarkTheme()

    }
    MaterialTheme(
        colors = if (color) darkThemeColours else lightThemeColours,
        content = content
    )

    val systemUiController = rememberSystemUiController()
        if (color) {
            systemUiController.setStatusBarColor(darkThemeColours.background)
        }

}