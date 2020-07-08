package tools.monitorretriever

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class MonitorRetrieverArguments(parser: ArgParser) {
    val overwrite by parser.flagging(
        "-o", "--overwrite",
        help = "overwrite existing mapping file"
    ).default(false)

    val baseUrl: String by parser.storing(
        "-u", "--baseUrl",
        help = "path of artifact to benchmark"
    ).default("http://localhost")

    val prometheusPort: String by parser.storing(
        "-p", "--prometheusPort",
        help = "path of artifact to benchmark"
    ).default("9090")

    val kialiPort: String by parser.storing(
        "-k", "--kialiPort",
        help = "path of artifact to benchmark"
    ).default("20001")

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