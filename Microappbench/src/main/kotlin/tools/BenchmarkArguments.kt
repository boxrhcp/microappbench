package tools

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class BenchmarkArguments(parser: ArgParser) {
    val overwrite by parser.flagging(
        "-o", "--overwrite",
        help = "overwrite existing mapping file").default(false)
    val artifactToBenchmark: String by parser.storing(
        "-a", "--artifact",
        help = "artifact to benchmark")

    val artifactPath: String by parser.storing(
        "-p", "--path",
        help = "path of artifact to benchmark")
}
