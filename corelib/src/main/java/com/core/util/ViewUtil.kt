package com.core.util

import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView

object ViewUtil {
    fun setTargetTextsCustom(
        textView: TextView,
        fullText: String,
        targetText1: String,
        targetTextColor1: Int,
        targetTextSize1: Float? = null,
        targetText2: String? = null,
        targetTextColor2: Int? = null,
        targetTextSize2: Float? = null,
        targetText1Click: View.OnClickListener? = null,
        targetText2Click: View.OnClickListener? = null
    ) {
        if (TextUtils.isEmpty(fullText)) {
            return
        }
        textView.highlightColor = 0x00000000
        val ss = SpannableString(fullText)
        if (!TextUtils.isEmpty(targetText1)) {
            val i = fullText.indexOf(targetText1)
            if (i != -1) {
                val colorSpan = ForegroundColorSpan(targetTextColor1)
                val end = i + targetText1.length
                ss.setSpan(colorSpan, i, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                targetTextSize1?.let {
                    val sizeSpan = AbsoluteSizeSpan(targetTextSize1.toInt())
                    ss.setSpan(sizeSpan, i, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                }
                targetText1Click?.let {
                    val clickSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            it.onClick(widget)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            ds.bgColor = 0x00000000
                            ds.isUnderlineText = false
                        }
                    }
                    ss.setSpan(clickSpan, i, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                }
            }
        }
        if (!TextUtils.isEmpty(targetText2)) {
            val i = fullText.indexOf(targetText2!!)
            if (i != -1) {
                val end = i + targetText2.length
                targetTextColor2?.let {
                    val colorSpan = ForegroundColorSpan(targetTextColor2)
                    ss.setSpan(colorSpan, i, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                }
                targetTextSize2?.let {
                    val sizeSpan = AbsoluteSizeSpan(targetTextSize2.toInt())
                    ss.setSpan(sizeSpan, i, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                }
                targetText2Click?.let {
                    val clickSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            it.onClick(widget)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            ds.bgColor = 0x00000000
                            ds.isUnderlineText = false
                        }
                    }
                    ss.setSpan(clickSpan, i, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                }
            }
        }
        if (targetText1Click != null || targetText2Click != null) {
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
        textView.text = ss
    }
}