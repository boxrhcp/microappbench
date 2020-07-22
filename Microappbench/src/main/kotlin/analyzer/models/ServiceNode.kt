package analyzer.models

import api.ApiRetrieval
import api.models.MetricType
import org.slf4j.LoggerFactory
import utils.ResourceManager
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


data class ServiceNode(val service: String, val version: String) {
    private val log = LoggerFactory.getLogger("ServiceNode")!!
    private val metricsAvg = HashMap<String, BigDecimal>()
    val flags: ArrayList<String> = ArrayList()

    fun getPrometheusData(start: Long, end: Long) {
        val config = ResourceManager.getConfigFile()
        val retriever = ApiRetrieval(config)
        val metricsList =
            retriever.retrievePrometheus(TimeUnit.MICROSECONDS.toSeconds(start), TimeUnit.MICROSECONDS.toSeconds(end))
        var serviceName = service
        // In case we find orders, difference between both versions
        if (service == config.getAsJsonObject("sut").get("serviceToBenchmark").asString) {
            serviceName = if (version == config.get("firstVersion").asString) {
                config.getAsJsonObject("sut").get("serviceFirstVersion").asString
            } else {
                config.getAsJsonObject("sut").get("serviceSecondVersion").asString
            }
        }
        //TODO: filter services
        for (type in MetricType.values()) {
            val find = metricsList.stream().filter{ it.pod.contains(serviceName) && it.type == type.typeName }.findAny()
            if(find.isPresent){
                val list = find.get()
                var average = BigDecimal(0)
                for (metric in list.values) {
                    average += metric.value
                }
                metricsAvg[type.typeName] = average / BigDecimal(list.values.size)
            }else{
                log.error("This metric key ${type.typeName} is not available for service $service")
            }
        }
    }

    fun getMetricAvgByType(key: String): BigDecimal?{
        if (!metricsAvg.containsKey(key)) {
            log.error("This metric type $key is not available for service $service")
            return null
        }
        return metricsAvg[key]
    }
}