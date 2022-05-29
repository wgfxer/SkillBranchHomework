package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int> {
    if (this == null || substr.isEmpty() || !substr.contains(substr, ignoreCase)) return emptyList()
    var startIndexForSearch = 0
    val indexes: MutableList<Int> = mutableListOf()
    while(startIndexForSearch < this.length) {
        val index = indexOf(substr, startIndexForSearch, ignoreCase = ignoreCase)
        if (index != -1) {
            indexes.add(index)
            startIndexForSearch = index + substr.length
        } else {
            break
        }
    }
    return indexes
}