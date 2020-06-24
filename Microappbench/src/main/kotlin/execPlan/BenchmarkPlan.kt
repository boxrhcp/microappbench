package execPlan

import models.ArtifactObject
import org.slf4j.LoggerFactory
import run.ScriptRunner
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BenchmarkPlan (address: String) {
    private val log = LoggerFactory.getLogger("BenchMarkPlan")
    private val affected: ArrayList<ArtifactObject> = ArrayList()
    private val rootIssue: ArrayList<ArtifactObject> = ArrayList()
    private val scriptRunner: ScriptRunner = ScriptRunner()
    private val address: String = address


    fun executePlan(artifact: String, path: String): Boolean {
        return true
    }

    private fun executeBenchmark(artifact: ArtifactObject) {
        //benchmark artifact
        scriptRunner.executeOpenISBT(false, address, "v1", "8000")

    }
}