package tools.analyzer

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import database.DatabaseOperator
import org.slf4j.LoggerFactory
import workload.Analyzer
import java.io.File

val log = LoggerFactory.getLogger("Analyzer")!!

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::AnalyzerArguments).run {
        val analyzer = Analyzer()
        val reports = analyzer.execAnalysis()
        val gson: Gson = GsonBuilder().create()
        File(resultsFileName).writeText(gson.toJson(reports))
        println("Done. See measurements in " + File(resultsFileName).absoluteFile)
    }
}