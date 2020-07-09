package json.models

data class PatternJson(
    val resource: String,
    val version: String,
    val patternName: String,
    val requestId: Int,
    val workerId: Int,
    val start: Long,
    val end: Long,
    val duration: Long,
    val operations: ArrayList<OperationJson>
) {
}