package workload

import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import models.ApiRequestObject
import run.ApiHandler
import java.util.*
import kotlin.collections.ArrayList

class MonitoringRetrieval(
    val baseUrl: String,
    val kialiPort: String,
    val prometheusPort: String,
    val start: String,
    val end: String
) {

    private val apiHandler = ApiHandler()

    fun retrieveKiali() {
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

    }

    fun retrievePrometheus() {
        val parameterList = ArrayList<Pair<String, String>>()
        parameterList.add(
            Pair(
                "query",
                "sum(rate(container_cpu_usage_seconds_total{container_name!=\"POD\",pod_name!=\"\"}[5m])) by (pod_name)"
            )
        )
        parameterList.add(Pair("start", start))
        parameterList.add(Pair("end", end))
        val queryPath = "/api/v1/query"
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

    }


}