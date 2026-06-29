plugins {
    kotlin("jvm") version "2.3.20" apply false
    id("com.google.devtools.ksp") version "2.3.0" apply false
}

group = "pers.jamestang"
version = "1.0-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}
