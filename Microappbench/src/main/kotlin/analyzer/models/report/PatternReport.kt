package analyzer.models.report

data class PatternReport(
    val name: String,
    val resource: String,
    val issueTraces: ArrayList<TraceIssueReport>
) {

}