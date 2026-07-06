# expose-entity-helper

KSP processor for generating Exposed `EnhanceTable` objects from annotated Kotlin entity classes.

## Modules

- `annotations`: compile-time annotations used by application code.
- `processor`: KSP processor that generates table objects.
- `sample`: minimal integration sample that verifies generated code against Exposed.

## Build

Build the single helper jar:

```shell
./gradlew helperJar
```

The jar is written to:

```text
build/libs/expose-entity-helper-1.0-SNAPSHOT.jar
```

## Usage

Copy the helper jar into your application, then add the same jar to regular compilation and KSP:

```kotlin
plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(files("libs/expose-entity-helper-1.0-SNAPSHOT.jar"))
    ksp(files("libs/expose-entity-helper-1.0-SNAPSHOT.jar"))

    implementation("org.jetbrains.exposed:exposed-core:1.3.0")
    implementation("org.jetbrains.exposed:exposed-r2dbc:1.3.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
}
```

The helper jar includes:

- `pers.jamestang.exposed.entity.EnhanceEntity`
- `pers.jamestang.exposed.entity.EnhanceTable`
- `pers.jamestang.exposed.entity.Entity`
- `pers.jamestang.exposed.entity.Column`
- the KSP processor service registration

When adding shortcut methods inside `EnhanceTable`, use the R2DBC Exposed extensions:

```kotlin
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update
```

Annotate your entity:

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

The generated `SysUsers` object skips common `EnhanceEntity` fields for column declarations, but still maps them in `toEntity(row)`.

Generated table objects include entity-specific write helpers:

```kotlin
val id: Int = SysUsers.insert(user)
val affected: Int = SysUsers.update(user)
```

All generated table objects also inherit common R2DBC helpers:

```kotlin
SysUsers.selectById(id)
SysUsers.listAll()
SysUsers.list(where = { username eq "admin" })
SysUsers.countAll()
SysUsers.existsById(id)
SysUsers.page(page = 1, size = 20)
SysUsers.softDeleteById(id)
SysUsers.restoreById(id)
```

By default the generated table extends `EnhanceTable`. If your base table class has a fully qualified name, pass it as a KSP option:

```kotlin
ksp {
    arg("exposedEntityHelper.enhanceTableClass", "com.example.EnhanceTable")
}
```
