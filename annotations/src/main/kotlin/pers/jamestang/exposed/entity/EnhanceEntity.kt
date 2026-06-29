package pers.jamestang.exposed.entity

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
 open class EnhanceEntity(
     open var id: Int?,
     open var createTime: LocalDateTime,
    open var updateTime: LocalDateTime?,
     open var deleteTime: LocalDateTime?,
     open var deleted: Boolean,
)
