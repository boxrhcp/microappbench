package utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

object ResourceManager {
    private val config = JsonParser().parse(
        File("config.json").readText(Charsets.UTF_8)
    ).asJsonObject

    fun getConfigFile(): JsonObject {
        return config
    }
}