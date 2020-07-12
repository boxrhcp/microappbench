package api

import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import api.models.*
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.collections.ArrayList

class ApiRetrieval(
    private val config: JsonObject,
    private val start: Long,
    private val end: Long
) {
    private val baseUrl = config.get("baseUrl").asString
    private val apiHandler = ApiHandler()
    private val log = LoggerFactory.getLogger("MonitoringRetrieval")!!


    fun retrieveKiali(): ArrayList<TraceApiObject> {
        log.info("Retrieving Kiali information")
        val user = config.getAsJsonObject("kiali").get("user").asString
        val password = config.getAsJsonObject("kiali").get("password").asString
        val auth = "$user:$password"
        val kialiPort = config.getAsJsonObject("kiali").get("port").asString
        val parameterList = ArrayList<Pair<String, String>>()
        parameterList.add(Pair("namespace", config.getAsJsonObject("sut").get("namespace").asString))
        parameterList.add(Pair("service", config.getAsJsonObject("sut").get("serviceToBenchmark").asString))
        val queryPath = config.getAsJsonObject("kiali").get("queryPath").asString
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
        val results = JsonParser().parse(request.response).asJsonObject.getAsJsonArray("data")
        val traces = ArrayList<TraceApiObject>()
        for (traceElement in results) {
            val trace = traceElement.asJsonObject
            val traceId = trace.get("traceID").asString
            var version = config.get("firstVersion").asString
            var traceStart = 0L
            var traceEnd = 0L
            var traceUrl = ""
            var traceMethod = ""
            var traceHeaderId = ""
            val spans = ArrayList<SpanApiObject>()
            for (spanElement in trace.getAsJsonArray("spans")) {
                val span = spanElement.asJsonObject
                val start = span.get("startTime").asLong
                if (start < traceStart || traceStart == 0L) traceStart = start
                val duration = span.get("duration").asLong
                val end = duration + start
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
                var headerId = ""
                for (tagElement in span.getAsJsonArray("tags")) {
                    val tag = tagElement.asJsonObject
                    when (tag.get("key").asString) {
                        "request_size" -> requestSize = tag.get("value").asInt
                        "response_size" -> responseSize = tag.get("value").asInt
                        "http.method" -> httpMethod = tag.get("value").asString
                        "http.status_code" -> httpStatus = tag.get("value").asInt
                        "http.url" -> httpUrl = tag.get("value").asString
                        "guid:x-request-id" -> headerId = tag.get("value").asString
                        "node_id" -> {
                            if (tag.get("value").asString.contains(
                                    config.getAsJsonObject("sut").get("serviceSecondVersion").asString
                                )
                            ) {
                                version = config.get("secondVersion").asString
                            }
                        }
                    }
                }
                //TODO: specific workaround
                if (process == "istio-ingressgateway") {
                    traceUrl = httpUrl
                    traceMethod = httpMethod
                    traceHeaderId = headerId

                }
                log.debug("Adding span - spanId:$spanId parentId:$parentId start:$start end:$end duration:$duration httpMethod:$httpMethod httpUrl:$httpUrl httpStatus:$httpStatus responseSize:$responseSize requestSize:$requestSize process:$process")
                spans.add(
                    SpanApiObject(
                        spanId,
                        parentId,
                        start,
                        end,
                        duration,
                        httpMethod,
                        httpUrl,
                        httpStatus,
                        responseSize,
                        requestSize,
                        process
                    )
                )
            }

            val duration = traceEnd - traceStart
            log.debug("Adding trace - traceId:$traceId version:$version traceUrl:$traceUrl traceMethod:$traceMethod traceHeaderId:$traceHeaderId traceStart:$traceStart duration:$duration traceEnd:$traceEnd")
            traces.add(
                TraceApiObject(
                    traceId,
                    version,
                    traceUrl,
                    traceMethod,
                    traceHeaderId,
                    traceStart,
                    traceEnd,
                    duration,
                    spans
                )
            )
        }
        return traces
    }

    fun retrievePrometheus(): ArrayList<PrometheusApiObject> {
        val prometheusPort = config.getAsJsonObject("prometheus").get("port").asString
        val prometheusQueries = config.getAsJsonObject("prometheus").getAsJsonObject("queries")
        val measurements = ArrayList<Pair<String, String>>()
        measurements.add(Pair("cpu", prometheusQueries.get("cpuQuery").asString))
        measurements.add(Pair("memory", prometheusQueries.get("memoryQuery").asString))
        measurements.add(Pair("sentBytes", prometheusQueries.get("sentBytesQuery").asString))
        measurements.add(Pair("receivedBytes", prometheusQueries.get("receivedBytesQuery").asString))
        val prometheusData = ArrayList<PrometheusApiObject>()
        for (measurement in measurements) {
            log.info("Retrieving prometheus ${measurement.first} information")
            val parameterList = ArrayList<Pair<String, String>>()
            parameterList.add(Pair("query", measurement.second))
            parameterList.add(Pair("start", start.toString()))
            parameterList.add(Pair("end", end.toString()))
            parameterList.add(Pair("step", config.getAsJsonObject("prometheus").get("step").asString))
            val queryPath = config.getAsJsonObject("prometheus").get("queryPath").asString
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

            val results =
                JsonParser().parse(request.response).asJsonObject.getAsJsonObject("data").getAsJsonArray("result")
            for (resultElem in results) {
                val type = measurement.first
                val result = resultElem.asJsonObject
                var pod = ""
                if (result.getAsJsonObject("metric").has("pod_name")) {
                    pod =
                        result.getAsJsonObject("metric").get("pod_name").asString
                    if (pod.contains("kube") || pod.contains(
                            "stackdriver"
                        ) || pod.contains("istiod") || pod.contains("istio-tracing") || pod.contains(
                            "fluentd"
                        ) || pod.contains("prometheus") || pod.contains("kiali") || pod.contains("grafana") || pod.contains(
                            "metrics-server"
                        ) || pod.contains("l7-default-backend") || pod.contains("istio-egress") || pod.contains("heapster-gke") || pod.contains(
                            "event-exporter"
                        )
                    ) continue
                }
                val values = result.getAsJsonArray("values")
                val metrics = ArrayList<PrometheusValuesObject>()
                for (valueElem in values) {
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
        return prometheusData
    }


}