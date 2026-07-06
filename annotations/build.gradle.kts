plugins {
    `java-library`
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    api("org.jetbrains.exposed:exposed-core:1.3.0")
    api("org.jetbrains.exposed:exposed-r2dbc:1.3.0")
    api("org.jetbrains.exposed:exposed-kotlin-datetime:1.3.0")
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
}

kotlin {
    jvmToolchain(25)
}
