package database.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime

object TraceObject: Table() {
    val id = varchar("id", 50)
    val version = varchar("version", 10)
    val start = datetime("start")
    val end = datetime("end")
}