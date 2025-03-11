package com.walhalla.grimoire

import App
import androidx.compose.ui.unit.dp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import di.appModule
import kotlinx.coroutines.NonDisposableHandle.dispose
import moe.tlaster.precompose.PreComposeApp
import org.koin.compose.KoinApplication
import org.koin.dsl.module
import java.awt.AWTEventMulticaster.add
import java.awt.Button
import java.awt.Dialog
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Frame
import java.awt.Label


fun main() = application {
    val windowState = rememberWindowState(
        width = 1024.dp,
        height = 768.dp
    )

//    Window(
//        onCloseRequest = ::exitApplication,
//        title = "Grimoire",
//        //state = windowState,
//        //centered = true
//    ) {
//        App()
//    }


    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        Dialog(Frame(), e.message ?: "Error").apply {
            layout = FlowLayout()
            val label = Label(e.message)
            add(label)
            val button = Button("OK").apply {
                addActionListener { dispose() }
            }
            add(button)
            setSize(300,300)
            isVisible = true
        }
    }

    application {
        //throw Exception("My custom error!")
        Window(
            title = "Multiplatform App",
            state = windowState,
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(350, 600)
            PreComposeApp {
                KoinApplication(
                    application = {
                        //val desktopModule = module {}

                        modules(/*desktopModule +*/ appModule)
                    }
                ) {
                    App()
                }
            }
        }
    }
}