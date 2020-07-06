package tools.analyzer

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import database.DatabaseOperator
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("MonitorRetriever")!!

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::AnalyzerArguments).run {
        val db = DatabaseOperator()
        if (clean){
            db.dropTables()
            db.createTables()
        }
        db.insertPattern("orders", "v2", "LIST"
            , 1, 0, DateTime.now(), DateTime.now())
    }
}