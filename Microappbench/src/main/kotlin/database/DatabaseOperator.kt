package database

import database.models.PatternAggObject
import database.models.TraceAggObject
import database.tables.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.math.BigDecimal

class DatabaseOperator {
    private val log = LoggerFactory.getLogger("DatabaseOperator")!!

    private val db = Database.db

    fun createTables() {
        log.info("Creating db and tables")
        transaction {
            SchemaUtils.create(Patterns, Operations, PrometheusDatas, Traces, Spans)
            commit()
        }
        log.info("Db creation successful")
    }

    fun dropTables() {
        log.info("Dropping db and tables")
        transaction {
            SchemaUtils.drop(Patterns, Operations, PrometheusDatas, Spans, Traces)
            commit()
        }
        log.info("Drop db successful")
    }

    fun insertPattern(
        _resource: String,
        _version: String,
        _patternName: String,
        _requestId: Int,
        _workId: Int,
        _start: Long,
        _end: Long,
        _duration: Long
    ): Int {
        log.info("inserting pattern")
        var result = 0
        transaction {
            val id = Patterns.insertAndGetId {
                it[resource] = _resource
                it[version] = _version
                it[patternName] = _patternName
                it[requestId] = _requestId
                it[workerId] = _workId
                it[start] = _start
                it[end] = _end
                it[duration] = _duration
            }
            commit()
            result = id.value
        }
        return result
    }

    fun insertOperation(
        _patternId: Int,
        _path: String,
        _operation: String,
        _concretePath: String,
        _concreteMethod: String,
        _index: Int,
        _start: Long,
        _end: Long,
        _duration: Long
    ): Int {
        log.info("inserting operation")
        var result = 0
        transaction {
            val id = Operations.insertAndGetId {
                it[patternId] = _patternId
                it[path] = _path
                it[operation] = _operation
                it[concretePath] = _concretePath
                it[concreteMethod] = _concreteMethod
                it[index] = _index
                it[start] = _start
                it[end] = _end
                it[duration] = _duration
            }
            commit()
            result = id.value
        }
        return result
    }

    fun insertTrace(
        _traceId: String,
        _version: String,
        _traceUrl: String,
        _traceMethod: String,
        _start: Long,
        _end: Long,
        _duration: Long
    ): Int {
        log.info("inserting trace")
        var result = 0
        transaction {
            val id = Traces.insertAndGetId {
                it[traceId] = _traceId
                it[version] = _version
                it[traceUrl] = _traceUrl
                it[traceMethod] = _traceMethod
                it[start] = _start
                it[end] = _end
                it[duration] = _duration
            }
            commit()
            result = id.value
        }
        return result
    }

    fun insertSpan(
        _spanId: String,
        _traceId: Int,
        _version: String,
        _start: Long,
        _end: Long,
        _duration: Long,
        _process: String,
        _httpMethod: String,
        _httpStatusCode: Int,
        _httpUrl: String,
        _requestSize: Int,
        _responseSize: Int
    ): Int {
        log.info("inserting Span")
        var result = 0
        transaction {
            val id = Spans.insertAndGetId {
                it[spanId] = _spanId
                it[traceId] = _traceId
                it[version] = _version
                it[start] = _start
                it[end] = _end
                it[duration] = _duration
                it[process] = _process
                it[httpMethod] = _httpMethod
                it[httpUrl] = _httpUrl
                it[httpStatusCode] = _httpStatusCode
                it[requestSize] = _requestSize
                it[responseSize] = _responseSize
            }
            commit()
            result = id.value
        }
        return result
    }

    fun insertPrometheusData(
        _type: String,
        _pod: String,
        _time: Long,
        _value: BigDecimal
    ): Int {
        log.info("Inserting prometheus data")
        var result = 0
        transaction {
            val id = PrometheusDatas.insertAndGetId {
                it[type] = _type
                it[pod] = _pod
                it[time] = _time
                it[value] = _value
            }
            commit()
            result = id.value
        }
        return result
    }

    fun aggregateTraces(): ArrayList<TraceAggObject> {
        val results = ArrayList<TraceAggObject>()
        transaction {
            val query = Traces.slice(Traces.traceMethod, Traces.version, Traces.duration.avg()).selectAll()
                .groupBy(Traces.traceMethod, Traces.version)

            query.forEach {
                results.add(TraceAggObject(it[Traces.traceMethod], it[Traces.version], it[Traces.duration.avg()]!!))
            }
        }
        return results
    }

    fun aggregatePatterns(): ArrayList<PatternAggObject> {
        val results = ArrayList<PatternAggObject>()
        transaction {
            val query =
                Patterns.slice(Patterns.resource, Patterns.version, Patterns.patternName, Patterns.duration.avg())
                    .selectAll().groupBy(Patterns.resource, Patterns.version, Patterns.patternName)
            query.forEach {
                results.add(
                    PatternAggObject(
                        it[Patterns.resource],
                        it[Patterns.version],
                        it[Patterns.patternName],
                        it[Patterns.duration.avg()]!!
                    )
                )
            }
        }
        return results
    }
}