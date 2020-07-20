package analyzer

import analyzer.models.SpanNode
import com.google.gson.JsonElement
import database.DatabaseOperator
import database.models.PatternAggObject
import database.models.SpanObject
import database.models.TraceMatchObject
import org.slf4j.LoggerFactory
import kotlin.math.absoluteValue

class AnalyzerCore(
    private val firstVersion: String,
    private val secondVersion: String,
    private val threshold: JsonElement
) {
    private val log = LoggerFactory.getLogger("AnalyzerCore")!!
    private val db = DatabaseOperator()

    fun findPerformanceDrop(): ArrayList<PatternAggObject> {
        log.info("Comparing benchmark results and check if there is any performance drop in new version")
        val drops = ArrayList<PatternAggObject>()
        val firstResults = db.aggregatePatternsByVersion(firstVersion)
        val secondResults = db.aggregatePatternsByVersion(secondVersion)
        for (pattern in firstResults) {
            val find = secondResults.stream()
                .filter { it.patternName == pattern.patternName && it.resource == pattern.resource }
                .findAny()
            if (find.isPresent) {
                val mirror = find.get()
                log.debug("Checking ${pattern.resource} with pattern ${pattern.patternName}")
                log.debug("${pattern.version}: ${pattern.durationAvg}")
                log.debug("${mirror.version}: ${mirror.durationAvg}")
                if ((1.toBigDecimal() + threshold.asBigDecimal) * pattern.durationAvg < mirror.durationAvg) {
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
                //log.debug("Trying to match version traces for ${trace.tracePath} with ${trace.traceMethod} [${trace.requestId}]")
                //TODO: Watch out, is the match really working?
                val find = secondTraces.stream()
                    .filter { it.requestId == trace.requestId && it.index == it.index && it.tracePath == trace.tracePath && it.traceMethod == trace.traceMethod }
                    .findAny()
                if (find.isPresent) {
                    val mirror = find.get()
                    log.debug("Request match between versions for ${trace.tracePath} with ${trace.traceMethod} [${trace.requestId}]")
                    if ((1 + threshold.asDouble) * trace.duration < mirror.duration) {
                        log.info("Difference between trace versions for ${trace.tracePath} with ${trace.traceMethod} [${trace.requestId}]")
                        results.add(Pair(trace, mirror))
                    }
                }
            }
        }
        return results
    }

    fun getSpansTree(trace: TraceMatchObject): SpanNode? {
        val spans = db.getSpansByTraceId(trace)
        val find = spans.stream().filter { it.parentId == "" }.findAny()
        var rootEdge: SpanNode? = null
        if (find.isPresent) {
            rootEdge = createSpanTree(find.get(), spans)
        }
        /*for (span in spans) {
            log.debug("Span ${span.spanId} from trace ${span.traceId} child of ${span.parentId}")
        }*/
        if (rootEdge != null) readSpanTree(rootEdge)
        log.info("__________")
        return rootEdge
    }

    private fun createSpanTree(span: SpanObject, spans: ArrayList<SpanObject>): SpanNode {
        val edges = ArrayList<SpanNode>()
        val children = spans.stream().filter { it.parentId == span.spanId }
        for (child in children) {
            edges.add(createSpanTree(child, spans))
        }
        return SpanNode(span, edges)
    }

    private fun readSpanTree(edge: SpanNode) {
        log.info("I am ${edge.span.spanId} son of ${edge.span.parentId} I do ${edge.span.httpUrl} method ${edge.span.httpMethod}")
        for (child in edge.children) {
            readSpanTree(child)
        }
    }

}