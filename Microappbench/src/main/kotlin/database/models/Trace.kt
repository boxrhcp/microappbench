package database.models

import database.tables.Traces
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Trace (id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Trace>(Traces)
    var traceId by Traces.traceId
    var version by Traces.version
    var start by Traces.start
    var end by Traces.end
}