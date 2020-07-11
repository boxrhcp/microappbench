package api.models

data class TraceApiObject(
    val traceId: String,
    val version: String,
    val traceUrl: String,
    val traceMethod: String,
    val headerId: String,
    val start: Long,
    val end: Long,
    val duration: Long,
    val spans: ArrayList<SpanApiObject>
) {
}