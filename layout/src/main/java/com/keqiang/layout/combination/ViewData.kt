package com.keqiang.layout.combination

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.keqiang.layout.R

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

    abstract fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>
    )

    abstract fun <T : View> findViewById(id: Int): T?
}

class LazyColumnViewData(
    context: Context,
    type: Int,
    val lazyColumn: LazyColumn
) : ViewData(context, type) {

    private var mDataObserver: AdapterDataObserver? = null

    init {
        lazyColumn.registerAdapterChangeListener { oldAdapter, adapter ->
            mDataObserver?.apply {
                oldAdapter?.unregisterAdapterDataObserver(this)
                adapter?.registerAdapterDataObserver(this)
                this.onChanged()
            }
        }

        lazyColumn.setTag(R.id.lazy_column_layout_update_flag, 1L)
        lazyColumn.registerLayoutChangeListener {
            val flag: Long = lazyColumn.getTag(R.id.lazy_column_layout_update_flag) as Long
            lazyColumn.setTag(
                R.id.lazy_column_layout_update_flag,
                if (flag == Long.MAX_VALUE) 1L else flag + 1
            )
            mDataObserver?.apply { this.onChanged() }
        }
    }

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

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        lazyColumn.getAdapter<RecyclerView.ViewHolder>()
            ?.onBindViewHolder(holder, position, payloads)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : View> findViewById(id: Int): T? {
        return if (id == lazyColumn.id) {
            lazyColumn as T
        } else {
            null
        }
    }

    fun registerAdapterDataObserver(observer: AdapterDataObserver) {
        mDataObserver = observer
        lazyColumn.getAdapter<RecyclerView.ViewHolder>()?.registerAdapterDataObserver(observer)
    }
}

class NormalViewData(
    context: Context,
    type: Int,
    val children: MutableList<View>
) : ViewData(context, type) {

    private var viewHolder: RecyclerView.ViewHolder? = null

    override fun getViewCount(): Int {
        return 1
    }

    override fun getViewType(position: Int): Int {
        return type
    }

    override fun hasViewType(viewType: Int): Boolean {
        return viewType == type
    }

    @Suppress("UNCHECKED_CAST")
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
        linearLayout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        linearLayout.layoutParams = params
        children.forEach { linearLayout.addView(it) }
        viewHolder = BaseViewHolder(linearLayout)
        return viewHolder as VH
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : View> findViewById(id: Int): T? {
        for (view in children) {
            if (view.id == id) {
                return view as T
            }
        }

        return viewHolder?.itemView?.findViewById(id)
    }

    fun addView(view: View, index: Int) {
        children.add(index, view)
        viewHolder?.itemView?.apply {
            (this as LinearLayout).addView(view, index)
        }
    }

    fun removeView(view: View) {
        children.remove(view)
        viewHolder?.itemView?.apply {
            (this as LinearLayout).removeView(view)
        }
    }

    fun removeView(index: Int) {
        children.removeAt(index)
        viewHolder?.itemView?.apply {
            (this as LinearLayout).removeViewAt(index)
        }
    }
}