plugins {
    id("java-library")
    alias(libs.plugins.kotlinJvm)

    //kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies{
    implementation(libs.ktoml.core)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.0")

}