package tools.analyzer

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("MonitorRetriever")!!

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::AnalyzerArguments).run {

    }
}