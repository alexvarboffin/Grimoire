package com.walhalla.grimoire

import App
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Grimoire",
    ) {
        App()
    }
}