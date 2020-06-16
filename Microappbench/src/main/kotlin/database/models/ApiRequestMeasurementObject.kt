package database.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object ApiRequestMeasurementObject : IntIdTable() {
    val path : Column<String> = varchar("path",50)
    val abstractOperation : Column<String> = varchar("abstract_operation", 50)
    val index : Column<Int> = integer("index")
    val start : Column<Long> = long("start")
    val end : Column<Long> = long("end")
    val benchmarkId : Column<Int> = integer("bechmark_id").references(BenchmarkResultObject.id)
}