package tools.controller

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.slf4j.LoggerFactory
import workload.Controller

val log = LoggerFactory.getLogger("ExperimentController")!!

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::ExperimentControllerArguments).run {
        val controller = Controller(clean, verbose)
        controller.execute()
    }
    log.info("end")
}
