package analyzer.models.report

data class SpanIssueReport(
    val limit: Double,
    val difference: Double,
    val firstVersion: SpanReport,
    val secondVersion: SpanReport,
    val issueTag: String,
    val issueFlags: ArrayList<String>,
    val issueMessage: ArrayList<String>
) {
}