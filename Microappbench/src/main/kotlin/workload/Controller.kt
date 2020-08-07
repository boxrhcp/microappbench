package workload

import org.slf4j.LoggerFactory
import run.ScriptRunner

class Controller(private val clean: Boolean, private val verbose: Boolean) {
    private val log = LoggerFactory.getLogger("Controller")!!

    fun execute() {
        //TODO: fault tolerance
        log.info("Building the SUT")
        val runner = ScriptRunner(verbose)
        runner.bootSUT()
        log.info("Running the benchmark")
        runner.executeBenchmarkRunner(verbose)
        log.info("Retrieving the benchmark results")
        runner.executeMonitorRetriever(clean)
        log.info("Analyzing data")
        runner.executeAnalyzer()
    }
}