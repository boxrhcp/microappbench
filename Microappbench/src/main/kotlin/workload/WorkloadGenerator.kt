package workload

import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import run.ScriptRunner

class WorkloadGenerator () {
    private val log = LoggerFactory.getLogger("WorkloadGenerator")!!


    fun executeBenchmark(ipToBenchmark: String, firstVersion: String, secondVersion: String, build: Boolean) {
        val runner = ScriptRunner()
        runner.prepareOpenISBT(build, ipToBenchmark, firstVersion)
        runner.prepareOpenISBT(false, ipToBenchmark, secondVersion)
        val start = DateTime.now().millis
        runner.executeOpenISBT(ipToBenchmark, firstVersion, "8001")
        runner.executeOpenISBT( ipToBenchmark, secondVersion, "8002")
        val end = DateTime.now().millis
        log.debug("Start time: $start")
        log.debug("End time: $end")
    }
}