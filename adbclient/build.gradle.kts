plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.walhalla.adbclient"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.walhalla.adbclient"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }



    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}


dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation("androidx.compose:compose-bom:2025.02.00")

    implementation(libs.material)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    implementation(libs.kotlinx.coroutines.android)
    api(libs.json)
    
    debugImplementation("androidx.compose.ui:ui-tooling")
}