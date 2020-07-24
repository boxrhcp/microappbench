package analyzer.models.report

data class TraceIssueReport (
    val operation: String,
    val path: String,
    val method: String,
    val requestId: Int,
    val index: Int,
    val limit: Double,
    val difference: Double,
    val firstVersion: TraceReport,
    val secondVersion: TraceReport,
    val issues: ArrayList<SpanIssueReport>
){
}