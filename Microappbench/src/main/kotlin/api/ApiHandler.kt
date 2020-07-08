package api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import api.models.ApiRequestObject
import org.slf4j.LoggerFactory

class ApiHandler {
    private val log = LoggerFactory.getLogger("ApiHandler")!!

    suspend fun makeApiRequest(apiRequest: ApiRequestObject) {
        val maxContentLen = 200
        val client = HttpClient()
        var url = apiRequest.path
        url = buildUrl(url, apiRequest.parameter)
        log.debug("URL is $url")
        with(apiRequest.method) {
            log.info("Sending ${apiRequest.method} to $url, body: ${apiRequest.body}")
            when {
                equals("POST") -> {
                    val response = client.post<HttpResponse>(url) {
                        method = HttpMethod.Post
                        body =
                            TextContent(apiRequest.body.toString(), contentType = ContentType.Application.Json)

                        if (apiRequest.headers.isNotEmpty()) {
                            for (h in apiRequest.headers) {
                                log.debug("Add header: ${h.first}, ${h.second}")
                                headers.append(h.first, h.second)
                            }
                        }
                        headers.append("Accept", "application/json")
                    }

                    val responseText = response.readText()
                    var logtext = responseText
                    if (logtext.length > maxContentLen) {
                        logtext = logtext.substring(0, maxContentLen - 2) + "..."
                    }
                    log.info("Responded (${response.status.value}) $logtext")
                    apiRequest.response = responseText
                    apiRequest.status = response.status.value
                    client.close()

                }
                equals("GET") -> {
                    val response = client.get<HttpResponse>(url) {
                        method = HttpMethod.Get
                        body = TextContent(apiRequest.body.toString(), contentType = ContentType.Application.Json)
                        if (apiRequest.headers.isNotEmpty()) {
                            for (h in apiRequest.headers) {
                                log.debug("Add header: ${h.first}, ${h.second}")
                                headers.append(h.first, h.second)
                            }
                        }
                        headers.append("Accept", "application/json")

                    }
                    val responseText = response.readText()
                    var logtext = responseText
                    if (logtext.length > maxContentLen) {
                        logtext = logtext.substring(0, maxContentLen - 2) + "..."
                    }
                    log.info("Responded (${response.status.value}) $logtext")
                    apiRequest.response = responseText
                    apiRequest.status = response.status.value
                    client.close()

                }
            }
        }
    }


    private fun buildUrl(url: String, parameter: Array<Pair<String, String>>): String {
        var result = url

        //replace {pathParameter}
        while (result.contains("{") && result.contains("}")) {
            val first = result.substring(0, result.indexOf("{"))
            var paramName = result.substring(result.indexOf("{") + 1, result.indexOf("}"))
            val second = result.substring(result.indexOf("}") + 1)

            //Replace paramName with actual value given in parameters
            for (i in parameter.indices) {
                if (parameter[i].first == paramName) {
                    paramName = parameter[i].second
                    //paramName = URLEncoder.encode(parameter[i].second.toString(),"UTF-8")
                    parameter[i] = Pair("", "")
                }
            }
            //concat together
            result = "$first$paramName$second"
        }


        var firstParameter = true
        for (p in parameter) {
            if (p.first != "") {
                if (firstParameter) {
                    result += "?" + p.first + "=" + p.second
                    firstParameter = false
                } else {
                    result += "&" + p.first + "=" + p.second
                }
            }
        }
        return result
    }
}