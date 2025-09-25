import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.kotlinMultiplatform)

    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kspCompose)
    alias(libs.plugins.room)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {


    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            //implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.room.runtime)
            implementation(libs.androidx.room.runtime)
//            implementation(libs.sqlite.bundled)
//            implementation(libs.sqlite)


            implementation(libs.kotlinx.serialization.json)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.insert.koin.koin.compose)

            // Okio
            implementation(libs.okio)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)


            // PreCompose
            api(libs.precompose)  // Используем стабильную версию
            api(libs.precompose.viewmodel)
            api(libs.precompose.koin)

            implementation(compose.materialIconsExtended)
            implementation(libs.mpfilepicker)

            // Please do remember to add compose.foundation and compose.animation
            api(compose.animation)
            api(compose.foundation)

            //DataStore
            api(libs.datastore.preferences)
            api(libs.datastore)

            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            // https://mvnrepository.com/artifact/org.json/json
            implementation(libs.json)
            implementation(project(":core:network"))
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            //implementation(libs.androidx.sqlite.bundled.jvm)
            implementation(libs.mpfilepicker)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktoml.core)
            implementation(libs.logback)
            implementation(libs.velocity)

            implementation(libs.room.runtime)
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)
//            implementation(libs.sqlite)

            implementation(project(":lib"))
        }


    }

    // macosX64("native") { // on macOS
    // linuxX64("native") // on Linux
//    mingwX64("native") { // on Windows
//        binaries {
//            executable()
//        }
//    }

}

dependencies {
    add("kspDesktop", libs.room.compiler)

}
//kapt {
//    correctErrorTypes = true
//}
room {
    schemaDirectory("$projectDir/schemas")
}


compose.desktop {

    // Версия для MSI должна быть в формате MAJOR.MINOR.BUILD (максимум 255.255.65535)
    val buildNumber = SimpleDateFormat("HHmmss").format(Date()).toInt() % 65535 // Ограничиваем до 65535
    val appVersion = "1.0.$buildNumber"

    application {
        mainClass = "com.walhalla.grimoire.MainKt"
        //mainClass = "composeApp/src/desktopMain/kotlin/com/walhalla/grimoire/main.kt"

        nativeDistributions {

            windows{
                includeAllModules = true
            }


            //targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            //targetFormats(TargetFormat.Exe)
            targetFormats(TargetFormat.Msi, TargetFormat.Deb)
            modules("jdk.unsupported")
            modules("jdk.unsupported.desktop")


            //targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "com.walhalla.grimoire"
            packageVersion = appVersion
        }
    }
}
