package tools.analyzer

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class AnalyzerArguments(parser: ArgParser) {
    val clean by parser.flagging(
        "-c", "--clean",
        help = "clean existing db"
    ).default(false)
}