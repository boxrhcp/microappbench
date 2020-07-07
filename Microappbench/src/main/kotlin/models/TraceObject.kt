package models

import org.joda.time.DateTime

data class TraceObject(
    val traceId: String,
    var version: String,
    var start: Long,
    var end: Long,
    val spans: ArrayList<SpanObject>
) {
}