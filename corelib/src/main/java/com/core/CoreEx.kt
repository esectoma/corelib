package com.core

import android.content.Context
import android.content.res.Resources.Theme
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

/**
 * Created by barry on 2023/6/20
 */

fun Int.dp(context: Context = Core.getContext()): Float =
    this * context.resources.displayMetrics.density + 0.5f

fun Float.dp(context: Context = Core.getContext()): Float =
    this * context.resources.displayMetrics.density + 0.5f

fun Double.dp(context: Context = Core.getContext()): Double =
    this * context.resources.displayMetrics.density + 0.5f

fun Int.sp(context: Context = Core.getContext()): Float =
    this * context.resources.displayMetrics.scaledDensity + 0.5f

fun Float.sp(context: Context = Core.getContext()): Float =
    this / context.resources.displayMetrics.scaledDensity + 0.5f

fun String.color(): Int = Color.parseColor(this)

fun Int.res2color(context: Context = Core.getContext()): Int = ContextCompat.getColor(context, this)

fun Int.res2str(context: Context = Core.getContext()): String = context.resources.getString(this)

fun Int.res2int(context: Context = Core.getContext()): Int = context.resources.getInteger(this)

fun Int.res2intArr(context: Context = Core.getContext()): IntArray =
    context.resources.getIntArray(this)

fun Int.res2bool(context: Context = Core.getContext()): Boolean = context.resources.getBoolean(this)

fun Int.res2dimens(context: Context = Core.getContext()): Float =
    context.resources.getDimension(this)

fun Int.res2drawable(context: Context = Core.getContext(), theme: Theme? = null): Drawable =
    context.resources.getDrawable(this, theme)