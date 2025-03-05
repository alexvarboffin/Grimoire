package com.walhalla.grimoire

import App
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState


fun main() = application {
    val windowState = rememberWindowState(
        width = 1024.dp,
        height = 768.dp
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "Grimoire",
        state = windowState,
        //centered = true
    ) {
        App()
    }
}