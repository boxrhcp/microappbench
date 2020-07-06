package database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime

object Traces: Table() {
    val id = varchar("id", 50)
    val version = varchar("version", 10)
    val start = datetime("start")
    val end = datetime("end")

    override val primaryKey = PrimaryKey(id)
}