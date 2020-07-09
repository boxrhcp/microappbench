package analyzer

import database.DatabaseOperator

class AnalyzerCore {

    fun execAnalysis() {
        val db = DatabaseOperator()
        val traces = db.aggregateTraces()
        for (trace in traces) {
            print("Trace ${trace.traceId} version ${trace.version}: ${trace.duration}")
        }
    }
}