package workload

import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import run.ScriptRunner
import utils.ResourceManager

class WorkloadGenerator(private val verbose: Boolean) {
    private val log = LoggerFactory.getLogger("WorkloadGenerator")!!
    private val config = ResourceManager.getConfigFile()

    fun executeBenchmark(build: Boolean) {
        val firstVersion = config.get("firstVersion").asString
        val secondVersion = config.get("secondVersion").asString
        val ipToBenchmark = config.getAsJsonObject("sut").get("address").asString
        val service = config.getAsJsonObject("sut").get("serviceToBenchmark").asString
        val runner = ScriptRunner(verbose)
        runner.prepareOpenISBT(build, ipToBenchmark, service, firstVersion)
        runner.prepareOpenISBT(false, ipToBenchmark, service, secondVersion)
        val start = DateTime.now().millis
        runner.executeOpenISBT(ipToBenchmark, service, firstVersion, "8001")
        runner.executeOpenISBT(ipToBenchmark, service, secondVersion, "8002")
        val end = DateTime.now().millis
        log.debug("Start time: $start")
        log.debug("End time: $end")
    }
}