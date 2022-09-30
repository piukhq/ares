package com.bink.wallet.theme

import android.annotation.SuppressLint
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

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
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) darkThemeColours else lightThemeColours,
        content = content
    )
}