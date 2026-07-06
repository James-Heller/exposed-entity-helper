plugins {
    `java-library`
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    `maven-publish`
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
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "exposed-entity-helper-runtime"
            from(components["java"])
        }
    }
}
