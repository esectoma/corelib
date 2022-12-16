package com.qbw.expandableadapterx

import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Bond on 2016/4/2.
 * Item Order below:
 *
 *
 * Header
 * Child
 * Group
 * ***GroupChild
 * Footer
 */
abstract class BaseExpandableAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    abstract fun getItem(adapPos: Int): Any

    /**
     * @param groupPosition group position in group data list
     * @return
     */
    abstract fun getGroupChildCount(groupPosition: Int): Int

    /**
     * Called when setHeader,setChild or setFooter is called
     *
     * @param oldData
     * @param newData
     * @return true[will not update ui] false[update ui]
     */
    abstract fun isSameData(oldData: Any?, newData: Any?): Boolean
}