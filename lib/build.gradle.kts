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
    implementation(libs.ktoml.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.core)

}