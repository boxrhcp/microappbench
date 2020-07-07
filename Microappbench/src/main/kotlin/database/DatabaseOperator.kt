package database

import database.tables.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

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
        _start: DateTime,
        _end: DateTime
    ): Int {
        log.info("inserting pattern")
        var result = 0
        transaction {
            val id = Patterns.insertAndGetId{
                it[resource] = _resource
                it[version] = _version
                it[patternName] = _patternName
                it[requestId] = _requestId
                it[workerId] = _workId
                it[start] = _start
                it[end] = _end
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
        _index: Int,
        _start: DateTime,
        _end: DateTime
    ):Int {
        log.info("inserting operation")
        var result = 0
        transaction {
            val id = Operations.insertAndGetId {
                it[patternId] = _patternId
                it[path] = _path
                it[operation] = _operation
                it[index] = _index
                it[start] = _start
                it[end] = _end
            }
            commit()
            result = id.value
        }
        return result
    }

    fun insertTrace(_traceId: String, _version: String, _start: DateTime, _end: DateTime): Int {
        log.info("inserting trace")
        var result = 0
        transaction {
            val id = Traces.insertAndGetId {
                it[traceId] = _traceId
                it[version] = _version
                it[start] = _start
                it[end] = _end
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
        _start: DateTime,
        _end: DateTime,
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
}