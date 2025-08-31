package com.amarildo.spotifyfilter

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.amarildo.spotifyfilter.ui.view.SpotifyFilter

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SpotifyFilter",
    ) {
        SpotifyFilter()
    }
}
