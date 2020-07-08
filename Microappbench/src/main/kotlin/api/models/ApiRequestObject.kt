package api.models

import com.google.gson.JsonElement

data class ApiRequestObject (
    val service: String,
    val path: String,
    var parameter: Array<Pair<String, String>>,
    var headers: Array<Pair<String, String>>,
    val method: String,
    var body: JsonElement,
    var response: String,
    var status: Int){

    }