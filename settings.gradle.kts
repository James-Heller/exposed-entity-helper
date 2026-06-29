plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "expose-entity-helper"

include("annotations")
include("processor")
include("sample")
