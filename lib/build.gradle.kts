plugins {
    id("java-library")
    alias(libs.plugins.kotlinJvm)

    //kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
}
kotlin {
    jvmToolchain(17)
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies{
    implementation(libs.ktoml.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.velocity)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
}