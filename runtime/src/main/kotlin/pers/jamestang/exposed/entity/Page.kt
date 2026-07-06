package pers.jamestang.exposed.entity

import kotlinx.serialization.Serializable

@Serializable
data class Page<T>(
    val page: Int,
    val size: Int,
    val data: List<T>,
    val total: Long,
    val totalPage: Int
)
