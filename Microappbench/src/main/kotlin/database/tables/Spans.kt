package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.jodatime.datetime

object Spans: IntIdTable() {
    val spanId = varchar("span_id", 50)
    val traceId = integer("trace_id").references(Traces.id)
    val version = varchar("version", 10)
    val start = datetime("start")
    val end = datetime("end")
    val process = varchar("process", 30)
    val httpMethod = varchar("http_method", 10)
    val httpUrl = varchar("http_url", 50)
    val httpStatusCode = integer("http_status_code")
    val requestSize = integer("request_size")
    val responseSize = integer("response_size")
    val parentId = varchar("parent_id", 50).nullable()
}