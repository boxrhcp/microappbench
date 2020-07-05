package tools.monitorretriever

import workload.MonitoringRetrieval
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("MonitorRetriever")!!

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::MonitorRetrieverArguments).run {
        val retriever = MonitoringRetrieval(baseUrl, kialiPort, prometheusPort, start, end)
        //retriever.retrieveKiali()
        retriever.retrievePrometheus()
    }
}
