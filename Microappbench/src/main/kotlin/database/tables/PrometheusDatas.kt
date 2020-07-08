package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.jodatime.datetime

object PrometheusDatas: IntIdTable() {
    val type = varchar("type", 10)
    val pod = varchar("pod", 30)
    val time = datetime("time")
    val value = decimal("value",30,19)
}