package demo

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.datetime

abstract class EnhanceTable<T : EnhanceEntity>(name: String) : IntIdTable(name) {
    val createTime = datetime("create_time")
    val updateTime = datetime("update_time").nullable()
    val deleteTime = datetime("delete_time").nullable()
    val deleted = bool("deleted")

    abstract fun toEntity(row: ResultRow): T
}
