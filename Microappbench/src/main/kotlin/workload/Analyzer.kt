package workload

import analyzer.AnalyzerCore
import analyzer.Comparator
import analyzer.models.report.TraceIssueReport
import kotlinx.coroutines.*
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

    fun execAnalysis(): ArrayList<TraceIssueReport> {
        log.info("Starting analysis")
        val drops = core.findPerformanceDrop()
        val traces = core.matchPatternAndTraces(drops)
        val traceReports = ArrayList<TraceIssueReport>()
        runBlocking {
            coroutineScope {
                traces.map {
                    async(Dispatchers.IO) {
                        val comparator = Comparator(
                            config.getAsJsonObject("analyzer").get("execTimeThreshold"),
                            config.getAsJsonObject("analyzer").get("cpuUsageThreshold"),
                            config.getAsJsonObject("analyzer").get("memoryUsageThreshold"),
                            config.getAsJsonObject("analyzer").get("receivedBytesThreshold"),
                            config.getAsJsonObject("analyzer").get("sentBytesThreshold"),
                            config.getAsJsonObject("analyzer").get("httpRequestSizeThreshold"),
                            config.getAsJsonObject("analyzer").get("httpResponseSizeThreshold")
                        )
                        val firstRoot = core.getSpansTree(it.first)
                        val secondRoot = core.getSpansTree(it.second)
                        if (firstRoot != null && secondRoot != null) {
                            comparator.compareSpans(Pair(firstRoot, secondRoot))
                            traceReports.add(core.generateReport(it, comparator.getIssues()))
                        } else {
                            log.error("Spans versions could not be retrieved for trace: ${it.first.id} pattern ${it.first.requestId} and index ${it.first.index}")
                        }
                    }
                }
            }.awaitAll()
        }

        return traceReports
    }
}