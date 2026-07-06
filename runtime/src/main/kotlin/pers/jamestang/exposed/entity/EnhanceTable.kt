package pers.jamestang.exposed.entity

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update

abstract class EnhanceTable<T : EnhanceEntity>(
    tableName: String,
) : IntIdTable(name = tableName) {
    val createTime = datetime("create_time")
    val updateTime = datetime("update_time").nullable()
    val deleteTime = datetime("delete_time").nullable()
    val deleted = bool("deleted").default(false)

    abstract suspend fun toEntity(row: ResultRow): T

    protected fun activeWhere(): Op<Boolean> = deleted eq false

    protected fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    suspend fun selectById(id: Int): T? = select(columns)
        .where((this.id eq id) and activeWhere())
        .map { toEntity(it) }
        .firstOrNull()

    suspend fun list(
        where: EnhanceTable<T>.() -> Op<Boolean> = { activeWhere() },
        orderBy: List<Pair<Expression<*>, SortOrder>> = emptyList(),
    ): List<T> {
        val query = selectAll().where(where())

        if (orderBy.isNotEmpty()) {
            query.orderBy(*orderBy.toTypedArray())
        }

        return query
            .map { toEntity(it) }
            .toList()
    }

    suspend fun listAll(): List<T> = list()

    suspend fun count(
        where: EnhanceTable<T>.() -> Op<Boolean> = { activeWhere() },
    ): Long = selectAll()
        .where(where())
        .count()

    suspend fun countAll(): Long = count()

    suspend fun exists(
        where: EnhanceTable<T>.() -> Op<Boolean>,
    ): Boolean = select(this.id)
        .where(where())
        .limit(1)
        .count() > 0

    suspend fun existsById(id: Int): Boolean = select(this.id)
        .where((this.id eq id) and activeWhere())
        .limit(1)
        .count() > 0

    suspend fun page(
        page: Int,
        size: Int,
        where: EnhanceTable<T>.() -> Op<Boolean> = { activeWhere() },
        orderBy: List<Pair<Expression<*>, SortOrder>> = emptyList(),
    ): Page<T> {
        require(page > 0) { "page must be greater than 0." }
        require(size > 0) { "size must be greater than 0." }

        val condition = where()
        val total = count { condition }
        val query = select(columns)
            .where(condition)
            .limit(size)
            .offset((page - 1L) * size)

        if (orderBy.isNotEmpty()) {
            query.orderBy(*orderBy.toTypedArray())
        }

        val data = query
            .map { toEntity(it) }
            .toList()

        val totalPage = ((total + size - 1) / size).toInt()
        return Page(page, size, data, total, totalPage)
    }

    suspend fun updateById(
        id: Int,
        body: EnhanceTable<T>.(UpdateStatement) -> Unit,
    ): Int = update(
        where = { (this.id eq id) and activeWhere() },
        limit = null,
        body = body,
    )

    suspend fun softDeleteById(
        id: Int,
        deleteTime: LocalDateTime = now(),
    ): Int = updateById(id) {
        it[deleted] = true
        it[this.deleteTime] = deleteTime
    }

    suspend fun restoreById(id: Int): Int = update(
        where = { this.id eq id },
        limit = null,
    ) {
        it[deleted] = false
        it[deleteTime] = null
    }
}
