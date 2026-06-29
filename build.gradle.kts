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

tasks.register<Jar>("helperJar") {
    group = "build"
    description = "Builds a single jar containing both annotations and the KSP processor."
    archiveBaseName.set(rootProject.name)
    archiveVersion.set(project.version.toString())
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    dependsOn(":annotations:classes", ":processor:classes")
    from(project(":annotations").layout.buildDirectory.dir("classes/kotlin/main"))
    from(project(":processor").layout.buildDirectory.dir("classes/kotlin/main"))
    from(project(":processor").layout.buildDirectory.dir("resources/main"))
}
