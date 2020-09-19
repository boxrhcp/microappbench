package database

import database.models.PatternAggObject
import database.models.SpanObject
import database.models.TraceMatchObject
import database.tables.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.math.BigDecimal

class DatabaseOperator {
    private val log = LoggerFactory.getLogger("DatabaseOperator")!!

    private val db = Database.db

    fun createTables() {
        log.debug("Creating db and tables")
        transaction {
            SchemaUtils.create(Patterns, Operations, PrometheusDatas, Traces, Spans)
            commit()
        }
        log.debug("Db creation successful")
    }

    fun dropTables() {
        log.debug("Dropping db and tables")
        transaction {
            SchemaUtils.drop(Patterns, Operations, PrometheusDatas, Spans, Traces)
            commit()
        }
        log.debug("Drop db successful")
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
        log.debug("inserting pattern")
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
        _headerId: String,
        _index: Int,
        _start: Long,
        _end: Long,
        _duration: Long
    ): Int {
        log.debug("inserting operation")
        var result = 0
        transaction {
            val id = Operations.insertAndGetId {
                it[patternId] = _patternId
                it[path] = _path
                it[operation] = _operation
                it[concretePath] = _concretePath
                it[concreteMethod] = _concreteMethod
                it[headerId] = _headerId
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
        _headerId: String,
        _start: Long,
        _end: Long,
        _duration: Long
    ): Int {
        log.debug("inserting trace")
        var result = 0
        transaction {
            val id = Traces.insertAndGetId {
                it[traceId] = _traceId
                it[version] = _version
                it[traceUrl] = _traceUrl
                it[traceMethod] = _traceMethod
                it[headerId] = _headerId
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
        _responseSize: Int,
        _parentId: String
    ): Int {
        log.debug("inserting Span")
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
                it[parentId] = _parentId
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
        log.debug("Inserting prometheus data")
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

    fun aggregatePatternsByVersion(version: String): ArrayList<PatternAggObject> {
        log.debug("Querying aggregated patterns")
        val results = ArrayList<PatternAggObject>()
        transaction {
            val query =
                Patterns.slice(Patterns.resource, Patterns.version, Patterns.patternName, Patterns.duration.avg())
                    .select { Patterns.version eq version }
                    .groupBy(Patterns.resource, Patterns.version, Patterns.patternName)
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

    fun getTracesMatchedWithPattern(pattern: PatternAggObject, version: String): ArrayList<TraceMatchObject> {
        log.debug("Querying traces from given pattern - resource: ${pattern.resource} pattern: ${pattern.patternName} version: $version")
        val results = ArrayList<TraceMatchObject>()
        transaction {
            val traces = Operations.innerJoin(Patterns)
                .join(Traces, JoinType.INNER, additionalConstraint = { Traces.headerId eq Operations.headerId })
                .slice(
                    Traces.id,
                    Traces.traceId,
                    Traces.version,
                    Patterns.requestId,
                    Operations.index,
                    Operations.path,
                    Traces.traceUrl,
                    Operations.operation,
                    Traces.traceMethod,
                    Traces.headerId,
                    Traces.start,
                    Traces.end,
                    Traces.duration
                )
                .select { Patterns.patternName eq pattern.patternName and (Patterns.resource eq pattern.resource and (Patterns.version eq version)) }
            traces.forEach {
                log.debug("trace id: ${it[Traces.id].value}")
                results.add(
                    TraceMatchObject(
                        it[Traces.id].value,
                        it[Traces.traceId],
                        it[Traces.version],
                        it[Patterns.requestId],
                        it[Operations.index],
                        it[Operations.path],
                        it[Traces.traceUrl],
                        it[Operations.operation],
                        it[Traces.traceMethod],
                        it[Traces.headerId],
                        it[Traces.start],
                        it[Traces.end],
                        it[Traces.duration]
                    )
                )
            }
        }
        return results
    }

    fun getSpansByTraceId(trace: TraceMatchObject): ArrayList<SpanObject> {
        val results = ArrayList<SpanObject>()
        transaction {
            val spans = Spans.innerJoin(Traces).slice(
                Spans.spanId,
                Traces.traceId,
                Spans.version,
                Spans.start,
                Spans.end,
                Spans.duration,
                Spans.process,
                Spans.httpMethod,
                Spans.httpUrl,
                Spans.httpStatusCode,
                Spans.requestSize,
                Spans.responseSize,
                Spans.parentId
            ).select { Spans.traceId eq trace.id }

            spans.forEach {
                results.add(
                    SpanObject(
                        it[Spans.spanId],
                        it[Traces.traceId],
                        it[Spans.version],
                        trace.requestId,
                        trace.index,
                        it[Spans.start],
                        it[Spans.end],
                        it[Spans.duration],
                        it[Spans.process],
                        it[Spans.httpMethod],
                        it[Spans.httpUrl],
                        it[Spans.httpStatusCode],
                        it[Spans.requestSize],
                        it[Spans.responseSize],
                        it[Spans.parentId]
                    )
                )
            }
        }
        return results
    }

    fun commit() {
        transaction {
            commit()
        }
    }
}
