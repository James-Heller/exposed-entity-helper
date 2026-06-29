# expose-entity-helper

KSP processor for generating Exposed `EnhanceTable` objects from annotated Kotlin entity classes.

## Modules

- `annotations`: compile-time annotations used by application code.
- `processor`: KSP processor that generates table objects.
- `sample`: minimal integration sample that verifies generated code against Exposed.

## Usage

Add the annotations dependency to regular compilation and the processor to KSP:

```kotlin
plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":annotations"))
    ksp(project(":processor"))
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
