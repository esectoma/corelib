package com.nanako.util

import android.text.TextUtils
import com.nanako.log.Log.Companion.log
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Random

object MathUtil {
    fun add(v1: String?, v2: String?): Double {
        try {
            val bd1 = BigDecimal(v1)
            val bd2 = BigDecimal(v2)
            return bd1.add(bd2).toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            log.e(e)
        }
        return 0.toDouble()
    }

    fun sub(v1: String?, v2: String?): Double {
        try {
            val bd1 = BigDecimal(v1)
            val bd2 = BigDecimal(v2)
            return bd1.subtract(bd2).toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            log.e(e)
        }
        return 0.toDouble()
    }

    fun divideFloor(v1: String?, v2: String?, scale: Int): Double {
        try {
            val bd1 = BigDecimal(v1)
            val bd2 = BigDecimal(v2)
            return bd1.divide(bd2, scale, BigDecimal.ROUND_FLOOR).toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            log.e(e)
        }
        return 0.toDouble()
    }

    fun divide(v1: String?, v2: String?, scale: Int): Double {
        try {
            val bd1 = BigDecimal(v1)
            val bd2 = BigDecimal(v2)
            return bd1.divide(bd2, scale, BigDecimal.ROUND_HALF_UP).toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            log.e(e)
        }
        return 0.toDouble()
    }

    fun multiply(v1: String?, v2: String?): Double {
        try {
            val bd1 = BigDecimal(v1)
            val bd2 = BigDecimal(v2)
            return bd1.multiply(bd2).toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            log.e(e)
        }
        return 0.toDouble()
    }

    fun multiply(v1: String, v2: String, v3: String, v4: String): Double {
        return multiply(arrayOf(v1, v2, v3, v4))
    }

    fun multiply(v1: String, v2: String, v3: String): Double {
        return multiply(arrayOf(v1, v2, v3))
    }

    fun multiply(vs: Array<String>?): Double {
        if (vs.isNullOrEmpty()) {
            return 0.toDouble()
        }
        try {
            var bd: BigDecimal? = null
            for (v in vs) {
                bd = if (bd == null) {
                    BigDecimal(v)
                } else {
                    bd.multiply(BigDecimal(v))
                }
            }
            return bd!!.toDouble()
        } catch (e: Exception) {
            log.e(e)
        }
        return 0.toDouble()
    }

    /**
     * 保留小数点后x位，不四舍五入，截取模式
     */
    fun formatNumberFloor(v: Double, pointCount: Int): String {
        return formatNumber(v, RoundingMode.FLOOR, pointCount)
    }

    /**
     * 保留小数点后x位，四舍五入模式
     */
    fun formatNumberHalfUp(v: Double, pointCount: Int): String {
        return formatNumber(v, RoundingMode.HALF_UP, pointCount)
    }

    /**
     * 保留小数点后x位，天花板模式
     */
    fun formatNumberCeiling(v: Double, pointCount: Int): String {
        return formatNumber(v, RoundingMode.CEILING, pointCount)
    }

    fun formatNumber(v: Double, roundingMode: RoundingMode?, pointCount: Int): String {
        return formatNumber(v, roundingMode, 0, pointCount)
    }

    /**
     * @param v             需要格式化的数字
     * @param roundingMode  格式化模式（比如截取模式，四舍五入模式，以及其他的模式）
     * @param minPointCount 需要保留小数点后面最少几位（不足的会以0补齐）
     * @param maxPointCount 需要保留小数点后面最多几位
     * @return
     */
    fun formatNumber(
        v: Double,
        roundingMode: RoundingMode? = RoundingMode.HALF_UP,
        minPointCount: Int,
        maxPointCount: Int
    ): String {
        try {
            val otherSymbols = DecimalFormatSymbols.getInstance()
            otherSymbols.decimalSeparator = '.'
            otherSymbols.groupingSeparator = '.'
            val formater = DecimalFormat()
            formater.decimalFormatSymbols = otherSymbols
            formater.minimumFractionDigits = minPointCount
            formater.maximumFractionDigits = maxPointCount
            formater.groupingSize = 0
            formater.roundingMode = roundingMode
            return formater.format(v)
        } catch (e: Exception) {
            log.e(e)
        }
        return ""
    }

    /**
     * 提供精确的加法运算。
     *
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    fun add(v1: Double, v2: Double): Double {
        return add(v1.toString(), v2.toString())
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param v     需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    fun round(v: Double, scale: Int): Double {
        require(scale >= 0) { "The scale must be a positive integer or zero" }
        val b = BigDecimal(v.toString())
        val one = BigDecimal("1")
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).toDouble()
    }

    /**
     * 生成一个 startNum到 endNum之间的（int）随机数
     *
     * @param startNum
     * @param endNum
     * @return
     */
    fun getRandom(startNum: Int, endNum: Int): Int {
        if (endNum > startNum) {
            val random = Random()
            return random.nextInt(endNum - startNum) + startNum
        }
        return 0
    }

    fun stripTrailingZeros(doubleValue: Double): String {
        return stripTrailingZeros(doubleValue.toString())
    }

    fun stripTrailingZeros(doubleValue: String): String {
        try {
            return BigDecimal(doubleValue).stripTrailingZeros().toPlainString()
        } catch (e: Exception) {
            log.e(e)
        }
        return doubleValue
    }

    /**
     * 小数点后补0
     */
    fun appendZeroAfterPoint(_text: String, pointCount: Int): String {
        var text = _text
        val pi = text.indexOf(".")
        if (pi == -1) {
            text += "."
            for (i in 0 until pointCount) {
                text += "0"
            }
        } else {
            val pos = text.substring(pi + 1)
            if (pos.length < pointCount) {
                for (i in 0 until pointCount - pos.length) {
                    text += "0"
                }
            }
        }
        return text
    }

    fun getPointCount(text: String): Int {
        val pi = text.indexOf(".")
        if (pi != -1) {
            val sub = text.substring(pi + 1)
            return if (TextUtils.isEmpty(sub)) 0 else sub.length
        }
        return 0
    }
}