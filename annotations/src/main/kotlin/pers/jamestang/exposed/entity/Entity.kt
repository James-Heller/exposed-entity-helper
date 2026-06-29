package pers.jamestang.exposed.entity

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class Entity(
    val tableName: String = "",
    val objectName: String = "",
)
