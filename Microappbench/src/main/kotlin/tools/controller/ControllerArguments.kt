package tools.controller

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class ControllerArguments(parser: ArgParser) {
    val clean by parser.flagging(
        "-c", "--clean",
        help = "clean existing db"
    ).default(false)

    val verbose by parser.flagging(
        "-v", "--verbose",
        help = "print verbose"
    ).default(false)

}