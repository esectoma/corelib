package com.nanako.titlebar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.nanako.R
import com.nanako.titlebar.TitleBar
import java.lang.Exception
import java.util.HashMap

class TitleBar : FrameLayout, View.OnClickListener {
    private val TAG = javaClass.name
    private val mViewClicked: MutableMap<Int, Long> = HashMap()
    private var mListener: Listener? = null
    var viewStatus: View? = null
        private set
    var vgTitle: ViewGroup? = null
        private set
    var txtTitle: TextView? = null
        private set
    var tvSubTitle: TextView? = null
        private set
    var vgTitleLayout: ViewGroup? = null
        private set
    var vgSubTitleLayout: ViewGroup? = null
        private set
    var vgTitleBox: ViewGroup? = null
        private set
    var vgLeft: ViewGroup? = null
        private set
    var txtLeft: TextView? = null
        private set
    var imgLeft: ImageView? = null
        private set
    var vgRight: ViewGroup? = null
        private set
    var txtRight: TextView? = null
        private set
    var imgRight: ImageView? = null
        private set
    var vBottomLine: View? = null
        private set
    private var mFastClickDuration = 500

    constructor(context: Context?) : super(context!!) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.tb_view_titlebar, this, true)
        viewStatus = view.findViewById(R.id.view_status)
        vgTitle = view.findViewById<View>(R.id.layout_title) as ViewGroup
        txtTitle = view.findViewById<View>(R.id.txt_title) as TextView
        vgLeft = view.findViewById<View>(R.id.layout_left) as ViewGroup
        txtLeft = view.findViewById<View>(R.id.txt_left) as TextView
        imgLeft = view.findViewById<View>(R.id.img_left) as ImageView
        vgRight = view.findViewById<View>(R.id.layout_right) as ViewGroup
        txtRight = view.findViewById<View>(R.id.txt_right) as TextView
        imgRight = view.findViewById<View>(R.id.img_right) as ImageView
        tvSubTitle = view.findViewById<View>(R.id.tv_sub_title) as TextView
        vgTitleLayout = view.findViewById<View>(R.id.layout_main_title) as ViewGroup
        vgSubTitleLayout = view.findViewById<View>(R.id.layout_sub_title) as ViewGroup
        vgTitleBox = view.findViewById<View>(R.id.vg_title_box) as ViewGroup
        vBottomLine = view.findViewById(R.id.v_bottom_line)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleBar)
        val sbg = typedArray.getDrawable(R.styleable.TitleBar_tb_status_background)
        if (sbg != null) {
            viewStatus?.setBackgroundDrawable(sbg)
        }
        viewStatus?.visibility = if (typedArray.getBoolean(
                R.styleable.TitleBar_tb_status_visible,
                false
            )
        ) VISIBLE else GONE
        var sheight = typedArray.getDimensionPixelSize(
            R.styleable.TitleBar_tb_status_height,
            -1
        )
        if (sheight == -1) {
            if (!adjustStatusHeight()) {
                sheight = context.resources.getDimension(R.dimen.tb_status_height).toInt()
            }
        }
        if (sheight != -1) {
            val paramsSh = viewStatus?.layoutParams
            paramsSh?.height = sheight
            viewStatus?.layoutParams = paramsSh
        }
        val bg = typedArray.getDrawable(R.styleable.TitleBar_tb_backgroupd)
        if (bg != null) {
            view.setBackgroundDrawable(bg)
        }
        val defaultColor = resources.getColor(android.R.color.white)
        txtTitle!!.text = typedArray.getString(R.styleable.TitleBar_tb_title)
        txtTitle!!.setTextColor(
            typedArray.getColor(
                R.styleable.TitleBar_tb_title_color,
                defaultColor
            )
        )
        txtTitle!!.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            typedArray.getDimension(R.styleable.TitleBar_tb_title_textsize, 55f)
        )
        txtTitle!!.visibility = if (typedArray.getBoolean(
                R.styleable.TitleBar_tb_title_visible,
                true
            )
        ) VISIBLE else INVISIBLE
        val isBold = typedArray.getBoolean(R.styleable.TitleBar_tb_title_bold, true)
        if (isBold) {
            txtTitle!!.setTypeface(null, Typeface.BOLD)
        }
        vgSubTitleLayout!!.visibility = if (typedArray.getBoolean(
                R.styleable.TitleBar_tb_sub_title_visible,
                false
            )
        ) VISIBLE else GONE
        tvSubTitle!!.text = typedArray.getString(R.styleable.TitleBar_tb_sub_title)
        val defaultSubTitleColor = Color.parseColor("#999999")
        tvSubTitle!!.setTextColor(
            typedArray.getColor(
                R.styleable.TitleBar_tb_sub_title_color,
                defaultSubTitleColor
            )
        )
        tvSubTitle!!.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            typedArray.getDimension(R.styleable.TitleBar_tb_sub_title_size, 22f)
        )
        vgLeft!!.visibility = if (typedArray.getBoolean(
                R.styleable.TitleBar_tb_left_visible,
                false
            )
        ) VISIBLE else GONE
        txtLeft!!.text = typedArray.getString(R.styleable.TitleBar_tb_left_text)
        txtLeft!!.setTextColor(
            typedArray.getColor(
                R.styleable.TitleBar_tb_left_text_color,
                defaultColor
            )
        )
        txtLeft!!.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            typedArray.getDimension(R.styleable.TitleBar_tb_left_text_size, 22f)
        )
        if (typedArray.getBoolean(R.styleable.TitleBar_tb_left_text_bold, false)) {
            txtLeft!!.setTypeface(null, Typeface.BOLD)
        }
        val tvLeftParams = txtLeft!!.layoutParams as LinearLayout.LayoutParams
        tvLeftParams.leftMargin =
            typedArray.getDimension(R.styleable.TitleBar_tb_left_text_margin_left, 0f).toInt()
        txtLeft!!.layoutParams = tvLeftParams
        val drawableLeft = typedArray.getDrawable(R.styleable.TitleBar_tb_left_image)
        imgLeft!!.setImageDrawable(drawableLeft)
        imgLeft!!.visibility = if (typedArray.getBoolean(
                R.styleable.TitleBar_tb_left_image_visible,
                false
            )
        ) VISIBLE else GONE
        val leftImageMarginLeft =
            typedArray.getDimension(R.styleable.TitleBar_tb_left_image_margin_left, 0f).toInt()
        if (leftImageMarginLeft != 0) {
            val params = imgLeft!!.layoutParams as LinearLayout.LayoutParams
            params.leftMargin = leftImageMarginLeft
            imgLeft!!.layoutParams = params
        }
        val leftMargin = typedArray.getDimensionPixelSize(R.styleable.TitleBar_tb_left_margin, 0)
        if (leftMargin != 0) {
            val params = vgLeft!!.layoutParams as LayoutParams
            params.leftMargin = leftMargin
            vgLeft!!.layoutParams = params
        }
        vgRight!!.visibility = if (typedArray.getBoolean(
                R.styleable.TitleBar_tb_right_visible,
                false
            )
        ) VISIBLE else GONE
        txtRight!!.text = typedArray.getString(R.styleable.TitleBar_tb_right_text)
        txtRight!!.setTextColor(
            typedArray.getColor(
                R.styleable.TitleBar_tb_right_text_color,
                defaultColor
            )
        )
        txtRight!!.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            typedArray.getDimension(R.styleable.TitleBar_tb_right_text_size, 22f)
        )
        imgRight!!.visibility = if (typedArray.getBoolean(
                R.styleable.TitleBar_tb_right_image_visible, false
            )
        ) VISIBLE else GONE
        imgRight!!.setImageDrawable(typedArray.getDrawable(R.styleable.TitleBar_tb_right_image))
        val rightMargin = typedArray.getDimensionPixelSize(R.styleable.TitleBar_tb_right_margin, 0)
        if (rightMargin != 0) {
            val params = vgRight!!.layoutParams as LayoutParams
            params.rightMargin = rightMargin
            vgRight!!.layoutParams = params
        }
        var theight = typedArray.getDimensionPixelSize(R.styleable.TitleBar_tb_title_height, -1)
        if (theight == -1) {
            theight = context.resources.getDimension(R.dimen.tb_title_height).toInt()
        }
        val titleMarginHorizontal = typedArray.getDimensionPixelSize(
            R.styleable.TitleBar_tb_title_margin_horizontal,
            0
        )
        val paramsTh = vgTitle!!.layoutParams as LinearLayout.LayoutParams
        paramsTh.height = theight
        vgTitle!!.layoutParams = paramsTh
        val titleBoxParams = vgTitleBox!!.layoutParams as LayoutParams
        titleBoxParams.leftMargin = titleMarginHorizontal
        titleBoxParams.rightMargin = titleMarginHorizontal
        vgTitleBox!!.layoutParams = titleBoxParams
        vgLeft!!.minimumWidth = theight
        vgRight!!.minimumWidth = theight
        val titleMarginTop =
            typedArray.getDimension(R.styleable.TitleBar_tb_title_margin_top, 1f).toInt()
        val subTitleMarginTop =
            typedArray.getDimension(R.styleable.TitleBar_tb_sub_title_margin_top, 1f).toInt()
        var titleParams = vgTitleLayout!!.layoutParams as LinearLayout.LayoutParams
        titleParams.topMargin = titleMarginTop
        titleParams = vgSubTitleLayout!!.layoutParams as LinearLayout.LayoutParams
        titleParams.topMargin = subTitleMarginTop
        val showBottomLine = typedArray.getBoolean(
            R.styleable.TitleBar_tb_bottom_line_visible,
            false
        )
        vBottomLine?.visibility = if (showBottomLine) VISIBLE else GONE
        val bottomLineColor = typedArray.getColor(R.styleable.TitleBar_tb_bottom_line_color, 0)
        vBottomLine?.setBackgroundColor(bottomLineColor)
        val bottomLineHeight =
            typedArray.getDimension(R.styleable.TitleBar_tb_bottom_line_height, 1f).toInt()
        val lineParams = vBottomLine?.layoutParams
        lineParams?.height = bottomLineHeight
        vBottomLine?.layoutParams = lineParams
        val rightTextBg = typedArray.getDrawable(R.styleable.TitleBar_tb_right_text_background)
        if (rightTextBg != null) {
            txtRight!!.setBackgroundDrawable(rightTextBg)
        }
        val rightTextWidth =
            typedArray.getDimension(R.styleable.TitleBar_tb_right_text_width, -1f).toInt()
        val rightTextHeight =
            typedArray.getDimension(R.styleable.TitleBar_tb_right_text_height, -1f).toInt()
        if (rightTextWidth >= 0 && rightTextHeight >= 0) {
            val params = txtRight!!.layoutParams as LinearLayout.LayoutParams
            params.width = rightTextWidth
            params.height = rightTextHeight
            txtRight!!.layoutParams = params
        }
        vgLeft!!.setOnClickListener(this)
        txtLeft!!.setOnClickListener(this)
        imgLeft!!.setOnClickListener(this)
        vgRight!!.setOnClickListener(this)
        txtRight!!.setOnClickListener(this)
        imgRight!!.setOnClickListener(this)
        txtTitle!!.setOnClickListener(this)
        typedArray.recycle()
        updateTitleMargin()
    }

    private fun updateTitleMargin() {
        updateTitleMargin(0)
    }

    fun updateTitleMargin(extraSpace: Int) {
        vgLeft!!.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            var vgLeftWidth = 0
            override fun onGlobalLayout() {
                vgLeft!!.viewTreeObserver.removeGlobalOnLayoutListener(this)
                vgLeftWidth = vgLeft!!.width
                vgRight!!.viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    var vgRightWidth = 0
                    override fun onGlobalLayout() {
                        vgRight!!.viewTreeObserver.removeGlobalOnLayoutListener(this)
                        vgRightWidth = vgRight!!.width
                        val paramsLeft = vgLeft!!.layoutParams as LayoutParams
                        val vgLeftLeftMargin = paramsLeft.leftMargin
                        val vgLeftRightMargin = paramsLeft.rightMargin
                        val paramsRight = vgRight!!.layoutParams as LayoutParams
                        val vgRightLeftMargin = paramsRight.leftMargin
                        val vgRightRightMargin = paramsRight.rightMargin
                        val maxMargin = Math.max(
                            vgLeftWidth + vgLeftLeftMargin + vgLeftRightMargin,
                            vgRightWidth + vgRightLeftMargin + vgRightRightMargin
                        )
                        setTitleMarginHorizontal(maxMargin + extraSpace)
                    }
                })
            }
        })
    }

    override fun onClick(v: View) {
        if (mListener == null) {
            return
        }
        val vid = v.id
        if (mViewClicked.containsKey(vid)) {
            val lastClickTime = mViewClicked[vid]!!
            if (System.currentTimeMillis() - lastClickTime <= mFastClickDuration) {
                Log.w(TAG, "you click so fast!")
                return
            }
        }
        mViewClicked[vid] = System.currentTimeMillis()
        if (vid == R.id.layout_left || vid == R.id.txt_left || vid == R.id.img_left) {
            mListener!!.onLeftAreaClick()
        } else if (vid == R.id.layout_right || vid == R.id.txt_right || vid == R.id.img_right) {
            mListener!!.onRightAreaClick()
        } else if (vid == R.id.txt_title) {
            mListener!!.onCenterAreaClick()
        }
    }

    interface Listener {
        fun onLeftAreaClick()
        fun onRightAreaClick()
        fun onCenterAreaClick()
    }

    fun setListener(listener: Listener?) {
        mListener = listener
    }

    fun setRightVisible(b: Boolean) {
        vgRight!!.visibility = if (b) VISIBLE else GONE
    }

    fun setLeftVisible(b: Boolean) {
        vgLeft!!.visibility = if (b) VISIBLE else GONE
    }

    fun setLeftImageVisible(show: Boolean) {
        imgLeft!!.visibility = if (show) VISIBLE else GONE
    }

    fun setLeftImage(drawable: Int) {
        imgLeft!!.setImageResource(drawable)
    }

    fun setLeftImage(drawable: Drawable?) {
        imgLeft!!.setImageDrawable(drawable)
    }

    fun setRightText(resText: Int) {
        txtRight!!.setText(resText)
    }

    fun setRightText(text: String?) {
        txtRight!!.text = text
    }

    fun setRightTextColor(color: Int) {
        txtRight!!.setTextColor(color)
    }

    val rightText: String
        get() = txtRight!!.text.toString()

    fun setRightImageVisible(show: Boolean) {
        imgRight!!.visibility = if (show) VISIBLE else GONE
    }

    fun setRightImage(drawable: Int) {
        imgRight!!.setImageResource(drawable)
    }

    fun setRightImage(drawable: Drawable?) {
        imgRight!!.setImageDrawable(drawable)
    }

    fun setTitle(resTitle: Int) {
        txtTitle!!.setText(resTitle)
    }

    var title: String?
        get() = txtTitle!!.text.toString()
        set(title) {
            txtTitle!!.text = title
        }

    fun setTitleBackground(drawable: Drawable?) {
        txtTitle!!.setBackgroundDrawable(drawable)
    }

    fun setTitleVisible(b: Boolean) {
        vgTitle!!.visibility = if (b) VISIBLE else GONE
    }

    fun setSubTitle(subTitle: Int) {
        tvSubTitle!!.setText(subTitle)
    }

    fun setSubTitleVisible(b: Boolean) {
        vgSubTitleLayout!!.visibility = if (b) VISIBLE else GONE
    }

    var subTitle: String?
        get() = tvSubTitle!!.text.toString()
        set(subTitle) {
            tvSubTitle!!.text = subTitle
        }

    fun setLeftText(resText: Int) {
        txtLeft!!.setText(resText)
    }

    fun setLeftText(text: String?) {
        txtLeft!!.text = text
    }

    val leftText: String
        get() = txtLeft!!.text.toString()

    fun setLeftTextColor(color: Int) {
        txtLeft!!.setTextColor(color)
    }

    fun setStatusVisible(show: Boolean) {
        viewStatus!!.visibility = if (show) VISIBLE else GONE
    }

    fun setStatusBackground(drawable: Drawable?) {
        viewStatus!!.setBackgroundDrawable(drawable)
    }

    fun setTitleSize(textSize: Float) {
        txtTitle!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    fun setTitleColor(color: Int) {
        txtTitle!!.setTextColor(color)
    }

    fun setTitleMarginHorizontal(marginHorizontal: Int) {
        val titleBoxParams = vgTitleBox!!.layoutParams as LayoutParams
        titleBoxParams.leftMargin = marginHorizontal
        titleBoxParams.rightMargin = marginHorizontal
        vgTitleBox!!.layoutParams = titleBoxParams
    }

    fun adjustStatusHeight(): Boolean {
        val height = getStatusHeight(context)
        if (height != -1) {
            val paramsSh = viewStatus!!.layoutParams
            paramsSh.height = height
            viewStatus!!.layoutParams = paramsSh
            return true
        }
        return false
    }

    fun setFastClickDuration(fastClickDuration: Int) {
        mFastClickDuration = fastClickDuration
    }

    companion object {
        fun getStatusHeight(context: Context): Int {
            var statusHeight = -1
            try {
                val clazz = Class.forName("com.android.internal.R\$dimen")
                val `object` = clazz.newInstance()
                val height = clazz.getField("status_bar_height")[`object`]
                    .toString().toInt()
                statusHeight = context.resources.getDimensionPixelSize(height)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return statusHeight
        }
    }
}