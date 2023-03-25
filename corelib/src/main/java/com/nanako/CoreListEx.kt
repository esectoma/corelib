package com.app.core

/**
 * barry 2022/10/7
 */
fun <T> MutableList<T>.split(rowCount: Int): MutableList<MutableList<T>>? {
    if (size <= 0) {
        return null
    }
    val ll = mutableListOf<MutableList<T>>()
    if (size <= rowCount) {
        ll.add(this)
    } else {
        val row = size / rowCount
        val col = size % rowCount
        for (i in 0 until row) {
            ll.add(subList(i * rowCount, (i + 1) * rowCount))
        }
        if (col > 0) {
            ll.add(subList(row * rowCount, size))
        }
    }
    return ll
}

fun <T> MutableList<T>.splitCount(rowCount: Int): Int? {
    if (size <= 0) {
        return null
    }
    val row = size / rowCount
    val col = size % rowCount
    return row + if (col > 0) 1 else 0
}

fun MutableList<String>.toString(join: String = ","): String {
    if (size <= 0) return ""
    var strs = ""
    for (i in 0 until size - 1) {
        strs += get(i) + join
    }
    strs += get(size - 1)
    return strs
}

fun List<Any>?.sizeEx(): Int {
    if (this == null) {
        return 0
    }
    return size
}