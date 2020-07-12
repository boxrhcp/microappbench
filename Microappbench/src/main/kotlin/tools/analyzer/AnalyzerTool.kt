package tools.analyzer

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import database.DatabaseOperator
import org.slf4j.LoggerFactory
import workload.Analyzer

val log = LoggerFactory.getLogger("Analyzer")!!

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::AnalyzerArguments).run {
        val analyzer = Analyzer()
        analyzer.execAnalysis()
    }
}