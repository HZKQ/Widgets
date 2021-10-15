package com.keqiang.layout.combination

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.keqiang.layout.R

// View数据
// Created by wanggaowan on 2021/9/15 15:41

sealed class ViewData<T : View>(
    val context: Context,
    val type: Int,
    val view: T
) {
    abstract fun getViewCount(): Int

    abstract fun getViewType(position: Int): Int

    abstract fun hasViewType(viewType: Int): Boolean

    abstract fun <VH : RecyclerView.ViewHolder> createViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH

    abstract fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int)

    abstract fun <T : View> findViewById(id: Int): T?
}

class AdapterViewData(
    context: Context,
    type: Int,
    adapterView: AdapterView
) : ViewData<AdapterView>(context, type, adapterView) {

    private var mDataObserver: AdapterDataObserver? = null

    init {
        view.registerAdapterChangeListener { oldAdapter, adapter ->
            mDataObserver?.apply {
                oldAdapter?.unregisterAdapterDataObserver(this)
                adapter?.registerAdapterDataObserver(this)
                this.onChanged()
            }
        }

        view.setTag(R.id.lazy_column_layout_update_flag, 1L)
        view.registerLayoutChangeListener {
            val flag: Long = view.getTag(R.id.lazy_column_layout_update_flag) as Long
            view.setTag(
                R.id.lazy_column_layout_update_flag,
                if (flag == Long.MAX_VALUE) 1L else flag + 1
            )
            mDataObserver?.apply { this.onChanged() }
        }
    }

    override fun getViewCount(): Int {
        return view.getAdapter<RecyclerView.Adapter<*>>()?.itemCount ?: 0
    }

    override fun getViewType(position: Int): Int {
        val itemViewType =
            view.getAdapter<RecyclerView.Adapter<*>>()?.getItemViewType(position)
        return if (itemViewType == null) {
            type
        } else {
            itemViewType + type
        }
    }

    override fun hasViewType(viewType: Int): Boolean {
        view.getAdapter<RecyclerView.Adapter<*>>()?.let {
            (0..it.itemCount).forEach { index ->
                if (it.getItemViewType(index) == viewType - type) {
                    return true
                }
            }
        }
        return false
    }

    override fun <VH : RecyclerView.ViewHolder> createViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {
        return view.getAdapter<RecyclerView.Adapter<VH>>()?.createViewHolder(parent, viewType - type) as VH
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        view.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()?.bindViewHolder(holder, position)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : View> findViewById(id: Int): T? {
        return if (id == view.id) {
            view as T
        } else {
            null
        }
    }

    fun registerAdapterDataObserver(observer: AdapterDataObserver) {
        mDataObserver = observer
        view.getAdapter<RecyclerView.Adapter<*>>()?.registerAdapterDataObserver(observer)
    }

    fun unRegisterAdapterDataObserver() {
        mDataObserver?.apply {
            view.getAdapter<RecyclerView.Adapter<*>>()?.unregisterAdapterDataObserver(this)
        }
        mDataObserver = null
    }
}

/**
 * 普通View数据，非[LazyColumn]、[LazyRow]、[AdapterView]
 */
class NormalViewData(
    context: Context,
    type: Int,
    view: View
) : ViewData<View>(context, type, view) {

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
    override fun <VH : RecyclerView.ViewHolder> createViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {
        if (view.parent is ViewGroup) {
            (view.parent as ViewGroup).removeView(view)
        }

        return object : RecyclerView.ViewHolder(view) {} as VH
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    @Suppress("UNCHECKED_CAST")
    override fun <T : View> findViewById(id: Int): T? {
        if (view.id == id) {
            return view as T
        }

        return if (view is ViewGroup) view.findViewById(id) else null
    }
}

/**
 * [GroupPlaceholder] ViewData
 */
class GroupPlaceholderViewData(
    context: Context,
    view: GroupPlaceholder
) : ViewData<GroupPlaceholder>(context, -1, view) {

    override fun getViewCount(): Int {
        var dataCount = 0
        view.viewDataList.forEach {
            dataCount += it.getViewCount()
        }
        return dataCount
    }

    override fun getViewType(position: Int): Int {
        return type
    }

    override fun hasViewType(viewType: Int): Boolean {
        return viewType == type
    }

    @Suppress("UNCHECKED_CAST")
    override fun <VH : RecyclerView.ViewHolder> createViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {
        throw IllegalArgumentException("GroupPlaceholderViewData can not be show")
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    @Suppress("UNCHECKED_CAST")
    override fun <T : View> findViewById(id: Int): T? {
        if (view.id == id) {
            return view as T
        }

        return view.findViewById2(id)
    }
}

class LazyColumnData(
    context: Context,
    type: Int,
    lazyColumn: LazyColumn
) : ViewData<LazyColumn>(context, type, lazyColumn) {

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
    override fun <VH : RecyclerView.ViewHolder> createViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {
        if (view.parent is ViewGroup) {
            (view.parent as ViewGroup).removeView(view)
        }

        return object : RecyclerView.ViewHolder(view) {} as VH
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    @Suppress("UNCHECKED_CAST")
    override fun <T : View> findViewById(id: Int): T? {
        if (id == view.id) {
            return view as T
        }

        return view.findViewById2(id)
    }
}

class LazyRowData(
    context: Context,
    type: Int,
    lazyRow: LazyRow
) : ViewData<LazyRow>(context, type, lazyRow) {

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
    override fun <VH : RecyclerView.ViewHolder> createViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {
        if (view.parent is ViewGroup) {
            (view.parent as ViewGroup).removeView(view)
        }

        return object : RecyclerView.ViewHolder(view) {} as VH
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    @Suppress("UNCHECKED_CAST")
    override fun <T : View> findViewById(id: Int): T? {
        if (id == view.id) {
            return view as T
        }

        return view.findViewById2(id)
    }
}