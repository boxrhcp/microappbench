package analyzer.models.report

data class SpanReport(
    val spanId: String,
    val url: String,
    val method: String,
    val requestSize: Int,
    val responseSize: Int,
    val caller: ServiceReport,
    val callee: ServiceReport,
    val duration: Long
) {
}