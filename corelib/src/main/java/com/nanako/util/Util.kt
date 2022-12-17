package com.nanako.util

import android.text.TextUtils
import java.lang.StringBuilder
import java.util.*

object Util {
    /**
     * 1234567890 -> 123****8901
     */
    fun replaceChar(
        text: String, headerLength: Int, placeholderLength: Int,
        placeholder: Char
    ): String {
        if (TextUtils.isEmpty(text)) {
            return text
        }
        if (text.length >= placeholderLength) {
            val end = headerLength + 4
            val pc = text.toCharArray()
            val sb = StringBuilder()
            for (i in 0 until text.length) {
                if (i >= headerLength && i < end) {
                    sb.append(placeholder)
                } else {
                    sb.append(pc[i])
                }
            }
            return sb.toString()
        }
        return text
    }

    /**
     * 将秒转化为小时分钟秒
     */
    fun getHourMinuteSecond(seconds: Int): IntArray {
        val hourSeconds = 3600
        val h = seconds / hourSeconds
        val m = (seconds - h * hourSeconds) / 60
        val s = seconds % 60
        return intArrayOf(h, m, s)
    }

    fun getHourMinuteSecondVideoDuration(seconds: Int): String {
        val hms = getHourMinuteSecond(seconds)
        return if (hms[0] > 0) {
            String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                hms[0],
                hms[1],
                hms[2]
            )
        } else String.format(
            Locale.getDefault(),
            "%02d:%02d",
            hms[1],
            hms[2]
        )
    }

    fun isFirstRow(pos: Int, rowColumnCount: Int): Boolean {
        return pos in 0 until rowColumnCount
    }

    fun isLastRow(pos: Int, rowColumnCount: Int, totalCount: Int): Boolean {
        val row = totalCount / rowColumnCount
        return if (totalCount % rowColumnCount == 0) {
            pos >= (row - 1) * rowColumnCount && pos < totalCount
        } else pos >= row * rowColumnCount && pos < totalCount
    }
}