package analyzer

import database.DatabaseOperator
import database.models.PatternAggObject
import database.models.TraceMatchObject
import org.slf4j.LoggerFactory
import java.math.BigDecimal

class AnalyzerCore(
    private val firstVersion: String,
    private val secondVersion: String,
    private val threshold: BigDecimal
) {
    private val log = LoggerFactory.getLogger("AnalyzerCore")!!
    private val db = DatabaseOperator()

    fun execAnalysis() {
        val drops = findPerformanceDrop()
        matchPatternAndTraces(drops)
    }

    fun findPerformanceDrop(): ArrayList<PatternAggObject> {
        log.info("Comparing benchmark results and check if there is any performance drop in new version")
        val drops = ArrayList<PatternAggObject>()
        val firstResults = db.aggregatePatterns(firstVersion)
        val secondResults = db.aggregatePatterns(secondVersion)
        for (pattern in firstResults) {
            val find = secondResults.stream()
                .filter { it.patternName == pattern.patternName && it.resource == pattern.resource }
                .findAny()
            if (find.isPresent) {
                val mirror = find.get()
                log.debug("Checking ${pattern.resource} with pattern ${pattern.patternName}")
                if ((mirror.durationAvg - pattern.durationAvg).abs() > threshold){
                    log.info("Difference found between versions: ${pattern.resource} with pattern ${pattern.patternName}")
                    drops.add(mirror)
                }
            }
        }
        return drops
    }

    fun matchPatternAndTraces(drops: ArrayList<PatternAggObject>): ArrayList<Pair<TraceMatchObject, TraceMatchObject>> {
        // get all traces or compare already traces and discard the correct ones?
        val results = ArrayList<Pair<TraceMatchObject, TraceMatchObject>>()
        for (drop in drops) {
            val firstTraces = db.getTracesMatchedWithPattern(drop, firstVersion)
            val secondTraces = db.getTracesMatchedWithPattern(drop, secondVersion)
            for (trace in firstTraces) {
                log.debug("Trying to match version traces for ${trace.tracePath} with ${trace.traceMethod} [${trace.requestId}]")
                //TODO: Watch out, is the match really working? I think the requestID order is not defined
                val find = secondTraces.stream()
                    .filter { it.requestId == trace.requestId }
                    .findAny()
                if (find.isPresent) {
                    val mirror = find.get()
                    log.info("Request match between versions for ${trace.tracePath} with ${trace.traceMethod} [${trace.requestId}]")
                    results.add(Pair(trace, mirror))
                }
            }
        }
        return results
    }

    fun generateSpanTrees() {


    }
}