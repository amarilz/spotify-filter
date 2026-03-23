package com.amarildoaliaj.spotifyfreshorcry.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class Shape {

    @Immutable
    data class Spacing(
        val xs: Dp = 4.dp,
        val sm: Dp = 8.dp,
        val md: Dp = 12.dp,
        val lg: Dp = 16.dp,
        val xl: Dp = 24.dp,
        val xxl: Dp = 32.dp,
    )

    @Immutable
    data class Radius(
        val sm: Dp = 6.dp,
        val md: Dp = 12.dp,
        val lg: Dp = 18.dp,
    )

    @Immutable
    data class Elevation(
        val level1: Dp = 1.dp,
        val level2: Dp = 3.dp,
        val level3: Dp = 6.dp,
    )
}
