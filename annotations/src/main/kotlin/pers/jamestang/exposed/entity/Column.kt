package pers.jamestang.exposed.entity

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
public annotation class Column(
    val name: String = "",
    val length: Int = 255,
    val unique: Boolean = false,
    val index: Boolean = false,
    val ignore: Boolean = false,
)
