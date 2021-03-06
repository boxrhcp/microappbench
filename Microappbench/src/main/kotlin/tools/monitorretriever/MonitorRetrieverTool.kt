package tools.monitorretriever

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.slf4j.LoggerFactory
import workload.MonitoringRetriever

val log = LoggerFactory.getLogger("MonitorRetriever")!!

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::MonitorRetrieverArguments).run {
        println("Starting Monitor Retriever module...")
        val startTime = System.currentTimeMillis()
        val retriever = MonitoringRetriever(start, end)
        if (clean) retriever.clean()
        retriever.downloadKiali()
        //retriever.downloadPrometheus()
        retriever.loadOpenISBTResults()
        val duration = System.currentTimeMillis() - startTime
        tools.benchmarkrunner.log.info("Execution time: $duration" )
        println("Monitor Retriever done")
    }
}
