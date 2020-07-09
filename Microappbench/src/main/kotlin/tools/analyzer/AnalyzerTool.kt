package tools.analyzer

import analyzer.AnalyzerCore
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import database.DatabaseOperator
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("Analyzer")!!

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::AnalyzerArguments).run {
        val core = AnalyzerCore()
        core.execAnalysis()
    }
}