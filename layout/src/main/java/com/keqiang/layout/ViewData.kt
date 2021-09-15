package com.keqiang.layout

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.viewholder.BaseViewHolder

// View数据
// Created by wanggaowan on 2021/9/15 15:41

sealed class ViewData(
    val context: Context,
    val type: Int
) {
    abstract fun getViewCount(): Int

    abstract fun getViewType(position: Int): Int

    abstract fun hasViewType(viewType: Int): Boolean

    abstract fun <VH : RecyclerView.ViewHolder> onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH

    abstract fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)

    abstract fun <T : View> findViewById(id: Int): T?
}

class LazyColumnViewData(
    context: Context,
    type: Int,
    val lazyColumn: LazyColumn
) : ViewData(context, type) {

    override fun getViewCount(): Int {
        return lazyColumn.getAdapter<RecyclerView.ViewHolder>()?.itemCount ?: 0
    }

    override fun getViewType(position: Int): Int {
        val itemViewType =
            lazyColumn.getAdapter<RecyclerView.ViewHolder>()?.getItemViewType(position)
        return if (itemViewType == null) {
            type
        } else {
            itemViewType + type
        }
    }

    override fun hasViewType(viewType: Int): Boolean {
        lazyColumn.getAdapter<RecyclerView.ViewHolder>()?.let {
            (0..it.itemCount).forEach { index ->
                if (it.getItemViewType(index) == viewType - type) {
                    return true
                }
            }
        }
        return false
    }

    override fun <VH : RecyclerView.ViewHolder> onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {
        return lazyColumn.getAdapter<VH>()?.onCreateViewHolder(parent, viewType - type) as VH
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        lazyColumn.getAdapter<RecyclerView.ViewHolder>()?.onBindViewHolder(holder, position)
    }

    override fun <T : View> findViewById(id: Int): T? {
        return null
    }
}

class NormalViewData(
    context: Context,
    type: Int,
    val children: MutableList<View>
) : ViewData(context, type) {

    private var viewHolder: RecyclerView.ViewHolder? = null
    private var lazyColumn: LazyColumn? = null
    private var isTop: Boolean = false

    override fun getViewCount(): Int {
        return 1
    }

    override fun getViewType(position: Int): Int {
        return type
    }

    override fun hasViewType(viewType: Int): Boolean {
        return viewType == type
    }

    override fun <VH : RecyclerView.ViewHolder> onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {
        if (viewHolder != null) {
            viewHolder!!.itemView.let {
                if (it.parent is ViewGroup) {
                    (it.parent as ViewGroup).removeView(it)
                }
            }

            return viewHolder as VH
        }

        val linearLayout = LinearLayout(context)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        linearLayout.layoutParams = params
        children.forEach { linearLayout.addView(it) }
        viewHolder = BaseViewHolder(linearLayout)
        return viewHolder as VH
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun <T : View> findViewById(id: Int): T? {
        return viewHolder?.itemView?.findViewById(id)
    }

    fun setLazyColumnData(lazyColumn: LazyColumn?, isTop: Boolean) {
        this.lazyColumn = lazyColumn
        this.isTop = isTop
    }
}