package com.app.expandableadapterx

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qbw.expandableadapterx.ExpandableAdapter

/**
 * barry 2022/10/5
 */
class TestAdapter : ExpandableAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemViewType(t: Any?): Int {
        return super.getItemViewType(t)
    }
}