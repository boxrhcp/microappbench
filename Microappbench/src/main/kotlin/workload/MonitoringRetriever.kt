package workload

import api.ApiRetrieval
import database.DatabaseOperator
import json.JsonFileHandler
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import utils.ResourceManager
import kotlin.coroutines.coroutineContext

class MonitoringRetriever(val start: Long, val end: Long) {
    private val config = ResourceManager.getConfigFile()
    private val log = LoggerFactory.getLogger("MonitoringRetriever")!!
    private val retriever = ApiRetrieval(
        config
    )
    private val loader = JsonFileHandler()
    private val db = DatabaseOperator()

    fun clean() {
        db.dropTables()
        db.createTables()
    }

    fun downloadKiali() = runBlocking {
        val traces = retriever.retrieveKiali()
        try {
            coroutineScope {
                traces.map {
                    async(Dispatchers.IO) {
                        val traceId = db.insertTrace(
                            it.traceId,
                            it.version,
                            it.traceUrl,
                            it.traceMethod,
                            it.headerId,
                            it.start,
                            it.end,
                            it.duration
                        )
                        for (span in it.spans) {
                            db.insertSpan(
                                span.spanId,
                                traceId,
                                it.version,
                                span.start,
                                span.end,
                                span.duration,
                                span.process,
                                span.httpMethod,
                                span.httpStatus,
                                span.httpUrl,
                                span.requestSize,
                                span.responseSize,
                                span.parentId
                            )
                        }
                    }
                }
            }.awaitAll()
        } catch (e: Exception) {
            log.error("Error loading Kiali API data to db.")
            log.debug("${e.message}")
        }

    }

    fun downloadPrometheus() {
        val prometheusData = retriever.retrievePrometheus(start, end)
        try {
            for (data in prometheusData) {
                for (value in data.values) {
                    db.insertPrometheusData(data.type, data.pod, value.time, value.value)
                }
            }
        } catch (e: Exception) {
            log.error("Error loading prometheus API data to db.")
            log.debug("${e.message}")
        }
    }

    fun loadOpenISBTResults() {
        loadOpenISBTJson(config.get("firstVersion").asString)
        loadOpenISBTJson(config.get("secondVersion").asString)
    }

    private fun loadOpenISBTJson(version: String) = runBlocking {
        val jsonData = loader.loadJsonFile(
            config.getAsJsonObject("sut").get("serviceToBenchmark").asString,
            version,
            config.get("benchmarkResultsDir").asString
        )
        try {
            coroutineScope {
                jsonData.map {
                    async(Dispatchers.IO) {
                        val patternId = db.insertPattern(
                            it.resource,
                            it.version,
                            it.patternName,
                            it.requestId,
                            it.workerId,
                            it.start,
                            it.end,
                            it.duration
                        )
                        for (operation in it.operations) {
                            db.insertOperation(
                                patternId,
                                operation.path,
                                operation.operation,
                                operation.concretePath,
                                operation.concreteMethod,
                                operation.headerId,
                                operation.index,
                                operation.start,
                                operation.end,
                                operation.duration
                            )
                        }
                    }
                }
            }.awaitAll()
        } catch (e: Exception) {
            log.error("Error loading openISBT data to db.")
            log.debug("${e.message}")
        }
    }
}