package com.nanako.statusbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.nanako.R
import com.nanako.statusbar.StatusBarPlaceHolderView
import com.nanako.titlebar.TitleBar

/**
 * Author：qbw on 2019/3/22 17:16
 */
class StatusBarPlaceHolderView : View {
    private var extraHeight = 0

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.StatusBarPlaceHolderView
        )
        extraHeight =
            typedArray.getDimension(R.styleable.StatusBarPlaceHolderView_sb_extra_height, 0f)
                .toInt()
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sh = TitleBar.getStatusHeight(context)
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(sh + extraHeight, MeasureSpec.EXACTLY)
        )
    }

    companion object {

        fun dp2px(context: Context, dp: Float): Float {
            val scale = context.resources.displayMetrics.density
            return dp * scale + 0.5f
        }
    }
}