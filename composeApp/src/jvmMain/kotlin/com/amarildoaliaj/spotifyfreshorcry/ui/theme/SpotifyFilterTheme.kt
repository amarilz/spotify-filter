package com.amarildoaliaj.spotifyfreshorcry.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun SpotifyFilterTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = true, // Android-only; ignorato altrove
    content: @Composable () -> Unit,
) {
//    val dynamic: ColorScheme? = platformDynamicColorScheme(dark = useDarkTheme, enabled = useDynamicColor)
//    val colors = dynamic ?: if (useDarkTheme) Color.darkColors else Color.lightColors

    CompositionLocalProvider(
        LocalSpacing provides Shape.Spacing(),
        LocalRadius provides Shape.Radius(),
        LocalElevation provides Shape.Elevation(),
    ) {
        MaterialTheme(
            colorScheme = Color.darkColors,
            typography = AppTypography,
            content = content,
        )
    }
}
