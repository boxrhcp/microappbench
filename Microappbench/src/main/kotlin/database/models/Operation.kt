package database.models

import database.tables.Operations
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Operation(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Operation>(Operations)

    var patternId by Pattern referencedOn Operations.patternId
    var path by Operations.path
    var operation by Operations.operation
    var index by Operations.index
    var start by Operations.start
    var end by Operations.end
}