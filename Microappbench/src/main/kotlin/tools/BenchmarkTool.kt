package tools

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.slf4j.LoggerFactory
import run.ScriptRunner


val log = LoggerFactory.getLogger("BenchmarkTool")!!

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::BenchmarkArguments).run {
        //val benchmarkPlan : BenchmarkPlan = BenchmarkPlan()
        //benchmarkPlan.executePlan(artifactToBenchmark, artifactPath)
        val runner: ScriptRunner = ScriptRunner()
        runner.executeOpenISBT(false, "34.78.24.189", "v1", "8001")
        runner.executeOpenISBT(false, "34.78.24.189", "v2", "8002")

    }
}
