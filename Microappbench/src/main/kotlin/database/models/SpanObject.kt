package database.models

data class SpanObject(
    val spanId: String,
    val traceId: String,
    val version: String,
    val patternId: Int,
    val index: Int,
    val start: Long,
    val end: Long,
    val duration: Long,
    val process: String,
    val httpMethod: String,
    val httpUrl: String,
    val httpStatusCode: Int,
    val requestSize: Int,
    val responseSize: Int,
    val parentId: String?
) {
}