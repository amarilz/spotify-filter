package com.amarildo.spotifyfilter.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSpacing = staticCompositionLocalOf { Shape.Spacing() }
val LocalRadius = staticCompositionLocalOf { Shape.Radius() }
val LocalElevation = staticCompositionLocalOf { Shape.Elevation() }

object AppTheme {
    val spacing: Shape.Spacing
        @Composable get() = LocalSpacing.current
    val radius: Shape.Radius
        @Composable get() = LocalRadius.current
    val elevation: Shape.Elevation
        @Composable get() = LocalElevation.current
}
