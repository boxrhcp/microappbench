package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Operations : IntIdTable() {
    val patternId = integer("pattern_id").references(Patterns.id)
    val path = varchar("path", 100)
    val operation = varchar("operation", 50)
    val concretePath = varchar("concrete_path", 100)
    val concreteMethod = varchar("concrete_method", 10)
    val headerId = varchar("headerId", 100)
    val index = integer("index")
    val start = long("start")
    val end = long("end")
    val duration = long("duration")

}