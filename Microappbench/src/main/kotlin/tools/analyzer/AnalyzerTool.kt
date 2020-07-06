package tools.analyzer

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import database.DatabaseOperator
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("Analyzer")!!

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::AnalyzerArguments).run {
        val db = DatabaseOperator()
        if (clean) {
            db.dropTables()
            db.createTables()
        }
        val patternId = db.insertPattern(
            "orders", "v2", "LIST"
            , 1, 0, DateTime.now(), DateTime.now()
        )
        db.insertOperation(patternId, "asdasd", "asdasd", 1, DateTime.now(), DateTime.now())
        val traceId = db.insertTrace("asdafiadsif", "v2", DateTime.now(), DateTime.now())
        val spanId = db.insertSpan(
            "asdsada",
            traceId,
            "v2",
            DateTime.now(),
            DateTime.now(),
            "neps",
            "GET",
            200,
            "asdasda",
            20,
            20
        )
    }
}