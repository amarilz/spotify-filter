package com.amarildoaliaj.spotifyfreshorcry.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

class Color {

    companion object {
        val mdThemeLightPrimary = Color(0xFF3156E0)
        val mdThemeLightOnprimary = Color(0xFFFFFFFF)
        val mdThemeLightSecondary = Color(0xFF5A6B8A)
        val mdThemeLightBackground = Color(0xFFF8F9FD)
        val mdThemeLightOnbackground = Color(0xFF111318)
        val mdThemeLightSurface = Color(0xFFFFFFFF)
        val mdThemeLightOnsurface = Color(0xFF131316)

        val mdThemeDarkPrimary = Color(0xFFB8C4FF)
        val mdThemeDarkOnprimary = Color(0xFF0F2167)
        val mdThemeDarkSecondary = Color(0xFFB4C2E2)
        val mdThemeDarkBackground = Color(0xFF0E1117)
        val mdThemeDarkOnbackground = Color(0xFFE6E8EE)
        val mdThemeDarkSurface = Color(0xFF141820)
        val mdThemeDarkOnsurface = Color(0xFFE6E8EE)

        val darkColors: ColorScheme = darkColorScheme(
            primary = mdThemeDarkPrimary,
            onPrimary = mdThemeDarkOnprimary,
            secondary = mdThemeDarkSecondary,
            background = mdThemeDarkBackground,
            onBackground = mdThemeDarkOnbackground,
            surface = mdThemeDarkSurface,
            onSurface = mdThemeDarkOnsurface,
        )

        val lightColors: ColorScheme = lightColorScheme(
            primary = mdThemeLightPrimary,
            onPrimary = mdThemeLightOnprimary,
            secondary = mdThemeLightSecondary,
            background = mdThemeLightBackground,
            onBackground = mdThemeLightOnbackground,
            surface = mdThemeLightSurface,
            onSurface = mdThemeLightOnsurface,
        )
    }
}
