package com.keqiang.layout.combination

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
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
    val adapterView: AdapterView
) : ViewData(context, type) {

    private var mDataObserver: AdapterDataObserver? = null

    init {
        adapterView.registerAdapterChangeListener { oldAdapter, adapter ->
            mDataObserver?.apply {
                oldAdapter?.unregisterAdapterDataObserver(this)
                adapter?.registerAdapterDataObserver(this)
                this.onChanged()
            }
        }

        adapterView.setTag(R.id.lazy_column_layout_update_flag, 1L)
        adapterView.registerLayoutChangeListener {
            val flag: Long = adapterView.getTag(R.id.lazy_column_layout_update_flag) as Long
            adapterView.setTag(
                R.id.lazy_column_layout_update_flag,
                if (flag == Long.MAX_VALUE) 1L else flag + 1
            )
            mDataObserver?.apply { this.onChanged() }
        }
    }

    override fun getViewCount(): Int {
        return adapterView.getAdapter<RecyclerView.Adapter<*>>()?.itemCount ?: 0
    }

    override fun getViewType(position: Int): Int {
        val itemViewType =
            adapterView.getAdapter<RecyclerView.Adapter<*>>()?.getItemViewType(position)
        return if (itemViewType == null) {
            type
        } else {
            itemViewType + type
        }
    }

    override fun hasViewType(viewType: Int): Boolean {
        adapterView.getAdapter<RecyclerView.Adapter<*>>()?.let {
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
        return adapterView.getAdapter<RecyclerView.Adapter<VH>>()?.createViewHolder(parent, viewType - type) as VH
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        adapterView.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()?.bindViewHolder(holder, position)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : View> findViewById(id: Int): T? {
        return if (id == adapterView.id) {
            adapterView as T
        } else {
            null
        }
    }

    fun registerAdapterDataObserver(observer: AdapterDataObserver) {
        mDataObserver = observer
        adapterView.getAdapter<RecyclerView.Adapter<*>>()?.registerAdapterDataObserver(observer)
    }

    fun unRegisterAdapterDataObserver() {
        mDataObserver?.apply {
            adapterView.getAdapter<RecyclerView.Adapter<*>>()?.unregisterAdapterDataObserver(this)
        }
    }
}

class NormalViewData(
    context: Context,
    type: Int,
    val orientation: Int,
    val children: MutableList<View>
) : ViewData(context, type) {

    private var linearLayout: LinearLayout? = null

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
        if (linearLayout == null) {
            val linearLayout = LinearLayout(context)
            linearLayout.orientation = orientation
            val params = if (orientation == LinearLayout.VERTICAL) {
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            } else {
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            linearLayout.layoutParams = params
            children.forEach {
                if (it.parent is ViewGroup) {
                    (it.parent as ViewGroup).removeView(it)
                }
                linearLayout.addView(it)
            }
            this.linearLayout = linearLayout
        }

        if (linearLayout?.parent is ViewGroup) {
            (linearLayout?.parent as ViewGroup).removeView(linearLayout)
        }

        return object : RecyclerView.ViewHolder(linearLayout!!) {} as VH
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    @Suppress("UNCHECKED_CAST")
    override fun <T : View> findViewById(id: Int): T? {
        for (view in children) {
            if (view.id == id) {
                return view as T
            } else if (view is CombinationLayout) {
                return view.findViewById2(id) ?: continue
            }
        }

        return linearLayout?.findViewById(id)
    }

    fun addView(view: View, index: Int) {
        if (index == -1) {
            children.add(view)
        } else {
            children.add(index, view)
        }

        linearLayout?.apply {
            if (index == -1) {
                addView(view)
            } else {
                addView(view, index)
            }
        }
    }

    fun removeView(view: View, preventRequestLayout: Boolean) {
        children.remove(view)
        linearLayout?.apply {
            if (preventRequestLayout) {
                removeViewInLayout(view)
            } else {
                removeView(view)
            }
        }
    }

    fun removeView(index: Int, preventRequestLayout: Boolean) {
        removeView(children[index], preventRequestLayout)
    }

    fun invalidate() {
        linearLayout?.invalidate()
    }

    fun requestLayout() {
        linearLayout?.requestLayout()
    }
}

class LazyColumnData(
    context: Context,
    type: Int,
    val lazyColumn: LazyColumn
) : ViewData(context, type) {

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
        if (lazyColumn.parent is ViewGroup) {
            (lazyColumn.parent as ViewGroup).removeView(lazyColumn)
        }

        return object : RecyclerView.ViewHolder(lazyColumn) {} as VH
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    @Suppress("UNCHECKED_CAST")
    override fun <T : View> findViewById(id: Int): T? {
        if (id == lazyColumn.id) {
            return lazyColumn as T
        }

        return lazyColumn.findViewById2(id)
    }
}

class LazyRowData(
    context: Context,
    type: Int,
    val lazyRow: LazyRow
) : ViewData(context, type) {

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
        if (lazyRow.parent is ViewGroup) {
            (lazyRow.parent as ViewGroup).removeView(lazyRow)
        }

        return object : RecyclerView.ViewHolder(lazyRow) {} as VH
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    @Suppress("UNCHECKED_CAST")
    override fun <T : View> findViewById(id: Int): T? {
        if (id == lazyRow.id) {
            return lazyRow as T
        }

        return lazyRow.findViewById2(id)
    }
}