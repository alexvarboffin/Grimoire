package di

import domain.io.FileSystem
import org.koin.dsl.module

val desktopModule = module {
    single { FileSystem() }
    single<domain.push.FcmService> { domain.push.DesktopFcmService(get(), get()) }
}
