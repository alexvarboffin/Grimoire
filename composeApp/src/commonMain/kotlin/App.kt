import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import di.appModule
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import navigation.PRESET_LIST_ROUTE
import navigation.presetGraph
import org.koin.compose.KoinApplication

@Composable
fun App() {
    PreComposeApp {
        KoinApplication(
            application = {
                modules(listOf(appModule))
            }
        ) {
            MaterialTheme {
                val navigator = rememberNavigator()
                NavHost(
                    navigator = navigator,
                    initialRoute = PRESET_LIST_ROUTE
                ) {
                    presetGraph(navigator)
                }
            }
        }
    }
} 