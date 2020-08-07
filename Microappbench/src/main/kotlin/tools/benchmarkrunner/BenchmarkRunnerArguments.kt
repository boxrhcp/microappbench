package tools.benchmarkrunner

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class BenchmarkRunnerArguments(parser: ArgParser) {

    val build by parser.flagging(
        "-b", "--build",
        help = "build the workload tool"
    ).default(false)

    val verbose by parser.flagging(
        "-v", "--verbose",
        help = "print verbose"
    ).default(false)
}
