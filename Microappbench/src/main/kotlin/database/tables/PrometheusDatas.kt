package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object PrometheusDatas: IntIdTable() {
    val type = varchar("type", 20)
    val pod = varchar("pod", 100)
    val time = long("time")
    val value = decimal("value",40,20)
}