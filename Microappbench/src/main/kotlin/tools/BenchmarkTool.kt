package tools

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import execPlan.BenchmarkPlan
import org.slf4j.LoggerFactory


class BenchmarkTool {

    val  log = LoggerFactory.getLogger("BenchmarkTool")!!

    fun main(args: Array<String>) = mainBody  {
        ArgParser(args).parseInto(::BenchmarkArguments).run {
            val benchmarkPlan : BenchmarkPlan = BenchmarkPlan()
            benchmarkPlan.executePlan(artifactToBenchmark, artifactPath)
        }
    }
}