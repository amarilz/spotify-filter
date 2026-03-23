package com.amarildoaliaj.spotifyfreshorcry

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.amarildoaliaj.spotifyfreshorcry.ui.view.SpotifyFilter

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SpotifyFilter",
    ) {
        SpotifyFilter()
    }
}
