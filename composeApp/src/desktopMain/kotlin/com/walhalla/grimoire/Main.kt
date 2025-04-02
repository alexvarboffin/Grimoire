package com.walhalla.grimoire

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application


import androidx.compose.ui.window.rememberWindowState
import com.walhalla.grimoire.utils.AppLogger
import di.appModule
import moe.tlaster.precompose.PreComposeApp
import org.koin.compose.KoinApplication
import org.koin.dsl.module
import java.awt.Button
import java.awt.Dialog
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Frame
import java.awt.Label
import java.awt.TextArea

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { _, e ->

        AppLogger.error("Error occurred", e)

        Dialog(Frame(), e.message ?: "Error").apply {
            layout = FlowLayout()
            val label = TextArea(e.message)
            add(label)
            val button = Button("OK").apply {
                addActionListener { dispose() }
            }
            add(button)
            setSize(500, 500)
            isVisible = true
        }
    }

    application {
        //throw Exception("My custom error!")
        Window(
            title = "Grimoire",
            state = rememberWindowState(
                width = 1024.dp,
                height = 768.dp
            ),
            onCloseRequest = ::exitApplication
        ) {

            AppLogger.debug("Debug message....")

            //window.minimumSize = Dimension(350, 600)
            PreComposeApp {
                KoinApplication(
                    application = {
                        val desktopModule = module {}
                        modules(desktopModule + appModule)
                    }
                ) {
                    App()
                }
            }
        }
    }
}
//fun main() = application {
//
//
//    // Пример использования:
//    AppLogger.debug("Debug message")
//    AppLogger.info("Info message")
//
//
//    try {
//        Thread.setDefaultUncaughtExceptionHandler { _, e ->
//            Dialog(Frame(), e.message ?: "Error").apply {
//                layout = FlowLayout()
//                val label = Label(e.message)
//                add(label)
//                val button = Button("OK").apply {
//                    addActionListener { dispose() }
//                }
//                add(button)
//                setSize(300, 300)
//                isVisible = true
//            }
//        }
//
//
//    } catch (e: Exception) {
//        AppLogger.error("Error occurred", e)
//    }

//
//}