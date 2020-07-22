package workload

import analyzer.AnalyzerCore
import org.slf4j.LoggerFactory
import utils.ResourceManager

class Analyzer {
    private val log = LoggerFactory.getLogger("Analyzer")!!
    private val config = ResourceManager.getConfigFile()
    private val core = AnalyzerCore(
        config.get("firstVersion").asString,
        config.get("secondVersion").asString,
        config.getAsJsonObject("analyzer").get("execTimeThreshold"),
        config.getAsJsonObject("analyzer").get("cpuUsageThreshold"),
        config.getAsJsonObject("analyzer").get("memoryUsageThreshold"),
        config.getAsJsonObject("analyzer").get("receivedBytesThreshold"),
        config.getAsJsonObject("analyzer").get("sentBytesThreshold"),
        config.getAsJsonObject("analyzer").get("httpRequestSizeThreshold"),
        config.getAsJsonObject("analyzer").get("httpResponseSizeThreshold")
        )

    fun execAnalysis() {
        log.info("Starting analysis")
        val drops = core.findPerformanceDrop()
        val traces = core.matchPatternAndTraces(drops)
        //val spanTrees = ArrayList<Pair<SpanNode, SpanNode>>()
        for (trace in traces) {
            core.clearIssues()
            val firstRoot = core.getSpansTree(trace.first)
            val secondRoot = core.getSpansTree(trace.second)
            if (firstRoot != null && secondRoot != null) {
                //spanTrees.add()
                core.compareSpans(Pair(firstRoot, secondRoot))
                //core.tagIssues()
            } else {
                log.error("Spans versions could not be retrieved for trace: ${trace.first.id} pattern ${trace.first.requestId} and index ${trace.first.index}")
            }
        }
        for (issue in core.getIssues()) {
            log.info("${issue.tag} issue found in traceId ${issue.spanPair.second.span.traceId} compared with ${issue.spanPair.first.span.traceId}: ${issue.message}")
        }
    }
}