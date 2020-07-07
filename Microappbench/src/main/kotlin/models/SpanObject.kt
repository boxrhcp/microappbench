package models

import org.joda.time.DateTime


data class SpanObject (
    val spanId: String,
    val parentId: String,
    val start: Long, //accuracy
    val end: Long,
    val httpMethod: String,
    val httpUrl: String,
    val httpStatus: Int,
    val responseSize: Int,
    val requestSize: Int,
    val process: String
){
}