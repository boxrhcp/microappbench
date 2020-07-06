package database.models

import database.tables.PrometheusDatas
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Span(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PrometheusData>(PrometheusDatas)
}