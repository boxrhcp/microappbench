package tools.controller

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.slf4j.LoggerFactory
import workload.Controller

val log = LoggerFactory.getLogger("ExperimentController")!!

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::ControllerArguments).run {
        println("Starting app execution...")
        val startTime = System.currentTimeMillis()
        val controller = Controller(clean, verbose)
        controller.execute()
        val duration = System.currentTimeMillis() - startTime
        tools.benchmarkrunner.log.info("Execution time: $duration" )
        println("Tool execution done.")
    }
}
