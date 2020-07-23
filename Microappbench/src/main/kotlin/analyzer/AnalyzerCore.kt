package analyzer

import analyzer.models.Issue
import analyzer.models.IssueFlag
import analyzer.models.ServiceNode
import analyzer.models.SpanNode
import analyzer.models.report.SpanIssueReport
import analyzer.models.report.SpanReport
import analyzer.models.report.TraceIssueReport
import analyzer.models.report.TraceReport
import api.models.MetricType
import com.google.gson.JsonElement
import database.DatabaseOperator
import database.models.PatternAggObject
import database.models.SpanObject
import database.models.TraceMatchObject
import org.slf4j.LoggerFactory

class AnalyzerCore(
    private val firstVersion: String,
    private val secondVersion: String,
    private val thresholdExecTime: JsonElement,
    private val cpuUsageThreshold: JsonElement,
    private val memoryUsageThreshold: JsonElement,
    private val receivedBytesThreshold: JsonElement,
    private val sentBytesThreshold: JsonElement,
    private val httpRequestSizeThreshold: JsonElement,
    private val httpResponseSizeThreshold: JsonElement
) {
    private val log = LoggerFactory.getLogger("AnalyzerCore")!!
    private val db = DatabaseOperator()
    private val issues = ArrayList<Issue>()

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
                if ((1.toBigDecimal() + thresholdExecTime.asBigDecimal) * pattern.durationAvg < mirror.durationAvg) {
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
                    if ((1 + thresholdExecTime.asDouble) * trace.duration < mirror.duration) {
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
        //if (rootEdge != null) readSpanTree(rootEdge)
        //log.info("__________")
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

    fun compareSpans(spanPair: Pair<SpanNode, SpanNode>) {
        val ogSpan = spanPair.first
        val newSpan = spanPair.second
        val issue = Issue(spanPair)
        //TODO compare urls?
        if (ogSpan.callee.service == newSpan.callee.service && ogSpan.caller.service == newSpan.caller.service && ogSpan.span.httpMethod == newSpan.span.httpMethod) {
            issue.flags[IssueFlag.CALL_MISMATCH.flagName] = false
            // check caller and callee metric differences
            for (type in MetricType.values()) {
                checkServiceNodes(issue, ogSpan.caller, newSpan.caller, type)
                checkServiceNodes(issue, ogSpan.callee, newSpan.callee, type)
            }
            //check span exec time difference
            issue.flags[IssueFlag.EXEC_TIME.flagName] =
                (1 * thresholdExecTime.asDouble) * ogSpan.span.duration < newSpan.span.duration

            //check span request size difference
            issue.flags[IssueFlag.REQ_SIZE.flagName] =
                (1 * httpRequestSizeThreshold.asDouble) * ogSpan.span.requestSize < newSpan.span.requestSize

            //check span response size difference
            issue.flags[IssueFlag.RES_SIZE.flagName] =
                (1 * httpResponseSizeThreshold.asDouble) * ogSpan.span.responseSize < newSpan.span.responseSize

            //check if http status code is different and its not success
            issue.flags[IssueFlag.CALL_ERROR.flagName] =
                (ogSpan.span.httpStatusCode != newSpan.span.httpStatusCode && newSpan.span.httpStatusCode != 200
                        && newSpan.span.httpStatusCode != 201 && newSpan.span.httpStatusCode != 202
                        && newSpan.span.httpStatusCode != 203 && newSpan.span.httpStatusCode != 204
                        && newSpan.span.httpStatusCode != 205 && newSpan.span.httpStatusCode != 206
                        && newSpan.span.httpStatusCode != 207 && newSpan.span.httpStatusCode != 208
                        && newSpan.span.httpStatusCode != 226)

            //check if there is a call mismatch
            issue.flags[IssueFlag.CHILD_MISMATCH.flagName] = ogSpan.children.size != newSpan.children.size

            for (ogChild in ogSpan.children) {
                val find = newSpan.children.stream().filter { it.span.index == ogChild.span.index }.findAny()
                if (find.isPresent) {
                    compareSpans(Pair(ogChild, find.get()))
                } else {
                    log.error("Error trying to match ${ogSpan.span.spanId} child with index ${ogChild.span.index} with ${newSpan.span.spanId}")
                    issue.flags[IssueFlag.CHILD_MISMATCH.flagName] = true
                }
            }

            if (issue.flags.isNotEmpty()) issues.add(issue)

        } else {
            for (flag in IssueFlag.values()) {
                if (flag == IssueFlag.CALL_MISMATCH) {
                    issue.flags[IssueFlag.CALL_MISMATCH.flagName] = true
                } else {
                    issue.flags[flag.flagName] = false
                }
            }
            issues.add(issue)
        }
    }

    private fun checkServiceNodes(issue: Issue, ogNode: ServiceNode, newNode: ServiceNode, type: MetricType) {
        val ogValue = ogNode.getMetricAvgByType(type.typeName)
        val newValue = newNode.getMetricAvgByType(type.typeName)
        val flag = when (type) {
            MetricType.CPU -> IssueFlag.CPU
            MetricType.MEMORY -> IssueFlag.MEMORY
            MetricType.SENT_BYTES -> IssueFlag.SENT_BYTES
            MetricType.RECEIVED_BYTES -> IssueFlag.RECEIVED_BYTES
        }
        val threshold = when (type) {
            MetricType.CPU -> cpuUsageThreshold
            MetricType.MEMORY -> memoryUsageThreshold
            MetricType.SENT_BYTES -> sentBytesThreshold
            MetricType.RECEIVED_BYTES -> receivedBytesThreshold
        }
        if (ogValue == null || newValue == null) {
            log.error("Error querying metric ${type.typeName}")
        } else {
            if ((1.toBigDecimal() + threshold.asBigDecimal) * ogValue < newValue) {
                issue.flags[flag.flagName] = true
                newNode.flags.add(flag.flagName)
            } else if (!issue.flags.containsKey(flag.flagName)) {
                issue.flags[flag.flagName] = false
            }
        }
    }

    fun generateReport(trace: Pair<TraceMatchObject, TraceMatchObject>): TraceIssueReport {
        val ogTrace = trace.first
        val newTrace = trace.second

        val ogId = ogTrace.traceId
        val newId = newTrace.traceId
        val ogVersion = ogTrace.version
        val newVersion = newTrace.version
        val path = ogTrace.tracePath
        val method = ogTrace.traceMethod
        val operation = ogTrace.operation
        val requestId = ogTrace.requestId
        val index = ogTrace.index
        val ogDuration = ogTrace.duration
        val newDuration = newTrace.duration
        val traceDifference = newDuration.toDouble() / ogDuration.toDouble()
        val spanIssues = ArrayList<SpanIssueReport>()
        for (issue in issues) {
            val ogSpan = issue.spanPair.first
            val newSpan = issue.spanPair.second
            val ogSpanId = ogSpan.span.spanId
            val newSpanId = newSpan.span.spanId
            val ogUrl = ogSpan.span.httpUrl
            val newUrl = newSpan.span.httpUrl
            val ogMethod = ogSpan.span.httpMethod
            val newMethod = newSpan.span.httpMethod
            val ogSpanDuration = ogSpan.span.duration
            val newSpanDuration = newSpan.span.duration
            val spanDifference = newSpanDuration.toDouble() / ogSpanDuration.toDouble()
            tagIssue(issue)
            spanIssues.add(
                SpanIssueReport(
                    spanDifference,
                    SpanReport(ogSpanId, ogUrl, ogMethod, ogSpan.caller.service, ogSpan.callee.service, ogSpanDuration),
                    SpanReport(
                        newSpanId,
                        newUrl,
                        newMethod,
                        newSpan.caller.service,
                        newSpan.callee.service,
                        newSpanDuration
                    ),
                    issue.tag,
                    issue.message
                )
            )
            log.info("Issue with ${issue.spanPair.first.caller.service} and ${issue.spanPair.first.callee.service} ")
        }
        return TraceIssueReport(
            operation,
            path,
            method,
            requestId,
            index,
            traceDifference,
            TraceReport(ogId, ogVersion, ogDuration),
            TraceReport(newId, newVersion, newDuration),
            spanIssues
        )
    }

    private fun tagIssue(issue: Issue) {
        val flags = issue.flags
        var message = ""
        if (flags[IssueFlag.EXEC_TIME.flagName]!!) {
            val difference = issue.spanPair.second.span.duration / issue.spanPair.first.span.duration
            message += "- Performance drop noticed in execution time:\n" +
                    "    first version: ${issue.spanPair.first.span.duration}, second version: ${issue.spanPair.second.span.duration}, difference: $difference.\n"
        }

        if (flags[IssueFlag.CALL_ERROR.flagName]!!) {
            message += "- New error detected in call: ${issue.spanPair.second.span.httpStatusCode}\n"
        }

        if (flags[IssueFlag.CALL_MISMATCH.flagName]!!) {
            issue.tag = "call"
            message +=
                "- The call differs in caller, callee or the method: \n " +
                        "    Original call: Caller - ${issue.spanPair.first.caller.service}" +
                        ", Callee - ${issue.spanPair.first.callee.service}, method: ${issue.spanPair.first.span.httpMethod}, call: ${issue.spanPair.first.span.httpUrl}\n" +
                        "    New call: Caller - ${issue.spanPair.second.caller.service}, Callee - ${issue.spanPair.second.callee.service}, method: ${issue.spanPair.second.span.httpMethod}, call: ${issue.spanPair.second.span.httpUrl}"

        }

        if (flags[IssueFlag.CHILD_MISMATCH.flagName]!!) {
            if (issue.tag == "") issue.tag = "call"
            message += "- The number child calls of the first version are different from the second version.\n First version child calls:\n"
            for (child in issue.spanPair.first.children) {
                message += "    From ${child.caller.service} to ${child.callee.service}, url ${child.span.httpUrl}, method ${child.span.httpMethod}\n"
            }
            message += "  Second version child calls: \n"
            for (child in issue.spanPair.second.children) {
                message += "    From ${child.caller.service} to ${child.callee.service}, url ${child.span.httpUrl}, method ${child.span.httpMethod}\n"
            }
        }
        issue.message = message
    }

    fun clearIssues() {
        issues.clear()
    }

    fun getIssues(): ArrayList<Issue> {
        return issues
    }

}