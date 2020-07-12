package json

import com.google.gson.JsonParser
import json.models.OperationJson
import json.models.PatternJson
import org.slf4j.LoggerFactory
import java.io.File

class JsonFileHandler {
    private val log = LoggerFactory.getLogger("JsonFileHandler")!!

    fun loadJsonFile(service: String, version: String, dir: String): ArrayList<PatternJson> {
        val results = ArrayList<PatternJson>()
        val json =
            JsonParser().parse(File("$dir/results-$service-$version.json").readText(Charsets.UTF_8)).asJsonArray
        for (patternElem in json) {
            val pattern = patternElem.asJsonObject
            val operations = ArrayList<OperationJson>()
            for (operationElem in pattern.getAsJsonArray("apiRequestMeasurements")) {
                val operation = operationElem.asJsonObject
                val path = operation.get("path").asString
                val operationName = operation.get("abstractOperation").asString
                val concretePath = operation.get("concretePath").asString
                val concreteMethod = operation.get("concreteMethod").asString
                val headerId = operation.get("headerId").asString
                val index = operation.get("index").asInt
                val start = operation.get("start").asLong
                val end = operation.get("end").asLong
                val duration = end - start
                log.debug("Loading operation from json file to db - path:$path operationName:$operationName concretePath:$concretePath concreteMethod:$concreteMethod headerId:$headerId index:$index start:$start end:$end duration:$duration")
                operations.add(
                    OperationJson(
                        path,
                        operationName,
                        concretePath,
                        concreteMethod,
                        headerId,
                        index,
                        start,
                        end,
                        duration
                    )
                )
            }
            val resource = pattern.get("resource").asString
            val patternName = pattern.get("patternName").asString
            val requestId = pattern.get("requestID").asInt
            val workerId = pattern.get("workerID").asInt
            val start = pattern.get("start").asLong
            val end = pattern.get("end").asLong
            val duration = end - start
            log.debug("Loading pattern from json to db - resource:$resource version:$version patternName:$patternName requestId:$requestId workerId:$workerId start:$start end:$end duration:$duration")
            results.add(
                PatternJson(
                    resource,
                    version,
                    patternName,
                    requestId,
                    workerId,
                    start,
                    end,
                    duration,
                    operations
                )
            )
        }
        return results
    }
}