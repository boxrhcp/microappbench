package database

import org.jetbrains.exposed.sql.Database

object DatabaseObject {
    val db by lazy{
        Database.connect("jdbc:mysql://url:3306:microappbench-db"
            , driver = "com.mysql.jdbc.Driver", user = "admin", password = "microappbench")
    }
}