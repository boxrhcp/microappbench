package workload

import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import run.ScriptRunner
import utils.ResourceManager

class WorkloadGenerator() {
    private val log = LoggerFactory.getLogger("WorkloadGenerator")!!
    private val config = ResourceManager.loadConfigFile()

    fun executeBenchmark(ipToBenchmark: String, build: Boolean) {
        val firstVersion = config.get("firstVersion").asString
        val secondVersion = config.get("secondVersion").asString
        val runner = ScriptRunner()
        runner.prepareOpenISBT(build, ipToBenchmark, firstVersion)
        runner.prepareOpenISBT(false, ipToBenchmark, secondVersion)
        val start = DateTime.now().millis
        runner.executeOpenISBT(ipToBenchmark, firstVersion, "8001")
        runner.executeOpenISBT(ipToBenchmark, secondVersion, "8002")
        val end = DateTime.now().millis
        log.debug("Start time: $start")
        log.debug("End time: $end")
    }
}