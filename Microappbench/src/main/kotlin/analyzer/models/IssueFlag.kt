package analyzer.models

import api.models.MetricType

enum class IssueFlag (val flagName: String) {
    CPU(MetricType.CPU.typeName),
    MEMORY(MetricType.MEMORY.typeName),
    SENT_BYTES(MetricType.SENT_BYTES.typeName),
    RECEIVED_BYTES(MetricType.RECEIVED_BYTES.typeName),
    EXEC_TIME("execTime"),
    REQ_SIZE("requestSize"),
    RES_SIZE("responseSize"),
    CALL_ERROR("callError"),
    CALL_MISMATCH("callMismatch"),
    CHILD_MISMATCH("childMismatch")
}