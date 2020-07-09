package database.models

import java.math.BigDecimal

data class TraceAggObject(val traceId: String, val version: String, val duration: BigDecimal) {
}