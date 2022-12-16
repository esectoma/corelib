package com.nanako.refreshload

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nanako.R
import com.nanako.expandableadapter.ExpandableAdapter
import com.nanako.refreshload.RecyclerViewScrollUtil.isContentNearBottom
import androidx.databinding.library.baseAdapters.BR

class RefreshLoadLayout : SwipeRefreshLayout {
    /**
     * 加载更多的状态
     * 正在加载，加载失败，没有更多
     */
    var loadStatus = Load.STATUS_LOADING
        private set

    /**
     * 是否显示加载更多
     */
    private var showStatusUi = false

    /**
     * 是否正在执行加载更多的任务
     */
    private var taskLoading = false

    /**
     * 是否正在执行刷新任务
     */
    private var taskRefreshing = false
    private var refreshLoadView: View? = null
    private var onRefreshListener: OnRefreshListener? = null
    private var onLoadListener: OnLoadListener? = null

    //private val handler = Handler()
    private var setRefreshingRunn: SetRefreshingRunn? = null
    var isLoadEnabled = true
    private var refreshEnable = true
    private var initialDownYValue = 0f
    private var miniTouchSlop = 0

    constructor(context: Context?) : super(context!!) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val context = context
        miniTouchSlop = ViewConfiguration.get(context).scaledTouchSlop * 8
        //setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        //setDistanceToTriggerSync((int) getResources().getDimension(R.dimen.dp100));
        setRefreshingRunn = SetRefreshingRunn(this)
    }

    interface OnLoadListener {
        fun onLoad()
    }

    interface OnLoadFailedListener {
        fun onRetryLoad()
    }

    @JvmOverloads
    fun onComplete(isRefresh: Boolean, delayRefresh: Long = 500) {
        if (isRefresh) {
            onCompleteWhenRefresh(false, delayRefresh)
        } else {
            setLoading(false)
        }
    }

    private fun isWorking(): Boolean {
        val b1 = isRefreshing
        val b2 = isLoading()
        //L.GL.d("isRefreshing=%b, isLoading=%b", b1, b2);
        return b1 || b2
    }

    private fun isCanLoad(): Boolean {
        val b1 = isWorking()
        val b2 = Load.isLoadFailed(loadStatus)
        val b3 = Load.isLoadNoMoreData(loadStatus)
        //L.GL.d("isWorking=%b, isLoadFailed=%b, isLoadNoMore=%b", b1, b2, b3);
        return !(b1 || b2 || b3) && isLoadEnabled
    }


    override fun setOnRefreshListener(listener: OnRefreshListener?) {
        super.setOnRefreshListener(listener)
        onRefreshListener = listener
    }

    private fun notifyRefreshing() {
        if (onRefreshListener != null) {
            setStatusNoMoreData(false)
            onRefreshListener!!.onRefresh()
        }
    }

    override fun setRefreshing(refreshing: Boolean) {
        onSetRefreshing(refreshing)
        super.setRefreshing(refreshing)
    }

    fun autoRefreshWithoutUi() {
        onSetRefreshing(true)
    }

    private fun onCompleteWhenRefresh(refreshing: Boolean, delay: Long) {
        handler.removeCallbacks(setRefreshingRunn!!)
        onSetRefreshing(refreshing)
        handler.postDelayed(setRefreshingRunn!!.refreshing(refreshing), delay)
    }

    private class SetRefreshingRunn(private val mRefreshLoadLayout: RefreshLoadLayout) : Runnable {
        private var isRefreshing = false
        override fun run() {
            mRefreshLoadLayout.isRefreshing = isRefreshing
        }

        fun refreshing(refreshing: Boolean): Runnable {
            isRefreshing = refreshing
            return this
        }
    }

    private fun onSetRefreshing(refreshing: Boolean) {
        taskRefreshing = refreshing
        if (taskRefreshing) {
            notifyRefreshing()
        }
    }

    private fun isLoading(): Boolean {
        return taskLoading
    }

    private fun setLoading(loading: Boolean) {
        taskLoading = loading
        if (taskLoading && onLoadListener != null) {
            super.setEnabled(false)
            onLoadListener!!.onLoad()
        } else {
            super.setEnabled(refreshEnable)
        }
    }

    override fun isRefreshing(): Boolean {
        return taskRefreshing || super.isRefreshing()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        refreshEnable = enabled
    }

    fun setStatusLoading(showStatusUi: Boolean) {
        setShowStatusAndUi(showStatusUi, Load.STATUS_LOADING)
    }

    fun setStatusNoMoreData(showStatusUi: Boolean) {
        setShowStatusAndUi(showStatusUi, Load.STATUS_LOAD_NO_MORE_DATA)
    }

    fun setStatusFailed(showStatusUi: Boolean) {
        setShowStatusAndUi(showStatusUi, Load.STATUS_LOAD_FAILED)
    }

    private fun setShowStatusAndUi(showLoading: Boolean, status: Int) {
        showStatusUi = showLoading
        loadStatus = status
        if (showStatusUi) {
            showLoad();
        } else {
            removeLoad();
        }
    }

    fun showLoad() {
        getAdapter()?.showLoad(loadStatus)
    }

    fun removeLoad() {
        getAdapter()?.removeLoad()
    }

    fun getRecyclerView(): RecyclerView? {
        val gc = childCount
        var view: RecyclerView? = null
        for (i in 0 until gc) {
            if (getChildAt(i) is RecyclerView) {
                view = getChildAt(i) as RecyclerView
                break
            }
        }
        return view
    }

    fun getAdapter(): Adapter? {
        return ((refreshLoadView as? RecyclerView)?.adapter) as? Adapter
    }

    fun setOnLoadListener(onLoadListener: OnLoadListener?) {
        setOnLoadListener(getRecyclerView(), onLoadListener)
    }

    fun setOnLoadListener(view: View?, onLoadListener: OnLoadListener?) {
        this.onLoadListener = onLoadListener
        refreshLoadView = view
        addOnScrollListener()
    }

    fun setOnLoadFailedListener(onLoadFailedListener: OnLoadFailedListener) {
        getAdapter()?.onLoadFailedListener = onLoadFailedListener
    }

    private fun addOnScrollListener() {
        if (onLoadListener != null && refreshLoadView != null) {
            if (refreshLoadView is RecyclerView) {
                (refreshLoadView as RecyclerView).addOnScrollListener(object :
                    RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        if (dy > 0) { //上拉
                            if (isCanLoad()) {
                                if (showStatusUi && isContentNearBottom(
                                        recyclerView,
                                        5
                                    ) || isContentNearBottom(
                                        recyclerView
                                    )
                                ) { //最后一个可见的时候
                                    setLoading(true)
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        val action = ev.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> initialDownYValue = ev.y
            MotionEvent.ACTION_MOVE -> {
                val yDiff = ev.y - initialDownYValue
                if (yDiff < miniTouchSlop) {
                    return false
                }
            }
            else -> {}
        }
        return super.onInterceptTouchEvent(ev)
    }

    class Load(private var status: Int) : BaseObservable() {
        @Bindable
        fun getStatus(): Int {
            return status
        }

        fun setStatus(status: Int) {
            this.status = status
            notifyPropertyChanged(BR.status)
        }

        companion object {
            const val STATUS_LOADING = 0
            const val STATUS_LOAD_FAILED = 1
            const val STATUS_LOAD_NO_MORE_DATA = 2

            @JvmStatic
            fun getLoadText(context: Context, status: Int): String {
                if (status == STATUS_LOADING) {
                    return context.resources.getString(R.string.rlm_loading)
                } else if (status == STATUS_LOAD_FAILED) {
                    return context.resources.getString(R.string.rlm_load_failed)
                } else if (status == STATUS_LOAD_NO_MORE_DATA) {
                    return context.resources.getString(R.string.rlm_nomore)
                }
                return ""
            }

            @JvmStatic
            fun setLoadFailed(load: Load) {
                load.setStatus(STATUS_LOAD_FAILED)
            }

            @JvmStatic
            fun isLoadFailed(status: Int): Boolean {
                return status == STATUS_LOAD_FAILED
            }

            @JvmStatic
            fun isLoadNoMoreData(status: Int): Boolean {
                return status == STATUS_LOAD_NO_MORE_DATA
            }

            @JvmStatic
            fun setLoadNoMoreData(load: Load) {
                load.setStatus(STATUS_LOAD_NO_MORE_DATA)
            }

            @JvmStatic
            fun isLoadLoading(status: Int): Boolean {
                return status == STATUS_LOADING
            }

            @JvmStatic
            fun setLoadLoading(load: Load) {
                load.setStatus(STATUS_LOADING)
            }
        }
    }

    fun getStringResource(@StringRes stringRes: Int): String {
        return context.resources.getString(stringRes)
    }

    abstract class Adapter(var context: Context) : ExpandableAdapter() {
        var onLoadFailedListener: OnLoadFailedListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                onGetLoadViewType() -> LoadHolder(context, parent, onLoadFailedListener)
                else -> super.createViewHolder(parent, viewType)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is LoadHolder) {
                holder.onBindData(position, getItem(position) as Load)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return onGetItemViewType(getItem(position))
        }

        override fun onGetItemViewType(t: Any?): Int {
            return if (t is Load) onGetLoadViewType() else super.onGetItemViewType(t)
        }

        /**
         * 显示加载更多布局
         */
        open fun showLoad(status: Int) {
            val fc: Int = footerCount
            if (fc > 0) {
                val fp = findLoadPosition()
                if (fp != -1) {
                    val load = getFooter(fp) as Load?
                    load!!.setStatus(status)
                    notifyItemChanged(convertFooterPosition(fp))
                } else {
                    addFooter(Load(status))
                }
            } else {
                addFooter(Load(status))
            }
        }

        /**
         * 删除加载更多布局
         */
        open fun removeLoad() {
            val fp = findLoadPosition()
            if (fp != -1) {
                removeFooter(fp)
            }
        }

        /**
         * 找到加载更多的位置
         */
        protected open fun findLoadPosition(): Int {
            val fc: Int = footerCount
            var i: Int = 0
            while (i < fc) {
                if (onGetItemViewType(getFooter(i)) == onGetLoadViewType()) {
                    return i
                }
                i++
            }
            return -1
        }

        /**
         * 加载更多布局的ViewType值
         */
        open fun onGetLoadViewType(): Int {
            return -1
        }
    }
}