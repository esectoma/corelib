package com.nanako.util

import android.text.TextUtils
import java.lang.StringBuilder
import java.util.*

object Util {
    /**
     * 1234567890 -> 123****8901
     */
    @JvmStatic
    fun replaceChar(
        text: String, headerLen: Int, placeholderLen: Int, placeholder: Char
    ): String {
        if (TextUtils.isEmpty(text)) {
            return text
        }
        if (text.length >= placeholderLen) {
            val end = headerLen + 4
            val pc = text.toCharArray()
            val sb = StringBuilder()
            for (i in text.indices) {
                if (i in headerLen until end) {
                    sb.append(placeholder)
                } else {
                    sb.append(pc[i])
                }
            }
            return sb.toString()
        }
        return text
    }

    @JvmStatic
    fun replaceChar(
        text: String, headerLen: Int, tailLen: Int, placeholderLen: Int, placeHolder: String
    ): String {
        if (text.isEmpty()) {
            return text
        }
        if (headerLen + tailLen > text.length) {
            return text
        }
        val sb = StringBuilder()
        if (headerLen > 0) sb.append(text.subSequence(0, headerLen))
        for (i in 0 until placeholderLen) sb.append(placeHolder)
        if (tailLen > 0) sb.append(text.subSequence(text.length - tailLen, text.length))
        return sb.toString()
    }

    /**
     * 将秒转化为小时分钟秒
     */
    @JvmStatic
    fun getHourMinuteSecond(seconds: Int): IntArray {
        val hourSeconds = 3600
        val h = seconds / hourSeconds
        val m = (seconds - h * hourSeconds) / 60
        val s = seconds % 60
        return intArrayOf(h, m, s)
    }

    @JvmStatic
    fun getHourMinuteSecondVideoDuration(seconds: Int): String {
        val hms = getHourMinuteSecond(seconds)
        return if (hms[0] > 0) {
            String.format(
                Locale.getDefault(), "%02d:%02d:%02d", hms[0], hms[1], hms[2]
            )
        } else String.format(
            Locale.getDefault(), "%02d:%02d", hms[1], hms[2]
        )
    }

    @JvmStatic
    fun isFirstRow(pos: Int, rowColumnCount: Int): Boolean {
        return pos in 0 until rowColumnCount
    }

    @JvmStatic
    fun isLastRow(pos: Int, rowColumnCount: Int, totalCount: Int): Boolean {
        val row = totalCount / rowColumnCount
        return if (totalCount % rowColumnCount == 0) {
            pos >= (row - 1) * rowColumnCount && pos < totalCount
        } else pos >= row * rowColumnCount && pos < totalCount
    }
}