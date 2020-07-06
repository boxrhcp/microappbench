package database

import database.models.Pattern
import database.tables.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

class DatabaseOperator {
    private val log = LoggerFactory.getLogger("DatabaseOperator")!!

    private val db = Database.db

    fun createTables() {
        log.info("Creating db and tables")
        transaction {
            SchemaUtils.create(Patterns)
            SchemaUtils.create(Operations)
            SchemaUtils.create(PrometheusDatas)
            SchemaUtils.create(Spans)
            SchemaUtils.create(Traces)
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
        res: String,
        ver: String,
        patternNam: String,
        reqId: Int,
        workId: Int,
        starT: DateTime,
        endT: DateTime
    ) {
        log.info("creating pattern")
        transaction {
            val trace = Pattern.new {
                resource = res
                version = ver
                patternName = patternNam
                requestId = reqId
                workerId = workId
                start = starT
                end = endT
            }
            commit()
        }
    }
}