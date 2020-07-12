package utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser

class ResourceManager {
    companion object {
        fun loadConfigFile(): JsonObject {
            return JsonParser().parse(this::class.java.classLoader.getResource("config.json")
                .readText()).asJsonObject
        }
    }
}