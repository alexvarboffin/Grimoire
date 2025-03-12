plugins {
    id("java-library")
    alias(libs.plugins.kotlinJvm)

    //kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
dependencies{
    implementation("com.akuleshov7:ktoml-core:0.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.0")

}