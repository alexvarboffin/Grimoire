import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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

            api(libs.room.runtime)
            api(libs.androidx.room.runtime)
            api(libs.sqlite.bundled)
            api(libs.sqlite)


            implementation(libs.kotlinx.serialization.json)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.insert.koin.koin.compose)

            // Okio
            implementation("com.squareup.okio:okio:3.9.0")

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)


            // PreCompose
            api("moe.tlaster:precompose:1.5.10")  // Используем стабильную версию
            api("moe.tlaster:precompose-viewmodel:1.5.10")
            api("moe.tlaster:precompose-koin:1.5.10")

            implementation(compose.materialIconsExtended)
            implementation(libs.mpfilepicker)

            // Please do remember to add compose.foundation and compose.animation
            api(compose.animation)
            api(compose.foundation)


            //DataStore
            api(libs.datastore.preferences)
            api(libs.datastore)


            //Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
            // https://mvnrepository.com/artifact/org.json/json
            api(libs.json)

        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.androidx.sqlite.bundled.jvm)
            implementation(libs.mpfilepicker)
            implementation(libs.ktor.client.okhttp)

            implementation("com.akuleshov7:ktoml-core:0.5.2")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.0")
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

    val majorVersion = 1
    val minorVersion = 3
    val patchVersion = 35
    //val versionSuffix = "beta.1"
    val versionSuffix = ""//"" для стабильной версии

    val _versionName = if (versionSuffix.isNotBlank()) {
        "$majorVersion.$minorVersion.$patchVersion-$versionSuffix"
    } else {
        "$majorVersion.$minorVersion.$patchVersion"
    }

    val _versionCode = majorVersion * 10000 + minorVersion * 100 + patchVersion

    application {
        mainClass = "com.walhalla.grimoire.MainKt"
        //mainClass = "composeApp/src/desktopMain/kotlin/com/walhalla/grimoire/main.kt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.walhalla.grimoire"
            packageVersion = "$_versionName"
        }
    }
}
