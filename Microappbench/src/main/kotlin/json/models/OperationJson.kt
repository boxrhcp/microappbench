package json.models

data class OperationJson(
    val path: String,
    val operation: String,
    val concretePath: String,
    val concreteMethod: String,
    val index: Int,
    val start: Long,
    val end: Long,
    val duration: Long
) {
}