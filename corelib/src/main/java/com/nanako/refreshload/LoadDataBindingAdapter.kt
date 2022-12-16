package com.nanako.refreshload

import android.widget.TextView
import androidx.databinding.BindingAdapter

/**
 * barry 2022/10/29
 */
object LoadDataBindingAdapter {
    @JvmStatic
    @BindingAdapter("loadStatusText")
    fun setLoadStatusText(textView: TextView, status: Int) {
        textView.text = RefreshLoadLayout.Load.getLoadText(textView.context, status)
    }
}