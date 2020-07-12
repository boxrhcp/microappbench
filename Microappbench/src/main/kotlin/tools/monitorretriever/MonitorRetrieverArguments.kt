package tools.monitorretriever

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class MonitorRetrieverArguments(parser: ArgParser) {

    val start by parser.storing(
        "-s", "--start",
        help = "start time of benchmark"
    ) { toLong() }

    val end by parser.storing(
        "-e", "--end",
        help = "end time of benchmark"
    ) { toLong() }

    val clean by parser.flagging(
        "-c", "--clean",
        help = "clean existing db"
    ).default(false)
}