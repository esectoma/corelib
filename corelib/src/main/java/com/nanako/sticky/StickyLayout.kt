package com.nanako.sticky

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nanako.expandableadapter.ExpandableAdapter
import java.lang.ref.WeakReference

class StickyLayout : FrameLayout {
    private val stickyGroupHelper = StickyGroupHelper()
    private var stickyGroupY = -1
    var updateDelay = 80 //5帧
    private var recyclerView: RecyclerView? = null
    private var expandableAdapter: ExpandableAdapter? = null
    private var stickyListener: StickyListener? = null
    private var stickyGroup = true
    private val stickyScrollListener = StickyScrollListener()

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (childCount > 1) {
            val childView = getChildAt(1)
            childView.layout(
                childView.left,
                stickyGroupY,
                childView.right,
                childView.measuredHeight + stickyGroupY
            )
            removeCallbacks(updateDelayRunn)
            postDelayed(updateDelayRunn, updateDelay.toLong())
        }
    }

    private class UpdateDelayRunn(stickyLayout: StickyLayout) : Runnable {
        private val wrStickyLayout: WeakReference<StickyLayout>

        init {
            wrStickyLayout = WeakReference(stickyLayout)
        }

        override fun run() {
            val sl = wrStickyLayout.get() ?: return
            sl.update()
        }
    }

    private val updateDelayRunn = UpdateDelayRunn(this)
    fun init() {
        recyclerView = getChildAt(0) as RecyclerView
        expandableAdapter = recyclerView!!.adapter as ExpandableAdapter?
        if (expandableAdapter == null) {
            throw RuntimeException("please set RecyclerView's Adapter first!!!")
        } else if (expandableAdapter !is StickyListener) {
            throw RuntimeException("Adapter must implement StickyListener!!!")
        }
        stickyListener = expandableAdapter as StickyListener?
        recyclerView!!.addOnScrollListener(stickyScrollListener)
    }

    private fun update(): Boolean {
        if (!stickyGroup) {
            stickyGroupY = 0
            return false
        }
        val firstVisibleItemPosition = findFirstVisibleItemPosition(recyclerView)
        if (RecyclerView.NO_POSITION == firstVisibleItemPosition) {
            return false
        }
        var groupPosition = -1
        var groupViewType = -1
        var groupAdapterPosition = -1
        if (stickyListener!!.isPostionGroup(firstVisibleItemPosition)) {
            groupAdapterPosition = firstVisibleItemPosition
            groupPosition = expandableAdapter!!.getGroupPosition(firstVisibleItemPosition)
            groupViewType = expandableAdapter!!.getItemViewType(firstVisibleItemPosition)
        } else if (stickyListener!!.isPostionGroupChild(firstVisibleItemPosition)) {
            val poss = expandableAdapter!!.getGroupChildPosition(firstVisibleItemPosition)
            groupAdapterPosition = firstVisibleItemPosition - (poss[1] + 1)
            groupPosition = poss[0]
            groupViewType = expandableAdapter!!.getItemViewType(groupAdapterPosition)
        }
        if (groupPosition == -1 || groupViewType == -1 || groupAdapterPosition == -1) {
            stickyGroupHelper.removeGroupViewHolder(this)
            stickyGroupY = 0
            return false
        }
        val groupCount = expandableAdapter!!.groupCount
        val nextGroupPosition = groupPosition + 1
        var nextAdapterPosition = -1
        var nextVh: RecyclerView.ViewHolder? = null //下一个需要判断是否相交的holder
        if (nextGroupPosition < groupCount) { //group下面还有group
            nextAdapterPosition = expandableAdapter!!.convertGroupPosition(nextGroupPosition)
        } else {
            val fcount = expandableAdapter!!.footerCount
            if (fcount > 0) { //group下面还有footer
                nextAdapterPosition = expandableAdapter!!.convertFooterPosition(0)
            }
        }
        if (nextAdapterPosition != -1) {
            nextVh = recyclerView!!.findViewHolderForAdapterPosition(nextAdapterPosition)
        }
        stickyGroupY = if (nextVh == null) {
            0
        } else {
            val nextHolderTop = nextVh.itemView.top
            val groupHolderHeight = stickyListener!!.getStickyGroupViewHolderHeight(groupViewType)
            if (nextHolderTop >= groupHolderHeight) {
                0
            } else {
                nextHolderTop - groupHolderHeight
            }
        }
        if (stickyGroupHelper.groupType != groupViewType) {
            stickyGroupHelper.addGroupViewHolder(
                this,
                groupAdapterPosition,
                groupPosition,
                groupViewType,
                groupCount,
                stickyListener!!.onCreateStickyGroupViewHolder(groupViewType, this),
                stickyListener!!
            )
        } else {
            stickyGroupHelper.bindGroupViewHolder(
                this, groupAdapterPosition,
                groupPosition,
                groupViewType,
                groupCount,
                stickyListener!!
            )
        }
        if (childCount > 1) {
            if (getChildAt(1).top == stickyGroupY) {
                return false
            }
            requestLayout()
            return true
        }
        return false
    }

    private inner class StickyScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (update()) {
                removeCallbacks(updateDelayRunn)
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (update()) {
                removeCallbacks(updateDelayRunn)
            }
        }
    }

    interface StickyListener {
        fun isPostionGroup(adapterPosition: Int): Boolean
        fun isPostionGroupChild(adapterPosition: Int): Boolean
        fun onCreateStickyGroupViewHolder(
            groupType: Int,
            parent: ViewGroup
        ): RecyclerView.ViewHolder

        fun onBindStickyGroupViewHolder(
            adapterPosition: Int,
            groupPosition: Int,
            stickyGroupViewHolder: RecyclerView.ViewHolder
        )

        /**
         * 返回指定group的高度
         */
        fun getStickyGroupViewHolderHeight(groupType: Int): Int

        /**
         * 返回指定group的marginLeft和marginRight
         */
        fun getStickyGroupViewHolderHorizontalMargin(groupType: Int): IntArray?
    }

    companion object {
        fun findFirstCompletelyVisibleItemPosition(recyclerView: RecyclerView): Int {
            var pos = RecyclerView.NO_POSITION
            val layoutManager = recyclerView.layoutManager
            if (layoutManager is LinearLayoutManager) {
                pos = layoutManager.findFirstCompletelyVisibleItemPosition()
            } else {
            }
            return pos
        }

        /**
         * 因为考虑到Group必须是一整行，所以不会对GridLayoutManager返回多个position做处理
         */
        fun findFirstVisibleItemPosition(recyclerView: RecyclerView?): Int {
            var pos = RecyclerView.NO_POSITION
            val layoutManager = recyclerView!!.layoutManager
            if (layoutManager is LinearLayoutManager) {
                pos = layoutManager.findFirstVisibleItemPosition()
            } else {
            }
            return pos
        }
    }
}