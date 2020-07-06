package database.models

import database.tables.Patterns
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Pattern(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Pattern>(Patterns)

    var resource by Patterns.resource
    var version by Patterns.version
    var patternName by Patterns.patternName
    var requestId by Patterns.requestId
    var workerId by Patterns.workerId
    var start by Patterns.start
    var end by Patterns.end
}