package database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime

object Spans: Table() {
    val id = varchar("id", 50)
    val traceId = varchar("trace_id", 50).references(Traces.id)
    val version = varchar("version", 10)
    val start = datetime("start")
    val end = datetime("end")
    val process = varchar("process", 30)
    val httpMethod = varchar("http_method", 10)
    val http_url = varchar("http_url", 50)
    val httpStatusCode = integer("http_status_code")
    val requestSize = integer("request_size")
    val responseSize = integer("response_size")
    var parentId = varchar("parent_id", 50).references(this.id)

    override val primaryKey = PrimaryKey(Traces.id)
}