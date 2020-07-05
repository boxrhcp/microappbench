package tools.benchmarkrunner

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class BenchmarkRunnerArguments(parser: ArgParser) {
    val overwrite by parser.flagging(
        "-o", "--overwrite",
        help = "overwrite existing mapping file"
    ).default(false)

    val build by parser.flagging(
        "-b", "--build",
        help = "build the workload tool"
    ).default(false)

    val ipToBenchmark: String by parser.storing(
        "-i", "--ip",
        help = "ip of the artifact to benchmark"
    )

    val firstVersion: String by parser.storing(
        "-f", "--firstVersion",
        help = "first version to benchmark"
    ).default("v1")

    val secondVersion: String by parser.storing(
        "-s", "--secondVersion",
        help = "second version to benchmark"
    ).default("v2")
}
