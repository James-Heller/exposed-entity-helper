plugins {
    `java-library`
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    implementation(project(":runtime"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.0")
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
            artifactId = "exposed-entity-helper-processor"
            from(components["java"])
        }
    }
}
