package com.nanako.refreshload

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nanako.R
import com.nanako.databinding.LoadHolderBinding
import com.nanako.refreshload.RefreshLoadLayout.Load.Companion.isLoadFailed
import com.nanako.refreshload.RefreshLoadLayout.Load.Companion.setLoadLoading


class LoadHolder(
    private val context: Context?, val binding: LoadHolderBinding,
    private val onLoadFailedListener: RefreshLoadLayout.OnLoadFailedListener?
) : RecyclerView.ViewHolder(binding.root) {
    private var data: RefreshLoadLayout.Load? = null

    constructor(
        context: Context?,
        viewParent: ViewGroup?,
        onLoadFailedListener: RefreshLoadLayout.OnLoadFailedListener?
    ) : this(
        context, DataBindingUtil.inflate<LoadHolderBinding>(
            LayoutInflater.from(context), R.layout.load_holder, viewParent, false
        ), onLoadFailedListener
    ) {
    }

    fun onBindData(position: Int, data: RefreshLoadLayout.Load) {
        this.data = data
        binding.load = data
        binding.executePendingBindings()
        itemView.setOnClickListener {
            if (isLoadFailed(data.getStatus())) {
                if (onLoadFailedListener != null) {
                    setLoadLoading(data)
                    onLoadFailedListener.onRetryLoad()
                }
            }
        }
    }
}