package workload

import analyzer.AnalyzerCore
import org.slf4j.LoggerFactory
import utils.ResourceManager

class Analyzer {
    private val log = LoggerFactory.getLogger("Analyzer")!!
    private val config = ResourceManager.loadConfigFile()
    private val core = AnalyzerCore(
        config.get("firstVersion").asString,
        config.get("secondVersion").asString,
        config.get("differenceThreshold").asBigDecimal
    )

    fun execAnalysis() {
        log.info("Starting analysis")
        val drops = core.findPerformanceDrop()
        core.matchPatternAndTraces(drops)
    }
}