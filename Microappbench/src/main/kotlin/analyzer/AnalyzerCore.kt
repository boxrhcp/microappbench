package analyzer

import database.DatabaseOperator
import org.slf4j.LoggerFactory

class AnalyzerCore {
    private val log = LoggerFactory.getLogger("AnalyzerCore")!!


    fun execAnalysis() {
        val db = DatabaseOperator()
        val patterns = db.aggregatePatterns()
        for (pattern in patterns) {
            log.info("resource ${pattern.resource} version ${pattern.version} pattern ${pattern.patternName}: ${pattern.durationAvg}")
        }
    }
}