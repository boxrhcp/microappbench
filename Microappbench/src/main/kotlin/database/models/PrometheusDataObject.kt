package database.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.jodatime.datetime

object PrometheusDataObject: IntIdTable() {
    val type = varchar("type", 10)
    val pod = varchar("pod", 30)
    val time = datetime("time")
    val value = float("value")
}