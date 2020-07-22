package analyzer.models

import database.models.SpanObject
import org.slf4j.LoggerFactory

data class SpanNode (val span: SpanObject, val children: ArrayList<SpanNode>) {
    private val log = LoggerFactory.getLogger("SpanNode")!!
    var caller = ServiceNode("", span.version)
    var callee = ServiceNode("", span.version)

    init{
        caller = ServiceNode(span.process.substringBefore('.'), span.version)
        callee = ServiceNode(getServiceFromUrl(span.httpUrl), span.version)
        caller.getPrometheusData(span.start, span.end)
        callee.getPrometheusData(span.start, span.end)
    }

    private fun getServiceFromUrl(url: String): String{
        val host = url.substringAfter("http://")
        //TODO: workaround fix to ip error
        return if(host.contains("orders")){
            "orders"
        }else{
            host.substringBefore('/')
        }
    }

}