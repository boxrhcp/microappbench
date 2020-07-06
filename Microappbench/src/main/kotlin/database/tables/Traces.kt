package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.jodatime.datetime

object Traces: IntIdTable() {
    val traceId = varchar("trace_id", 50)
    val version = varchar("version", 10)
    val start = datetime("start")
    val end = datetime("end")
}