package com.nanako.refreshload

import android.view.View
import android.widget.ScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.nanako.log.Log.Companion.log

/**
 * @作者 qinbaowei
 * @创建时间 2015年10月27日 14:03
 * @文件说明
 */
object RecyclerViewScrollUtil {

    @JvmStatic
    fun isContentToTop(view: View?): Boolean {
        if (null == view) {
            log.e("null == view")
            return true
        }
        if (view is RecyclerView) {
            val recyclerView = view
            val layoutManager = recyclerView.layoutManager
            val itemCount = layoutManager!!.itemCount
            log.v("itemCount = $itemCount")
            if (0 == itemCount) {
                return true
            }
            val poss = getRecyclerViewFirstCompleteVisiblePos(recyclerView)
            var iTop = false
            for (p in poss!!) {
                log.v("first complete visible position = $p")
                if (0 == p) {
                    iTop = true
                    break
                } else if (RecyclerView.NO_POSITION == p) { //NO_POSITION,比如第一个view高度很高,这种情况就没有完全显示的item
                    val viewHolder = recyclerView.findViewHolderForAdapterPosition(
                        0
                    )
                    if (null == viewHolder) {
                        log.v("null == viewHolder")
                        iTop = false
                    } else {
                        val top = viewHolder.itemView.top
                        log.v("first item view top = $top")
                        iTop = 0 == top
                        if (iTop) {
                            break
                        }
                    }
                }
            }
            return iTop
        } else if (view is ScrollView) {
            if (0 == view.scrollY) {
                return true
            }
        } else if (view is View) {
            return true
        }
        return false
    }

    @JvmStatic
    fun getRecyclerViewFirstCompleteVisiblePos(recyclerView: RecyclerView): IntArray? {
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is LinearLayoutManager || layoutManager is GridLayoutManager) {
            val linearLayoutManager = layoutManager as LinearLayoutManager
            return intArrayOf(linearLayoutManager.findFirstCompletelyVisibleItemPosition())
        } else if (layoutManager is StaggeredGridLayoutManager) {
            return layoutManager.findFirstCompletelyVisibleItemPositions(null)
        }
        return null
    }

    @JvmStatic
    fun isContentToBottom(view: View?): Boolean {
        var targetVisibleItemIndex = -1
        if (view is RecyclerView) {
            targetVisibleItemIndex = view.adapter!!.itemCount - 1
        }
        return isContentToBottom(view, targetVisibleItemIndex)
    }

    @JvmStatic
    fun isContentToBottom(view: View?, targetVisibleItemIndex: Int): Boolean {
        if (view is RecyclerView) {
            val layoutManager = view.layoutManager
            if (0 == layoutManager!!.itemCount) {
                return false
            }
            if (layoutManager is LinearLayoutManager || layoutManager is GridLayoutManager) {
                val linearLayoutManager = layoutManager as LinearLayoutManager
                if (targetVisibleItemIndex == linearLayoutManager.findLastCompletelyVisibleItemPosition()) {
                    return true
                }
            } else if (layoutManager is StaggeredGridLayoutManager) {
                val positions = layoutManager.findLastCompletelyVisibleItemPositions(
                    null
                )
                var b = false
                for (p in positions) {
                    if (targetVisibleItemIndex == p) {
                        b = true
                        break
                    }
                }
                return b
            }
        } else if (view is ScrollView) {
            val scrollView = view
            if (scrollView.scrollY + scrollView.height >= scrollView.measuredHeight) {
                return true
            }
        } else if (view is View) {
            return true
        }
        return false
    }

    @JvmStatic
    fun isContentNearBottom(recyclerView: RecyclerView): Boolean {
        return isContentNearBottom(recyclerView, 0)
    }


    @JvmStatic
    fun isContentNearBottom(recyclerView: RecyclerView, offset: Int): Boolean {
        val layoutManager = recyclerView.layoutManager
        if (0 == layoutManager!!.itemCount) {
            return false
        }
        if (layoutManager is LinearLayoutManager || layoutManager is GridLayoutManager) {
            val linearLayoutManager = layoutManager as LinearLayoutManager
            if (linearLayoutManager.itemCount - 1 - offset == linearLayoutManager.findLastVisibleItemPosition()) {
                return true
            }
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val staggeredGridLayoutManager = layoutManager
            val positions = staggeredGridLayoutManager.findLastVisibleItemPositions(null)
            var b = false
            for (p in positions) {
                if (staggeredGridLayoutManager.itemCount - 1 - offset == p) {
                    b = true
                    break
                }
            }
            return b
        }
        return false
    }

    @JvmStatic
    fun isContentNearTop(recyclerView: RecyclerView): Boolean {
        val layoutManager = recyclerView.layoutManager
        if (0 == layoutManager!!.itemCount) {
            return false
        }
        if (layoutManager is LinearLayoutManager || layoutManager is GridLayoutManager) {
            val linearLayoutManager = layoutManager as LinearLayoutManager
            if (0 == linearLayoutManager.findFirstVisibleItemPosition()) {
                return true
            }
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val positions = layoutManager.findFirstVisibleItemPositions(null)
            var b = false
            for (p in positions) {
                if (0 == p) {
                    b = true
                    break
                }
            }
            return b
        }
        return false
    }

    @JvmStatic
    fun getRecyclerViewFirstCompleteVisiblePos(
        recyclerView: RecyclerView,
        isComplete: Boolean,
        isFirst: Boolean
    ): IntArray? {
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is LinearLayoutManager || layoutManager is GridLayoutManager) {
            val linearLayoutManager = layoutManager as LinearLayoutManager
            return if (isComplete) {
                if (isFirst) {
                    intArrayOf(linearLayoutManager.findFirstCompletelyVisibleItemPosition())
                } else {
                    intArrayOf(linearLayoutManager.findLastCompletelyVisibleItemPosition())
                }
            } else {
                if (isFirst) {
                    intArrayOf(linearLayoutManager.findFirstVisibleItemPosition())
                } else {
                    intArrayOf(linearLayoutManager.findLastVisibleItemPosition())
                }
            }
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val staggeredGridLayoutManager = layoutManager
            return if (isComplete) {
                if (isFirst) {
                    staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(null)
                } else {
                    staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(null)
                }
            } else {
                if (isFirst) {
                    staggeredGridLayoutManager.findFirstVisibleItemPositions(null)
                } else {
                    staggeredGridLayoutManager.findLastVisibleItemPositions(null)
                }
            }
        }
        return null
    }
}