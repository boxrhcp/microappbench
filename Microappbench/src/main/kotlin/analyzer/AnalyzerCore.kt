package analyzer

import analyzer.models.Issue
import analyzer.models.IssueFlag
import analyzer.models.ServiceNode
import analyzer.models.SpanNode
import analyzer.models.report.*
import api.models.MetricType
import com.google.gson.JsonElement
import database.DatabaseOperator
import database.models.PatternAggObject
import database.models.SpanObject
import database.models.TraceMatchObject
import org.slf4j.LoggerFactory
import java.math.BigDecimal

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
                (1 + thresholdExecTime.asDouble) * ogSpan.span.duration < newSpan.span.duration

            //check span request size difference
            issue.flags[IssueFlag.REQ_SIZE.flagName] =
                (1 + httpRequestSizeThreshold.asDouble) * ogSpan.span.requestSize < newSpan.span.requestSize

            //check span response size difference
            issue.flags[IssueFlag.RES_SIZE.flagName] =
                (1 + httpResponseSizeThreshold.asDouble) * ogSpan.span.responseSize < newSpan.span.responseSize

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
                val count = newSpan.children.stream()
                    .filter {
                        it.caller.service == ogChild.caller.service
                                && it.callee.service == ogChild.callee.service
                                && it.span.httpMethod == ogChild.span.httpMethod
                                && it.span.httpUrl.substringAfter("http://")
                            .substringBefore("/") == ogChild.span.httpUrl.substringAfter("http://").substringBefore("/")
                                && it.span.httpUrl.substringAfter("http://").substringAfter("/")
                            .substringBefore("/") == ogChild.span.httpUrl.substringAfter("http://").substringAfter("/")
                            .substringBefore("/")
                        //    && it.span.httpUrl.substringAfter("http://").substringAfter("/")
                        //.substringAfter("/") != ogChild.span.httpUrl.substringAfter("http://").substringAfter("/")
                        //.substringAfter("/")
                    }.count()
                if (count == 1L) {
                    val find = newSpan.children.stream()
                        .filter {
                            it.caller.service == ogChild.caller.service
                                    && it.callee.service == ogChild.callee.service
                                    && it.span.httpMethod == ogChild.span.httpMethod
                                    && it.span.httpUrl.substringAfter("http://")
                                .substringBefore("/") == ogChild.span.httpUrl.substringAfter("http://")
                                .substringBefore("/")
                                    && it.span.httpUrl.substringAfter("http://").substringAfter("/")
                                .substringBefore("/") == ogChild.span.httpUrl.substringAfter("http://")
                                .substringAfter("/")
                                .substringBefore("/")
                            //    && it.span.httpUrl.substringAfter("http://").substringAfter("/")
                            //.substringAfter("/") != ogChild.span.httpUrl.substringAfter("http://").substringAfter("/")
                            //.substringAfter("/")
                        }.findAny()
                    compareSpans(Pair(ogChild, find.get()))
                } else if (count < 1L) {
                    log.error("Error trying to match ${ogSpan.span.spanId} child with index ${ogChild.span.index} with ${newSpan.span.spanId}. There are no matches with new span")
                    issue.flags[IssueFlag.CHILD_MISMATCH.flagName] = true
                } else if (count > 1L) {
                    log.error("Error trying to match ${ogSpan.span.spanId} child with index ${ogChild.span.index} with ${newSpan.span.spanId}. There are several call matches, probably a repeated call")
                    issue.flags[IssueFlag.CHILD_MISMATCH.flagName] = true
                }
            }

            if (!issue.flags.all { !it.value } && spanPair.second.caller.service != "istio-ingressgateway") issues.add(
                issue
            )

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
            //MetricType.SENT_BYTES -> IssueFlag.SENT_BYTES
            //MetricType.RECEIVED_BYTES -> IssueFlag.RECEIVED_BYTES
        }
        val threshold = when (type) {
            MetricType.CPU -> cpuUsageThreshold
            MetricType.MEMORY -> memoryUsageThreshold
            //MetricType.SENT_BYTES -> sentBytesThreshold
            //MetricType.RECEIVED_BYTES -> receivedBytesThreshold
        }
        if (ogValue == null || newValue == null) {
            log.error("Error querying metric ${type.typeName}")
        } else {
            // Cpu threshold + original cpu usage compared with new usage
            if (type == MetricType.CPU) {
                if (ogValue + threshold.asBigDecimal < newValue) {
                    issue.flags[flag.flagName] = true
                    newNode.flags.add(flag.flagName)
                } else if (!issue.flags.containsKey(flag.flagName)) {
                    issue.flags[flag.flagName] = false
                }
            } else {
                //Any other metric is compared by calculating the limit
                if ((1.toBigDecimal() + threshold.asBigDecimal) * ogValue < newValue) {
                    issue.flags[flag.flagName] = true
                    newNode.flags.add(flag.flagName)
                } else if (!issue.flags.containsKey(flag.flagName)) {
                    issue.flags[flag.flagName] = false
                }
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
        val traceLimit = (1 + thresholdExecTime.asDouble) * ogDuration
        val traceDifference = (1 - (newDuration.toDouble() / ogDuration.toDouble())) * 100
        val spanIssues = ArrayList<SpanIssueReport>()
        for (issue in issues) {
            val ogSpan = issue.spanPair.first
            val newSpan = issue.spanPair.second
            val ogSpanId = ogSpan.span.spanId
            val newSpanId = newSpan.span.spanId
            val ogUrl = ogSpan.span.httpUrl
            val ogReqSize = ogSpan.span.requestSize
            val ogResSize = ogSpan.span.responseSize
            val newReqSize = newSpan.span.requestSize
            val newResSize = newSpan.span.responseSize
            val ogCpuUsageCaller = if (ogSpan.caller.getMetricAvgByType(MetricType.CPU.typeName) != null) {
                ogSpan.caller.getMetricAvgByType(MetricType.CPU.typeName)!!
            } else {
                BigDecimal.ZERO
            }

            val newCpuUsageCaller = if (newSpan.caller.getMetricAvgByType(MetricType.CPU.typeName) != null) {
                newSpan.caller.getMetricAvgByType(MetricType.CPU.typeName)!!
            } else {
                BigDecimal.ZERO
            }

            val ogMemUsageCaller = if (ogSpan.caller.getMetricAvgByType(MetricType.MEMORY.typeName) != null) {
                ogSpan.caller.getMetricAvgByType(MetricType.MEMORY.typeName)!!
            } else {
                BigDecimal.ZERO
            }

            val newMemUsageCaller = if (newSpan.caller.getMetricAvgByType(MetricType.MEMORY.typeName) != null) {
                newSpan.caller.getMetricAvgByType(MetricType.MEMORY.typeName)!!
            } else {
                BigDecimal.ZERO
            }

            val ogCpuUsageCallee = if (ogSpan.callee.getMetricAvgByType(MetricType.CPU.typeName) != null) {
                ogSpan.callee.getMetricAvgByType(MetricType.CPU.typeName)!!
            } else {
                BigDecimal.ZERO
            }

            val newCpuUsageCallee = if (newSpan.callee.getMetricAvgByType(MetricType.CPU.typeName) != null) {
                newSpan.callee.getMetricAvgByType(MetricType.CPU.typeName)!!
            } else {
                BigDecimal.ZERO
            }

            val ogMemUsageCallee = if (ogSpan.callee.getMetricAvgByType(MetricType.MEMORY.typeName) != null) {
                ogSpan.callee.getMetricAvgByType(MetricType.MEMORY.typeName)!!
            } else {
                BigDecimal.ZERO
            }

            val newMemUsageCallee = if (newSpan.callee.getMetricAvgByType(MetricType.MEMORY.typeName) != null) {
                newSpan.callee.getMetricAvgByType(MetricType.MEMORY.typeName)!!
            } else {
                BigDecimal.ZERO
            }
            val newUrl = newSpan.span.httpUrl
            val ogMethod = ogSpan.span.httpMethod
            val newMethod = newSpan.span.httpMethod
            val ogSpanDuration = ogSpan.span.duration
            val newSpanDuration = newSpan.span.duration
            val spanLimit = (1 + thresholdExecTime.asDouble) * ogSpanDuration
            val spanDifference = (1 - (newSpanDuration.toDouble() / ogSpanDuration.toDouble())) * 100
            val flagList = tagIssue(issue)
            spanIssues.add(
                SpanIssueReport(
                    spanLimit,
                    spanDifference,
                    SpanReport(
                        ogSpanId,
                        ogUrl,
                        ogMethod,
                        ogReqSize,
                        ogResSize,
                        ServiceReport(
                            ogSpan.caller.service,
                            ogCpuUsageCaller,
                            ogMemUsageCaller
                        ),
                        ServiceReport(
                            ogSpan.callee.service,
                            ogCpuUsageCallee,
                            ogMemUsageCallee
                        ),
                        ogSpanDuration
                    ),
                    SpanReport(
                        newSpanId,
                        newUrl,
                        newMethod,
                        newReqSize,
                        newResSize,
                        ServiceReport(
                            newSpan.caller.service,
                            newCpuUsageCaller,
                            newMemUsageCaller
                        ),
                        ServiceReport(
                            newSpan.callee.service,
                            newCpuUsageCallee,
                            newMemUsageCallee
                        ),
                        newSpanDuration
                    ),
                    issue.tag,
                    flagList,
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
            traceLimit,
            traceDifference,
            TraceReport(ogId, ogVersion, ogDuration),
            TraceReport(newId, newVersion, newDuration),
            spanIssues
        )
    }

    private fun tagIssue(issue: Issue): ArrayList<String> {
        val flags = issue.flags
        var callerIssues = 0
        var calleeIssues = 0
        val flagList = ArrayList<String>()
        if (flags[IssueFlag.EXEC_TIME.flagName]!!) {
            val limit = (1 + thresholdExecTime.asDouble) * issue.spanPair.first.span.duration
            issue.message.add(
                "- Performance drop noticed in execution time:" +
                        "    first version: ${issue.spanPair.first.span.duration}, second version: ${issue.spanPair.second.span.duration}, limit: $limit. "
            )
            flagList.add(IssueFlag.EXEC_TIME.flagName)
        }

        if (flags[IssueFlag.CALL_ERROR.flagName]!!) {
            val statusPattern = issue.spanPair.second.span.httpStatusCode / 100
            //if error 4xx client bad requests types
            if (statusPattern == 4) {
                issue.tag = "parent"
            }
            // if error 5xx server error types
            if (statusPattern == 5) {
                issue.tag = "child"
            }
            issue.message.add("- New error detected in call: ${issue.spanPair.second.span.httpStatusCode}")
            flagList.add(IssueFlag.CALL_ERROR.flagName)
        }

        for (type in MetricType.values()) {
            val flag = when (type) {
                MetricType.CPU -> IssueFlag.CPU.flagName
                MetricType.MEMORY -> IssueFlag.MEMORY.flagName
                //MetricType.SENT_BYTES -> IssueFlag.SENT_BYTES.flagName
                //MetricType.RECEIVED_BYTES -> IssueFlag.RECEIVED_BYTES.flagName
            }

            val threshold = when (type) {
                MetricType.CPU -> cpuUsageThreshold
                MetricType.MEMORY -> memoryUsageThreshold
                //MetricType.SENT_BYTES -> sentBytesThreshold
                //MetricType.RECEIVED_BYTES -> receivedBytesThreshold
            }

            if (flags[flag]!!) {
                if (issue.spanPair.second.caller.flags.contains(flag)) {
                    callerIssues += 1
                    val ogUsage = issue.spanPair.first.caller.getMetricAvgByType(flag)
                    val newUsage = issue.spanPair.second.caller.getMetricAvgByType(flag)
                    val limit = if (flag == IssueFlag.CPU.flagName) {
                        ogUsage!!.plus(threshold.asBigDecimal)
                    } else {
                        calculateValueDiff(ogUsage, threshold)
                    }
                    issue.message.add(
                        "- Caller ${issue.spanPair.second.caller.service} $flag is over limit established: $limit " +
                                "    Original average: $ogUsage " +
                                "    New average $newUsage"
                    )
                }

                if (issue.spanPair.second.callee.flags.contains(flag)) {
                    calleeIssues += 1
                    val ogUsage = issue.spanPair.first.callee.getMetricAvgByType(flag)
                    val newUsage = issue.spanPair.second.callee.getMetricAvgByType(flag)
                    val limit = if (flag == IssueFlag.CPU.flagName) {
                        ogUsage!!.plus(threshold.asBigDecimal)
                    } else {
                        calculateValueDiff(ogUsage, threshold)
                    }
                    issue.message.add(
                        "- Callee ${issue.spanPair.second.callee.service} $flag is over limit established: $limit " +
                                "    Original average: $ogUsage " +
                                "    New average $newUsage"
                    )
                }
                flagList.add(flag)
            }
        }

        if (flags[IssueFlag.REQ_SIZE.flagName]!!) {
            if (flags[IssueFlag.CALL_ERROR.flagName]!!) issue.tag = "parent"
            callerIssues += 1
            val ogSize = issue.spanPair.first.span.requestSize
            val newSize = issue.spanPair.second.span.requestSize
            val limit = (1 + httpRequestSizeThreshold.asDouble) * ogSize
            issue.message.add(
                "- Request size is over threshold established: $limit " +
                        "    Original size: $ogSize " +
                        "    New size $newSize"
            )
            flagList.add(IssueFlag.REQ_SIZE.flagName)
        }

        if (flags[IssueFlag.RES_SIZE.flagName]!!) {
            calleeIssues += 1
            val ogSize = issue.spanPair.first.span.responseSize
            val newSize = issue.spanPair.second.span.responseSize
            val limit = (1 + httpResponseSizeThreshold.asDouble) * ogSize
            issue.message.add(
                "- Response size is over threshold established: $limit " +
                        "    Original size: $ogSize " +
                        "    New size $newSize"
            )
            flagList.add(IssueFlag.RES_SIZE.flagName)

        }

        if (flags[IssueFlag.CALL_MISMATCH.flagName]!!) {
            issue.tag = "call"
            issue.message.add(
                "- The call differs in caller, callee or the method: " +
                        "    Original call: Caller - ${issue.spanPair.first.caller.service}" +
                        ", Callee - ${issue.spanPair.first.callee.service}, method: ${issue.spanPair.first.span.httpMethod}, call: ${issue.spanPair.first.span.httpUrl} " +
                        "    New call: Caller - ${issue.spanPair.second.caller.service}, Callee - ${issue.spanPair.second.callee.service}, method: ${issue.spanPair.second.span.httpMethod}, call: ${issue.spanPair.second.span.httpUrl}"
            )
            flagList.add(IssueFlag.CALL_MISMATCH.flagName)
        }
        if (issue.tag == "") {
            if (callerIssues > calleeIssues) {
                issue.tag = "parent"
            } else if (callerIssues < calleeIssues) {
                issue.tag = "child"
            } else {
                issue.tag = "call"
            }
        }

        if (flags[IssueFlag.CHILD_MISMATCH.flagName]!!) {
            var childMessage = ""
            //if there is a performance drop in current span while having a child mismatch it means that the performance drop is provoked by these extra calls
            if (flags[IssueFlag.EXEC_TIME.flagName]!! || flags[IssueFlag.CPU.flagName]!!) {
                if (issue.tag == "") issue.tag = "child"
                childMessage = "- Anomalous calls called by ${issue.spanPair.second.callee.service}"
            }

            childMessage += "   The number child calls of the first version are different from the second version. First version child calls:"
            for (child in issue.spanPair.first.children) {
                childMessage += "    From ${child.caller.service} to ${child.callee.service}, url ${child.span.httpUrl}, method ${child.span.httpMethod}"
            }
            childMessage += "   Second version child calls: "
            for (child in issue.spanPair.second.children) {
                childMessage += "    From ${child.caller.service} to ${child.callee.service}, url ${child.span.httpUrl}, method ${child.span.httpMethod}"
            }
            issue.message.add(childMessage)
            flagList.add(IssueFlag.CHILD_MISMATCH.flagName)
        }
        return flagList
    }

    fun clearIssues() {
        issues.clear()
    }

    private fun calculateValueDiff(ogValue: BigDecimal?, threshold: JsonElement): BigDecimal {
        return try {
            (1.toBigDecimal() + threshold.asBigDecimal) * ogValue!!
        } catch (e: ArithmeticException) {
            log.error("Arithmetic error when calculating difference")
            ogValue!!
        }
    }

}