package com.nanako.statusbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.nanako.R
import com.nanako.statusbar.StatusBarPlaceHolderView

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
        val sh = getStatusHeight(context)
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(sh + extraHeight, MeasureSpec.EXACTLY)
        )
    }

    companion object {
        fun getStatusHeight(context: Context): Int {
            var statusHeight = dp2px(context, 24f).toInt()
            try {
                val clazz = Class.forName("com.android.internal.R\$dimen")
                val `object` = clazz.newInstance()
                val height = clazz.getField("status_bar_height")[`object`]
                    .toString().toInt()
                statusHeight = context.resources.getDimensionPixelSize(height)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            }
            return statusHeight
        }

        fun dp2px(context: Context, dp: Float): Float {
            val scale = context.resources.displayMetrics.density
            return dp * scale + 0.5f
        }
    }
}