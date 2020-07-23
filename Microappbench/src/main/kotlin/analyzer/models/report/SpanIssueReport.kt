package analyzer.models.report

data class SpanIssueReport(
    val difference: Double,
    val firstVersion: SpanReport,
    val secondVersion: SpanReport,
    val issueTag: String,
    val issueMessage: String
) {
}