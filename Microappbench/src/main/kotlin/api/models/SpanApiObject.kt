package api.models

import com.google.gson.JsonArray

data class SpanApiObject(
    val spanId: String,
    var parentId: String,
    val start: Long, //accuracy
    val end: Long,
    val duration: Long,
    val httpMethod: String,
    val httpUrl: String,
    val httpStatus: Int,
    val responseSize: Int,
    val requestSize: Int,
    val process: String,
    val warnings: JsonArray
) {
}