package com.app.core

import java.util.*
import kotlin.math.abs

/**
 * Created by barry on 2023/1/10
 */
fun Long.timestampToCurrWeek(): Int {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar.get(Calendar.DAY_OF_WEEK)
}

fun Long.timestampToCurrWeekMonday(): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    val oneDay = 24 * 60 * 60 * 1000L
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.TUESDAY -> return this - oneDay
        Calendar.WEDNESDAY -> this - oneDay * 2
        Calendar.THURSDAY -> this - oneDay * 3
        Calendar.FRIDAY -> this - oneDay * 4
        Calendar.SATURDAY -> this - oneDay * 5
        Calendar.SUNDAY -> return this - oneDay * 6
        else -> this
    }
}

fun Long.timestampToCurrWeekFriday(): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    val oneDay = 24 * 60 * 60 * 1000L
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> return this + oneDay * 4
        Calendar.TUESDAY -> return this + oneDay * 3
        Calendar.WEDNESDAY -> this + oneDay * 2
        Calendar.THURSDAY -> this + oneDay
        Calendar.SATURDAY -> this - oneDay
        Calendar.SUNDAY -> return this - oneDay * 2
        else -> this
    }
}

fun Long.differenceBetween(otherTimestamp: Long): Array<Long> {
    val diff = abs(otherTimestamp - this)
    val oneMinute = 60 * 1000
    val oneHour = 60 * oneMinute
    val oneDay = 24 * oneHour
    val d = diff / oneDay
    val h = (diff % oneDay) / oneHour
    val m = (diff % oneHour) / oneMinute
    val s = (diff % oneMinute) / 1000
    return arrayOf(d, h, m, s)
}