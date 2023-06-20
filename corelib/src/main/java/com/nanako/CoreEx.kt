package com.nanako

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat

/**
 * Created by barry on 2023/6/20
 */

fun Int.dp(context: Context): Float = this * context.resources.displayMetrics.density + 0.5f

fun Float.dp(context: Context): Float = this * context.resources.displayMetrics.density + 0.5f

fun Double.dp(context: Context): Double = this * context.resources.displayMetrics.density + 0.5f

fun Int.sp(context: Context): Float = this * context.resources.displayMetrics.scaledDensity + 0.5f

fun Float.sp(context: Context): Float = this / context.resources.displayMetrics.scaledDensity + 0.5f

fun String.color(): Int = Color.parseColor(this)

fun Int.color(context: Context): Int = ContextCompat.getColor(context, this)