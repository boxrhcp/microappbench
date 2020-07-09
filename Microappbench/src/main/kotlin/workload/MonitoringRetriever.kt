package workload

import api.ApiRetrieval
import database.DatabaseOperator
import json.JsonFileHandler
import org.slf4j.LoggerFactory

class MonitoringRetriever(baseUrl: String, kialiPort: String, prometheusPort: String, start: Long, end: Long) {
    private val log = LoggerFactory.getLogger("MonitoringRetriever")!!
    private val retriever = ApiRetrieval(baseUrl, kialiPort, prometheusPort, start, end)
    private val loader = JsonFileHandler()
    private val db = DatabaseOperator()

    fun clean() {
        db.dropTables()
        db.createTables()
    }

    fun downloadKiali() {
        val traces = retriever.retrieveKiali()
        try {
            for (trace in traces) {
                val traceId = db.insertTrace(
                    trace.traceId,
                    trace.version,
                    trace.traceUrl,
                    trace.traceMethod,
                    trace.start,
                    trace.end,
                    trace.duration
                )
                for (span in trace.spans) {
                    db.insertSpan(
                        span.spanId,
                        traceId,
                        trace.version,
                        span.start,
                        span.end,
                        span.duration,
                        span.process,
                        span.httpMethod,
                        span.httpStatus,
                        span.httpUrl,
                        span.requestSize,
                        span.responseSize
                    )
                }
            }
        } catch (e: Exception) {
            log.error("Error loading kiali API data to db: ${e.message}")
        }

    }

    fun downloadPrometheus() {
        val prometheusData = retriever.retrievePrometheus()
        try {
            for (data in prometheusData) {
                for (value in data.values) {
                    db.insertPrometheusData(data.type, data.pod, value.time, value.value)
                }
            }
        } catch (e: Exception) {
            log.error("Error loading prometheus API data to db: ${e.message}")
        }
    }

    fun loadOpenISBTResults(version: String) {
        val jsonData = loader.loadJsonFile(version)
        try {
            for (pattern in jsonData) {
                val patternId = db.insertPattern(
                    pattern.resource,
                    pattern.version,
                    pattern.patternName,
                    pattern.requestId,
                    pattern.workerId,
                    pattern.start,
                    pattern.end,
                    pattern.duration
                )
                for (operation in pattern.operations) {
                    db.insertOperation(
                        patternId,
                        operation.path,
                        operation.operation,
                        operation.concretePath,
                        operation.concreteMethod,
                        operation.index,
                        operation.start,
                        operation.end,
                        operation.duration
                    )
                }
            }

        } catch (e: Exception) {
            log.error("Error loading openISBT json file data to db: ${e.message}")
        }
    }
}