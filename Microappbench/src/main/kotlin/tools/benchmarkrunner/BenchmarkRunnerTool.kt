package tools.benchmarkrunner

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.slf4j.LoggerFactory
import workload.WorkloadGenerator


val log = LoggerFactory.getLogger("BenchmarkTool")!!

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::BenchmarkRunnerArguments).run {
        println("Starting benchmark module...")
        val startTime = System.currentTimeMillis()
        val generator = WorkloadGenerator(verbose)
        generator.executeBenchmark(ipToBenchmark, build)
        val duration = System.currentTimeMillis() - startTime
        log.info("Execution time: $duration" )
        println("Benchmark done.")
    }
}
