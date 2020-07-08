package models

import java.math.BigDecimal

data class PrometheusValuesObject(
    val time: Long,
    val value: BigDecimal
) {
}