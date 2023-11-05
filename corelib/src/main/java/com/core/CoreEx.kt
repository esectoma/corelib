package com.core

import android.content.Context
import android.content.res.Resources.Theme
import android.graphics.Color
import android.graphics.drawable.Drawable
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

fun Int.res2color(context: Context): Int = ContextCompat.getColor(context, this)

fun Int.res2str(context: Context): String = context.resources.getString(this)

fun Int.res2int(context: Context): Int = context.resources.getInteger(this)

fun Int.res2intArr(context: Context): IntArray = context.resources.getIntArray(this)

fun Int.res2bool(context: Context): Boolean = context.resources.getBoolean(this)

fun Int.res2dimens(context: Context): Float = context.resources.getDimension(this)

fun Int.res2drawable(context: Context, theme: Theme? = null): Drawable =
    context.resources.getDrawable(this, theme)