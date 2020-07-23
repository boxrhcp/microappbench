package tools.analyzer

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class AnalyzerArguments(parser: ArgParser) {
    val resultsFileName: String by parser.storing(
        "-o", "--output",
        help = "name of the file to store results"
    ).default("results.json")

}