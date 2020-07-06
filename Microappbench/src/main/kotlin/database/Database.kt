package database

import org.jetbrains.exposed.sql.Database

object Database {
    val db by lazy {
        Database.connect(
            "jdbc:mysql://localhost:3306/microappbench",
            driver = "com.mysql.jdbc.Driver",
            user = "root",
            password = "leopardo"
        )
    }
}