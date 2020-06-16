package database.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object BenchmarkResultObject : IntIdTable() {
    val resource : Column<String> = varchar("resource", 50)
    val pattern : Column<String> = varchar("pattern", 50)
    val requestId : Column<Int> = integer("request_id")
    val start : Column<Long> = long("start")
    val end : Column<Long> = long("end")
}