package utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser

object ResourceManager {
    private val config = JsonParser().parse(
        this::class.java.classLoader.getResource("config.json")
            .readText()
    ).asJsonObject

    fun getConfigFile(): JsonObject {
        return config
    }
}