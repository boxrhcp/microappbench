package tools

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class BenchmarkArguments(parser: ArgParser) {
    val overwrite by parser.flagging(
        "-o", "--overwrite",
        help = "overwrite existing mapping file").default(false)

    val artifactToBenchmark by parser.adding(
        "-a", "--artifact",
        help = "path which should be excluded from mapping")
}
