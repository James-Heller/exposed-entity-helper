package demo

import kotlinx.datetime.LocalDateTime

abstract class EnhanceEntity(
    open var id: Int?,
    open var createTime: LocalDateTime,
    open var updateTime: LocalDateTime?,
    open var deleteTime: LocalDateTime?,
    open var deleted: Boolean,
)
