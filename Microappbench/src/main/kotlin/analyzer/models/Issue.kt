package analyzer.models

data class Issue(
    val spanPair: Pair<SpanNode, SpanNode>,
    var tag: String = "",
    val flags: HashMap<String, Boolean> = HashMap(),
    var message: String = ""
) {
}