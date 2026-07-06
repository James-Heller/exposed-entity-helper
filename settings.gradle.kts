plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "exposed-entity-helper"

include("runtime")
include("processor")
include("sample")
