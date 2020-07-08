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
        val patternId = db.insertPattern(
            "orders", "v2", "LIST"
            , 1, 0, 0L, 0L
        )
        db.insertOperation(patternId, "asdasd", "asdasd", 1, 0L, 0L)
        val traceId = db.insertTrace("asdafiadsif", "v2", 0L, 0L)
        val spanId = db.insertSpan(
            "asdsada",
            traceId,
            "v2",
            0L,
            0L,
            "neps",
            "GET",
            200,
            "asdasda",
            20,
            20
        )
    }
}