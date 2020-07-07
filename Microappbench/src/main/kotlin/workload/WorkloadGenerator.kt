package workload

import org.slf4j.LoggerFactory
import run.ScriptRunner

class WorkloadGenerator () {
    private val log = LoggerFactory.getLogger("Workload Generator")!!


    fun executeBenchmark(ipToBenchmark: String, firstVersion: String, secondVersion: String) {
        val runner = ScriptRunner()
        runner.prepareOpenISBT(false, ipToBenchmark, firstVersion)
        runner.prepareOpenISBT(false, ipToBenchmark, secondVersion)
        runner.executeOpenISBT(ipToBenchmark, firstVersion, "8001")
        runner.executeOpenISBT( ipToBenchmark, secondVersion, "8002")
    }
}