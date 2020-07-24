package api.models

enum class MetricType(val typeName: String) {
    CPU("cpu"),
    MEMORY("memory")
    //SENT_BYTES("sentBytes"),
    //RECEIVED_BYTES("receivedBytes")
}