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
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            
            implementation(libs.room.runtime)
            implementation(libs.kotlinx.serialization.json)
            
            // Koin
            implementation(libs.koin.core)
            implementation(libs.insert.koin.koin.compose)
            
            // Okio
            implementation("com.squareup.okio:okio:3.9.0")
            
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            // Please do remember to add compose.foundation and compose.animation
            api(compose.foundation)
            api(compose.animation)

            // PreCompose
            api("moe.tlaster:precompose:1.5.10")  // Используем стабильную версию
            api("moe.tlaster:precompose-viewmodel:1.5.10")
            api("moe.tlaster:precompose-koin:1.5.10")

            implementation(compose.materialIconsExtended)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.androidx.sqlite.bundled.jvm)
        }
    }
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
    application {
        mainClass = "com.walhalla.grimoire.MainKt"
        //mainClass = "composeApp/src/desktopMain/kotlin/com/walhalla/grimoire/main.kt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.walhalla.grimoire"
            packageVersion = "1.0.0"
        }
    }
}
