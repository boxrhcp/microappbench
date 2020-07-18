package workload

import analyzer.AnalyzerCore
import analyzer.models.SpanNode
import org.slf4j.LoggerFactory
import utils.ResourceManager

class Analyzer {
    private val log = LoggerFactory.getLogger("Analyzer")!!
    private val config = ResourceManager.loadConfigFile()
    private val core = AnalyzerCore(
        config.get("firstVersion").asString,
        config.get("secondVersion").asString,
        config.get("differenceThreshold")
    )

    fun execAnalysis() {
        log.info("Starting analysis")
        val drops = core.findPerformanceDrop()
        val traces = core.matchPatternAndTraces(drops)
        val spanTrees = ArrayList<Pair<SpanNode, SpanNode>>()
        for (trace in traces) {
            val firstRoot = core.getSpansTree(trace.first)
            val secondRoot = core.getSpansTree(trace.second)
            if (firstRoot != null && secondRoot != null) {
                spanTrees.add(Pair(firstRoot, secondRoot))
            } else {
                log.error("Spans versions could not be retrieved for trace: ${trace.first.id} pattern ${trace.first.requestId} and index ${trace.first.index}")
            }

        }
    }
}