package com.walhalla.grimoire

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue


import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import navigation.NavGraph.TOOLS_ROUTE
import navigation.mainGraph
import org.koin.compose.koinInject
import theme.ThemeManager


@Composable
fun App() {

    val themeManager: ThemeManager = koinInject()
    val isDarkTheme by themeManager.isDarkTheme.collectAsState()

    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        println("MaterialTheme initialized")
        val navigator = rememberNavigator()
        NavHost(
            navigator = navigator,
            initialRoute = TOOLS_ROUTE
        ) {
            mainGraph(navigator)
        }
    }
}
