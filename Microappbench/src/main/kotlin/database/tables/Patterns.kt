package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.jodatime.datetime

object Patterns : IntIdTable() {
    val resource = varchar("resource", 50)
    val version = varchar("version", 10)
    val patternName = varchar("patternName", 50)
    val requestId = integer("request_id")
    val workerId = integer("woerker_id")
    val start = datetime("start")
    val end = datetime("end")
}