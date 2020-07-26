package analyzer.models.report

import java.math.BigDecimal

data class ServiceReport (
    val name: String,
    val cpuUsage: BigDecimal,
    val memoryUsage: BigDecimal
    //val sentByes: BigDecimal,
    //val receivedBytes: BigDecimal
) {
}