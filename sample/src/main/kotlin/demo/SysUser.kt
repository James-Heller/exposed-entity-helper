package demo

import kotlinx.datetime.LocalDateTime
import pers.jamestang.exposed.entity.Column
import pers.jamestang.exposed.entity.EnhanceEntity
import pers.jamestang.exposed.entity.Entity

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
