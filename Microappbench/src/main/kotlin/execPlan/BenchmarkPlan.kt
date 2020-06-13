package execPlan

import models.ArtifactObject
import org.slf4j.LoggerFactory

class BenchmarkPlan {
    private val log = LoggerFactory.getLogger("BenchMarkPlan")
    private val affected: ArrayList<ArtifactObject> = ArrayList()
    private val rootIssue: ArrayList<ArtifactObject> = ArrayList()

    fun executePlan(artifact: String, path: String): Boolean {
        val root = recursiveBenchmark(ArtifactObject(artifact, path))

        if (affected.size > 0) {
            return true
        } else {
            for (parent in root.parentList) {
                log.info("Benchmarking artifact " + root.name)
                //benchmark parent
                if (parent.performanceIssue) {
                    affected.add(parent)
                }
            }
            if (affected.size > 0) {
                rootIssue.add(root)
                return true
            }
        }
        return false
    }

    private fun recursiveBenchmark(artifact: ArtifactObject): ArtifactObject {
        //benchmark artifact

        if (artifact.performanceIssue) {
            affected.add(artifact)
            if (artifact.childList.isNotEmpty()) {
                for (child in artifact.childList) {
                    recursiveBenchmark(child)
                }
            } else {
                rootIssue.add(artifact)
            }
        }
        return artifact
    }
}