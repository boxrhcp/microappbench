package database.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.jodatime.datetime

object OperationObject : IntIdTable() {
    val patternId = integer("pattern_id").references(PatternObject.id)
    val path = varchar("path",50)
    val operation = varchar("operation", 50)
    val index = integer("index")
    val start = datetime("start")
    val end = datetime("end")

}