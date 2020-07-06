package database.models

import database.tables.Spans
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Span(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Span>(Spans)
    var spanId by Spans.spanId
    var traceId by Trace referencedOn Spans.traceId
    var version by Spans.version
    var start by Spans.start
    var end by Spans.end
    var process by Spans.process
    var httpMethod by Spans.httpMethod
    var httpUrl by Spans.httpUrl
    var httpStatusCode by Spans.httpStatusCode
    var requestSize by Spans.requestSize
    var responseSize by Spans.responseSize
    var parentId by Spans.parentId
}