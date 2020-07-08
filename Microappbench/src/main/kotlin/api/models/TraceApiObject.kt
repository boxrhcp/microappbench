package api.models

data class TraceApiObject(
    val traceId: String,
    var version: String,
    var start: Long,
    var end: Long,
    val spans: ArrayList<SpanApiObject>
) {
}