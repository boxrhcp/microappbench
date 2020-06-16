package database

import org.jetbrains.exposed.sql.Database

object DatabaseObject {
    val db by lazy{
        Database.connect("jdbc:mysql://microappbench-db.cx3ybesgvhcj.eu-central-1.rds.amazonaws.com:3306:microappbench-db"
            , driver = "com.mysql.jdbc.Driver", user = "admin", password = "microappbench")
    }
}