import Const.DATA_STORE_FILE_NAME
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences


import di.appModule
import di.provideDatabase
import domain.repository.AdbRepository
import kotlinx.coroutines.internal.synchronized
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import navigation.NavGraph.TOOLS_ROUTE
import navigation.mainGraph
import okio.Path.Companion.toPath
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.module
import theme.ThemeManager






@Composable
fun App() {

    PreComposeApp {
        KoinApplication(
            application = {
                val desktopModule = module {


                }

                modules(desktopModule + appModule)
            }
        ) {
            val themeManager: ThemeManager = koinInject()
            val isDarkTheme by themeManager.isDarkTheme.collectAsState()

            MaterialTheme(
                colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
            ) {
                val navigator = rememberNavigator()
                NavHost(
                    navigator = navigator,
                    initialRoute = TOOLS_ROUTE
                ) {
                    mainGraph(navigator)
                }
            }
        }
    }
}


expect fun provideDataStore() :DataStore<Preferences>
