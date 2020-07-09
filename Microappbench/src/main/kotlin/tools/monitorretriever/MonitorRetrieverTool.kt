package tools.monitorretriever

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.slf4j.LoggerFactory
import workload.MonitoringRetriever

val log = LoggerFactory.getLogger("MonitorRetriever")!!

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::MonitorRetrieverArguments).run {
        val retriever = MonitoringRetriever(baseUrl, kialiPort, prometheusPort, start, end)
        if (clean) retriever.clean()
        //retriever.downloadKiali()
        //retriever.downloadPrometheus()
        retriever.loadOpenISBTResults("v1")
        retriever.loadOpenISBTResults("v2")

        log.info("end")
    }
}
