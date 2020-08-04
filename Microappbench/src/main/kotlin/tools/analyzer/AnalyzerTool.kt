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
        println("Starting analyzer module...")
        val startTime = System.currentTimeMillis()
        val analyzer = Analyzer()
        val reports = analyzer.execAnalysis()
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        File(resultsFileName).writeText(gson.toJson(reports))
        val duration = System.currentTimeMillis() - startTime
        log.info("Execution time: $duration" )
        println("Analysis done. See results in " + File(resultsFileName).absoluteFile)
    }
}