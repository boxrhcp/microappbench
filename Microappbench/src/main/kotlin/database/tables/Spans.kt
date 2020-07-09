package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Spans: IntIdTable() {
    val spanId = varchar("span_id", 50)
    val traceId = integer("trace_id").references(Traces.id)
    val version = varchar("version", 10)
    val start = long("start")
    val end = long("end")
    val duration = long("duration")
    val process = varchar("process", 50)
    val httpMethod = varchar("http_method", 10)
    val httpUrl = varchar("http_url", 100)
    val httpStatusCode = integer("http_status_code")
    val requestSize = integer("request_size")
    val responseSize = integer("response_size")
    val parentId = varchar("parent_id", 50).nullable()
}