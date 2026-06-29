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
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(files("libs/expose-entity-helper-1.0-SNAPSHOT.jar"))
    ksp(files("libs/expose-entity-helper-1.0-SNAPSHOT.jar"))
}
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

By default the generated table extends `EnhanceTable`. If your base table class has a fully qualified name, pass it as a KSP option:

```kotlin
ksp {
    arg("exposedEntityHelper.enhanceTableClass", "com.example.EnhanceTable")
}
```
