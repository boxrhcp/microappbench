package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Patterns : IntIdTable() {
    val resource = varchar("resource", 50)
    val version = varchar("version", 10)
    val patternName = varchar("patternName", 50)
    val requestId = integer("request_id")
    val workerId = integer("worker_id")
    val start = long("start")
    val end = long("end")
    val duration = long("duration")
}