# exposed-entity-helper

KSP-based helpers for generating Exposed R2DBC table objects from Kotlin entity classes.

## Modules

- `runtime`: annotations, base entity/table classes, pagination model, and common R2DBC helpers.
- `processor`: KSP processor that generates Exposed table objects.
- `sample`: integration sample that verifies generated code against Exposed.

## Build

Requires JDK 25 or newer.

Build and verify all modules:

```shell
./gradlew build
```

Build a single local helper jar containing both runtime APIs and the processor:

```shell
./gradlew helperJar
```

The jar is written to:

```text
build/libs/exposed-entity-helper-0.1.0-SNAPSHOT.jar
```

You can also publish split artifacts to Maven Local:

```shell
./gradlew publishToMavenLocal
```

## Usage

For the single jar workflow, copy the helper jar into your application and add the same jar to regular compilation and KSP:

```kotlin
plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(files("libs/exposed-entity-helper-0.1.0-SNAPSHOT.jar"))
    ksp(files("libs/exposed-entity-helper-0.1.0-SNAPSHOT.jar"))

    implementation("org.jetbrains.exposed:exposed-core:1.3.0")
    implementation("org.jetbrains.exposed:exposed-r2dbc:1.3.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
}
```

For Maven Local split artifacts:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("pers.jamestang.exposed:exposed-entity-helper-runtime:0.1.0-SNAPSHOT")
    ksp("pers.jamestang.exposed:exposed-entity-helper-processor:0.1.0-SNAPSHOT")
}
```

## Entity Example

```kotlin
@Entity(tableName = "sys_user")
data class SysUser(
    override var id: Int?,
    @param:Column(length = 32, unique = true)
    var username: String,
    @param:Column(length = 255)
    var password: String,
    @param:Column(length = 11)
    var mobile: String?,
    @param:Column(length = 255)
    var email: String?,
    override var createTime: LocalDateTime,
    override var updateTime: LocalDateTime?,
    override var deleteTime: LocalDateTime?,
    override var deleted: Boolean,
) : EnhanceEntity(id, createTime, updateTime, deleteTime, deleted)
```

The generated `SysUsers` object declares columns, maps `ResultRow` back to the entity, and includes:

```kotlin
SysUsers.insert(user)
SysUsers.update(user)
SysUsers.selectById(id)
SysUsers.listAll()
SysUsers.list(where = { username eq "admin" })
SysUsers.countAll()
SysUsers.existsById(id)
SysUsers.page(page = 1, size = 20)
SysUsers.softDeleteById(id)
SysUsers.restoreById(id)
```

By default generated tables extend `pers.jamestang.exposed.entity.EnhanceTable`. To use a custom base table, pass:

```kotlin
ksp {
    arg("exposedEntityHelper.enhanceTableClass", "com.example.EnhanceTable")
}
```
