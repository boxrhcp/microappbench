package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Traces : IntIdTable() {
    val traceId = varchar("trace_id", 50)
    val version = varchar("version", 10)
    val traceUrl = varchar("trace_url", 100)
    val traceMethod = varchar("trace_method", 10)
    val headerId = varchar("header_id", 100)
    val start = long("start")
    val end = long("end")
    val duration = long("duration")
}