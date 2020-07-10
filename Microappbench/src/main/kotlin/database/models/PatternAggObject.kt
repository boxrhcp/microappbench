package database.models

import java.math.BigDecimal

data class PatternAggObject (
    val resource: String,
    val version: String,
    val patternName: String,
    val durationAvg: BigDecimal
){
}