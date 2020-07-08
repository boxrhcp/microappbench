package workload

import com.google.gson.JsonParser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import models.*
import org.slf4j.LoggerFactory
import run.ApiHandler
import java.util.*
import kotlin.collections.ArrayList

class MonitoringRetrieval(
    private val baseUrl: String,
    private val kialiPort: String,
    private val prometheusPort: String,
    private val start: Long,
    private val end: Long
) {

    private val apiHandler = ApiHandler()
    private val log = LoggerFactory.getLogger("MonitoringRetrieval")!!


    fun retrieveKiali() {
        log.info("Retrieving Kiali information")
        val auth = "admin:admin"
        val parameterList = ArrayList<Pair<String, String>>()
        parameterList.add(Pair("namespace", "sock-shop"))
        parameterList.add(Pair("service", "orders"))
        val queryPath = "/kiali/api/namespaces/{namespace}/services/{service}/traces"
        val headerList = ArrayList<Pair<String, String>>()
        headerList.add(Pair("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.toByteArray())))
        val request = ApiRequestObject(
            "kiali",
            "$baseUrl:$kialiPort$queryPath",
            parameterList.toTypedArray(),
            headerList.toTypedArray(),
            "GET",
            body = JsonParser().parse(""),
            response = "",
            status = 0
        )
        runBlocking {
            apiHandler.makeApiRequest(request)
        }
        log.info("wwowwww" + request.response)
        val results = JsonParser().parse(request.response).asJsonObject.getAsJsonArray("data")
        val traces = ArrayList<TraceApiObject>()
        for (traceElement in results) {
            val trace = traceElement.asJsonObject
            val traceId = trace.get("traceID").asString
            var version = "v1"
            var traceStart = 0L
            var traceEnd = 0L
            val spans = ArrayList<SpanApiObject>()
            for (spanElement in trace.getAsJsonArray("spans")) {
                val span = spanElement.asJsonObject
                val start = span.get("startTime").asLong
                if (start < traceStart || traceStart == 0L) traceStart = start
                val end = span.get("duration").asLong + start
                if (end > traceEnd) traceEnd = end
                val spanId = span.get("spanID").asString
                var parentId = ""
                val references = span.getAsJsonArray("references")
                if (references.size() > 0) {
                    for (referenceElem in references) {
                        val reference = referenceElem.asJsonObject
                        if (reference.get("refType").asString == "CHILD_OF") parentId = reference.get("spanID").asString
                    }
                }
                val process = trace.getAsJsonObject("processes").getAsJsonObject(span.get("processID").asString)
                    .get("serviceName").asString
                var requestSize = 0
                var responseSize = 0
                var httpMethod = ""
                var httpStatus = 0
                var httpUrl = ""
                for (tagElement in span.getAsJsonArray("tags")) {
                    val tag = tagElement.asJsonObject
                    when (tag.get("key").asString) {
                        "request_size" -> requestSize = tag.get("value").asInt
                        "response_size" -> responseSize = tag.get("value").asInt
                        "http.method" -> httpMethod = tag.get("value").asString
                        "http.status_code" -> httpStatus = tag.get("value").asInt
                        "http.url" -> httpUrl = tag.get("value").asString
                        "node_id" -> { //TODO: IMPROVE IT TO BE OPEN
                            if (tag.get("value").asString.contains("orders-v2")) {
                                version = "v2"
                            }
                        }
                    }
                }
                log.debug("Adding span - spanId:$spanId parentId:$parentId start:$start end:$end httpMethod:$httpMethod httpUrl:$httpUrl httpStatus:$httpStatus responseSize:$responseSize requestSize:$requestSize process:$process")
                spans.add(
                    SpanApiObject(
                        spanId,
                        parentId,
                        start,
                        end,
                        httpMethod,
                        httpUrl,
                        httpStatus,
                        responseSize,
                        requestSize,
                        process
                    )
                )
            }
            log.debug("Adding trace - traceId:$traceId version:$version traceStart:$traceStart traceEnd:$traceEnd")
            traces.add(TraceApiObject(traceId, version, traceStart, traceEnd, spans))
        }

    }

    fun retrievePrometheus() {
        log.info("Retrieving prometheus information")
        val parameterList = ArrayList<Pair<String, String>>()
        parameterList.add(
            Pair(
                "query",
                "sum(rate(container_cpu_usage_seconds_total{container_name!=\"POD\",pod_name!=\"\"}[5m])) by (pod_name)"
            )
        )
        parameterList.add(Pair("start", start.toString()))
        parameterList.add(Pair("end", end.toString()))
        parameterList.add(Pair("step", "5s"))
        val queryPath = "/api/v1/query_range"
        val headerList = ArrayList<Pair<String, String>>()

        val request = ApiRequestObject(
            "prometheus",
            "$baseUrl:$prometheusPort$queryPath",
            parameterList.toTypedArray(),
            headerList.toTypedArray(),
            "GET",
            body = JsonParser().parse(""),
            response = "",
            status = 0
        )

        runBlocking {
            apiHandler.makeApiRequest(request)
        }
        val prometheusData = ArrayList<PrometheusApiObject>()
        val results =
            JsonParser().parse(request.response).asJsonObject.getAsJsonObject("data").getAsJsonArray("result")
        for (resultElem in results) {
            val type = "try"
            val result = resultElem.asJsonObject
            val pod = result.getAsJsonObject("metric").get("pod_name").asString
            val values = result.getAsJsonArray("values")
            val metrics = ArrayList<PrometheusValuesObject>()
            for(valueElem in values){
                val tuple = valueElem.asJsonArray
                val timestamp = tuple.get(0).asLong
                val value = tuple.get(1).asBigDecimal
                metrics.add(PrometheusValuesObject(timestamp, value))
                log.debug("Adding values for pod $pod - timestamp:$timestamp value:$value")
            }

            log.debug("Adding prometheus data - type:$type pod:$pod")
            prometheusData.add(PrometheusApiObject(type, pod, metrics))
        }

    }


}