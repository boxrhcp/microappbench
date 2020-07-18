package analyzer.models

import database.models.SpanObject

data class SpanNode (val span: SpanObject, val children: ArrayList<SpanNode>) {
    var caller = ServiceNode("")
    var callee = ServiceNode("")

    init{
        caller = ServiceNode(span.process.substringBefore('.'))
        callee = ServiceNode(getServiceFromUrl(span.httpUrl))
        caller.getPrometheusData(span.start, span.end)
        callee.getPrometheusData(span.start, span.end)
    }

    private fun getServiceFromUrl(url: String): String{
        val host = url.substringAfter("http://")
        return if(host.contains("\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b")){
            host.substringAfter('/').substringBefore('/')
        }else{
            host.substringBefore('/')
        }
    }

}