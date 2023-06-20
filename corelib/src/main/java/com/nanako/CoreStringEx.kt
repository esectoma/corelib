package com.app.core

import android.graphics.Color

/**
 * Created by barry on 2023/3/2
 */

fun String?.int(): Int {
    if (isNullOrEmpty()) {
        return 0
    }
    try {
        return java.lang.Integer.parseInt(this)
    } catch (_: Exception) {

    }
    return 0
}

fun String?.long(): Long {
    if (isNullOrEmpty()) {
        return 0L
    }
    try {
        return java.lang.Long.parseLong(this)
    } catch (_: Exception) {

    }
    return 0L
}

fun String?.float(): Float {
    if (isNullOrEmpty()) {
        return 0F
    }
    try {
        return java.lang.Float.parseFloat(this)
    } catch (_: Exception) {

    }
    return 0F
}

fun String?.double(): Double {
    if (isNullOrEmpty()) {
        return 0.toDouble()
    }
    try {
        return java.lang.Double.parseDouble(this)
    } catch (_: Exception) {

    }
    return 0.toDouble()
}