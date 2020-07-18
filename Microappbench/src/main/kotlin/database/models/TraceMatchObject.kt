package database.models

data class TraceMatchObject(
    val id: Int,
    val traceId: String,
    val version: String,
    val requestId: Int,
    val index: Int,
    val tracePath: String,
    val traceUrl: String,
    val operation: String,
    val traceMethod: String,
    val headerId: String,
    val start: Long,
    val end: Long,
    val duration: Long
) {
}