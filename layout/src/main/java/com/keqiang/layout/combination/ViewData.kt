package com.keqiang.layout.combination

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.keqiang.layout.BuildConfig

// View数据
// Created by wanggaowan on 2021/9/15 15:41

sealed class ViewData<T : View>(
    val context: Context,
    var type: Int,
    val view: T,
    /**
     * view在父布局排列方向
     */
    val orientation: Int
) {
    abstract fun getViewCount(): Int

    abstract fun getViewType(position: Int): Int

    abstract fun hasViewType(viewType: Int): Boolean

    abstract fun <VH : RecyclerView.ViewHolder> createViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH

    abstract fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int)

    abstract fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>)

    abstract fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder)

    abstract fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder)

    abstract fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean

    abstract fun onViewRecycled(holder: RecyclerView.ViewHolder)
}

class AdapterViewData(
    context: Context,
    type: Int,
    adapterView: AdapterView,
    orientation: Int
) : ViewData<AdapterView>(context, type, adapterView, orientation) {

    private val mAdapterDataObserver = AdapterDataObserver()
    private var mDataObserver: RecyclerView.AdapterDataObserver? = null
    private var mAdapterChangeListener: AdapterChangeListener? = null

    init {
        view.layoutManager.orientation = orientation

        view.registerAdapterChangeListener { oldAdapter, adapter ->
            mAdapterChangeListener?.invoke(oldAdapter, adapter)
            unRegisterAdapterDataObserver(oldAdapter)
            registerAdapterDataObserver(adapter)
            mAdapterDataObserver.onChanged()
        }

        view.registerLayoutChangeListener { mAdapterDataObserver.onChanged() }
    }

    override fun getViewCount(): Int {
        return view.getViewCount()
    }

    override fun getViewType(position: Int): Int {
        if (position == 0 || position == getViewCount() - 1) {
            return BG_ITEM_TYPE
        }

        val itemViewType =
            view.getAdapter<RecyclerView.Adapter<*>>()?.getItemViewType(position - 1)
        return if (itemViewType == null) {
            type
        } else {
            itemViewType + type
        }
    }

    override fun hasViewType(viewType: Int): Boolean {
        if (viewType == BG_ITEM_TYPE) {
            return true
        }

        view.getAdapter<RecyclerView.Adapter<*>>()?.let {
            (0..it.itemCount).forEach { index ->
                if (it.getItemViewType(index) == viewType - type) {
                    return true
                }
            }
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    override fun <VH : RecyclerView.ViewHolder> createViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {

        if (viewType == BG_ITEM_TYPE) {
            val frameLayout = FrameLayout(parent.context)
            val params = if (orientation == LinearLayout.VERTICAL) {
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
            } else {
                ViewGroup.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            frameLayout.layoutParams = params
            return ViewHolderWrapper(frameLayout) as VH
        }

        val viewHolder = view.getAdapter<RecyclerView.Adapter<VH>>()?.createViewHolder(parent, viewType - type)
        return ViewHolderWrapper(viewHolder!!.itemView, viewHolder) as VH
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder !is ViewHolderWrapper) {
            return
        }

        @Suppress("UNCHECKED_CAST")
        holder.viewData = this
        holder.viewHolder?.let {
            setOwnerRecyclerView(it, view.recyclerView)
            // 清除BindingAdapter，调用viewData.bindViewHolder时会重新给position赋值
            clearBindingAdapter(it)
            if (position > 0 && position < getViewCount() - 1) {
                view.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()?.bindViewHolder(it, position - 1)
            }
        }
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        if (holder !is ViewHolderWrapper) {
            return
        }

        holder.viewHolder?.let { setPayloads(it, payloads) }
        bindViewHolder(holder, position)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder !is ViewHolderWrapper) {
            return
        }

        if (holder.viewHolder == null) {
            return
        }

        view.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()
            ?.onViewAttachedToWindow(holder.viewHolder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder !is ViewHolderWrapper) {
            return
        }

        if (holder.viewHolder == null) {
            return
        }

        view.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()
            ?.onViewDetachedFromWindow(holder.viewHolder)
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        if (holder !is ViewHolderWrapper) {
            return false
        }

        if (holder.viewHolder == null) {
            return false
        }

        return view.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()
            ?.onFailedToRecycleView(holder.viewHolder) ?: false
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder !is ViewHolderWrapper) {
            return
        }

        if (holder.viewHolder == null) {
            return
        }

        view.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()
            ?.onViewRecycled(holder.viewHolder)
    }

    private fun registerAdapterDataObserver(adapter: RecyclerView.Adapter<*>?) {
        try {
            adapter?.registerAdapterDataObserver(mAdapterDataObserver)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        }
    }

    private fun unRegisterAdapterDataObserver(adapter: RecyclerView.Adapter<*>?) {
        try {
            adapter?.unregisterAdapterDataObserver(mAdapterDataObserver)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        }
    }

    fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        registerAdapterDataObserver(view.getAdapter())
        mDataObserver = observer
    }

    fun unRegisterAdapterDataObserver() {
        unRegisterAdapterDataObserver(view.getAdapter())
        mDataObserver = null
    }

    fun setAdapterChangeListener(listener: AdapterChangeListener?) {
        mAdapterChangeListener = listener
    }

    private fun setPayloads(viewHolder: RecyclerView.ViewHolder, payloads: List<Any>?) {
        val clazz = RecyclerView.ViewHolder::class.java
        val declaredMethod = clazz.getDeclaredMethod("createPayloadsIfNeeded")
        declaredMethod.isAccessible = true
        declaredMethod.invoke(viewHolder)
        val declaredField = clazz.getDeclaredField("mPayloads")
        declaredField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val payload: MutableList<Any> = declaredField.get(viewHolder) as MutableList<Any>
        payload.clear()
        if (!payloads.isNullOrEmpty()) {
            payload.addAll(payloads)
        }
    }

    private fun setOwnerRecyclerView(viewHolder: RecyclerView.ViewHolder, recyclerView: RecyclerView) {
        val declaredField = RecyclerView.ViewHolder::class.java.getDeclaredField("mOwnerRecyclerView")
        declaredField.isAccessible = true
        declaredField.set(viewHolder, recyclerView)
    }

    private fun clearBindingAdapter(viewHolder: RecyclerView.ViewHolder) {
        val declaredField = RecyclerView.ViewHolder::class.java.getDeclaredField("mBindingAdapter")
        declaredField.isAccessible = true
        declaredField.set(viewHolder, null)
    }

    companion object {
        /**
         * 绘制背景类型
         */
        const val BG_ITEM_TYPE = 1000000000
    }

    /**
     * 对[AdapterView.getAdapter]生成的ViewHolder包装器，主要应用[AdapterView]设置的相关布局参数
     */
    internal class ViewHolderWrapper(
        view: View,
        val viewHolder: RecyclerView.ViewHolder? = null,
    ) : RecyclerView.ViewHolder(view) {
        var viewData: AdapterViewData? = null
    }

    /**
     * Adapter数据变更监听
     */
    private inner class AdapterDataObserver : RecyclerView.AdapterDataObserver() {

        override fun onChanged() {
            mDataObserver?.onChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            if (view.isGone) {
                return
            }

            // + 1 是AdapterView展示时，pos 0 位置有个Item用于绘制AdapterView padding、margin数据
            mDataObserver?.onItemRangeChanged(positionStart + 1, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            if (view.isGone) {
                return
            }

            mDataObserver?.onItemRangeChanged(positionStart + 1, itemCount, payload)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            if (view.isGone) {
                return
            }

            mDataObserver?.onItemRangeInserted(positionStart + 1, itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            if (view.isGone) {
                return
            }

            mDataObserver?.onItemRangeRemoved(positionStart + 1, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            if (view.isGone) {
                return
            }

            mDataObserver?.onItemRangeMoved(fromPosition + 1, toPosition + 1, itemCount)
        }
    }
}

/**
 * 普通View数据，非[LazyColumn]、[LazyRow]、[AdapterView]
 */
class NormalViewData(
    context: Context,
    type: Int,
    view: View,
    orientation: Int
) : ViewData<View>(context, type, view, orientation) {

    private lateinit var mWrapperView: FrameLayout

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

        if (!this::mWrapperView.isInitialized) {
            if (view.parent is ViewGroup) {
                (view.parent as ViewGroup).removeView(view)
            }

            // 包裹这一层是为了view改变可见性时，自定调整界面显示效果。否则view设置为gone时，如果view宽/高固定，
            // 那么界面显示上，宽/高所占空间并不会释放
            mWrapperView = DetectLayoutViewGroup(context, orientation, view)
        }

        if (mWrapperView.parent is ViewGroup) {
            (mWrapperView.parent as ViewGroup).removeView(mWrapperView)
        }

        return object : RecyclerView.ViewHolder(mWrapperView) {} as VH
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {}
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {}
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {}
    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {}
}

/**
 * [GroupPlaceholder] ViewData
 */
class GroupPlaceholderViewData(
    context: Context,
    view: GroupPlaceholder,
    orientation: Int
) : ViewData<GroupPlaceholder>(context, -1, view, orientation) {

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
    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {}
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {}
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {}
    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {}
}

class LazyColumnData(
    context: Context,
    type: Int,
    lazyColumn: LazyColumn,
    orientation: Int
) : ViewData<LazyColumn>(context, type, lazyColumn, orientation) {

    private lateinit var mWrapperView: FrameLayout

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

        if (!this::mWrapperView.isInitialized) {
            if (view.parent is ViewGroup) {
                (view.parent as ViewGroup).removeView(view)
            }

            // 包裹这一层是为了view改变可见性时，自定调整界面显示效果。否则view设置为gone时，如果view宽/高固定，
            // 那么界面显示上，宽/高所占空间并不会释放
            mWrapperView = DetectLayoutViewGroup(context, orientation, view)
        }

        if (mWrapperView.parent is ViewGroup) {
            (mWrapperView.parent as ViewGroup).removeView(mWrapperView)
        }

        return object : RecyclerView.ViewHolder(mWrapperView) {} as VH
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {}
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {}
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {}
    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {}
}

class LazyRowData(
    context: Context,
    type: Int,
    lazyRow: LazyRow,
    orientation: Int
) : ViewData<LazyRow>(context, type, lazyRow, orientation) {

    private lateinit var mWrapperView: FrameLayout

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

        if (!this::mWrapperView.isInitialized) {
            if (view.parent is ViewGroup) {
                (view.parent as ViewGroup).removeView(view)
            }

            // 包裹这一层是为了view改变可见性时，自定调整界面显示效果。否则view设置为gone时，如果view宽/高固定，
            // 那么界面显示上，宽/高所占空间并不会释放
            mWrapperView = DetectLayoutViewGroup(context, orientation, view)
        }

        if (mWrapperView.parent is ViewGroup) {
            (mWrapperView.parent as ViewGroup).removeView(mWrapperView)
        }

        return object : RecyclerView.ViewHolder(mWrapperView) {} as VH
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {}
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {}
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {}
    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {}
}