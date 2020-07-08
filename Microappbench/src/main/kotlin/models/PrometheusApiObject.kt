package models

data class PrometheusApiObject(
    val type: String,
    val pod: String,
    val values: ArrayList<PrometheusValuesObject>
) {

}