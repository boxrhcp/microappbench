package database.models

import database.tables.PrometheusDatas
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PrometheusData(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PrometheusData>(PrometheusDatas)

    var type by PrometheusDatas.type
    var pod by PrometheusDatas.pod
    var time by PrometheusDatas.time
    var value by PrometheusDatas.value
}