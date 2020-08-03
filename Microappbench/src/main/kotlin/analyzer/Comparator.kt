package analyzer

import analyzer.models.Issue
import analyzer.models.IssueFlag
import analyzer.models.ServiceNode
import analyzer.models.SpanNode
import api.models.MetricType
import com.google.gson.JsonElement
import org.slf4j.LoggerFactory

class Comparator(
    private val thresholdExecTime: JsonElement,
    private val cpuUsageThreshold: JsonElement,
    private val memoryUsageThreshold: JsonElement,
    private val receivedBytesThreshold: JsonElement,
    private val sentBytesThreshold: JsonElement,
    private val httpRequestSizeThreshold: JsonElement,
    private val httpResponseSizeThreshold: JsonElement
) {
    private val log = LoggerFactory.getLogger("Comparator")!!
    private val issues = ArrayList<Issue>()


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

    fun getIssues(): ArrayList<Issue> {
        return issues
    }
}