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
        runner.prepareOpenISBT(false, artifactToBenchmark, "v1")
        runner.prepareOpenISBT(false, artifactToBenchmark, "v2")
        runner.executeOpenISBT(artifactToBenchmark, "v1", "8001")
        runner.executeOpenISBT( artifactToBenchmark, "v2", "8002")

    }
}
