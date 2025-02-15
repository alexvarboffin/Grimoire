import di.appModule
import org.koin.core.context.startKoin

object GrimoireApp {
    fun init() {
        startKoin {
            printLogger()
            modules(appModule)
        }
    }
} 