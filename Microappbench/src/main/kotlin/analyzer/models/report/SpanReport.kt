package analyzer.models.report

data class SpanReport(
    val spanId: String,
    val url: String,
    val method: String,
    val caller: String,
    val callee: String,
    val duration: Long
) {
}