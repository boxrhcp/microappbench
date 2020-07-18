package analyzer.models

import api.ApiRetrieval
import api.models.PrometheusApiObject
import utils.ResourceManager
import java.util.concurrent.TimeUnit

class ServiceNode(val service: String) {
    private val metrics = HashMap<String, ArrayList<PrometheusApiObject>>()

    fun getPrometheusData(start: Long, end: Long) {
        val retriever = ApiRetrieval( ResourceManager.loadConfigFile())
        val metricsList = retriever.retrievePrometheus(TimeUnit.MILLISECONDS.toSeconds(start), TimeUnit.MILLISECONDS.toSeconds(end))
        for (metric in metricsList){
            if(metrics.containsKey(metric.type)){
                metrics[metric.type]!!.add(metric)
            }else{
                val newType = ArrayList<PrometheusApiObject>()
                newType.add(metric)
                metrics[metric.type] = newType
            }
        }
    }
}