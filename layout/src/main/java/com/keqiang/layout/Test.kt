package com.keqiang.layout

import androidx.recyclerview.widget.RecyclerView

/**
 * @author Created by wanggaowan on 2021/9/15 17:24
 */
internal class Test {
    private var mAdapter: RecyclerView.Adapter<*>? = null
    fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        mAdapter = adapter
    }

    fun <T : RecyclerView.ViewHolder> getAdapter(): RecyclerView.Adapter<T>? {
        return mAdapter as RecyclerView.Adapter<T>?
    }
}