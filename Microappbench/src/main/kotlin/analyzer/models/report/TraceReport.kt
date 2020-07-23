package analyzer.models.report

data class TraceReport (
    val traceId: String,
    val version: String,
    val duration: Long
){
}