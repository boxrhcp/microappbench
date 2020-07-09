package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Operations : IntIdTable() {
    val patternId = integer("pattern_id").references(Patterns.id)
    val path = varchar("path",100)
    val operation = varchar("operation", 50)
    val index = integer("index")
    val start = long("start")
    val end = long("end")

}