package com.keqiang.layout.combination

import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.*
import androidx.recyclerview.widget.RecyclerView
import com.keqiang.layout.BuildConfig
import com.keqiang.layout.R

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
        view.recyclerViewLayoutManager.orientation = orientation

        view.registerAdapterChangeListener { oldAdapter, adapter ->
            mAdapterChangeListener?.invoke(oldAdapter, adapter)
            unRegisterAdapterDataObserver(oldAdapter)
            registerAdapterDataObserver(adapter)
            mAdapterDataObserver.onChanged()
        }

        view.setTag(R.id.adapter_view_layout_update_flag, 1L)
        view.registerLayoutChangeListener {
            val flag: Long = view.getTag(R.id.adapter_view_layout_update_flag) as Long
            view.setTag(
                R.id.adapter_view_layout_update_flag,
                if (flag == Long.MAX_VALUE) 1L else flag + 1
            )
            mAdapterDataObserver.onChanged()
        }
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

        val frameLayout = FrameLayout(parent.context)
        val layoutParams = view.layoutParams
        val params = if (orientation == LinearLayout.VERTICAL) {
            ViewGroup.MarginLayoutParams(layoutParams.width, LinearLayout.LayoutParams.WRAP_CONTENT)
        } else {
            ViewGroup.MarginLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, layoutParams.height)
        }
        frameLayout.layoutParams = params

        if (viewType == BG_ITEM_TYPE) {
            return ViewHolderWrapper(frameLayout) as VH
        }

        val viewHolder = view.getAdapter<RecyclerView.Adapter<VH>>()?.createViewHolder(parent, viewType - type)
        return if (viewHolder != null) {
            frameLayout.addView(viewHolder.itemView)
            ViewHolderWrapper(frameLayout, viewHolder) as VH
        } else {
            ViewHolderWrapper(frameLayout) as VH
        }
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder !is ViewHolderWrapper) {
            return
        }

        @Suppress("UNCHECKED_CAST")
        holder.viewData = this
        applyAdapterViewLayoutParams(holder, position, this)
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

    /**
     * 给[AdapterView] adapter item应用[AdapterView]的布局数据
     */
    private fun applyAdapterViewLayoutParams(
        holder: ViewHolderWrapper,
        adapterIndex: Int,
        viewData: AdapterViewData
    ) {

        // 0：顶部，1：中间，2：底部
        val itemType = when (adapterIndex) {
            0 -> 0
            viewData.getViewCount() - 1 -> 2
            else -> 1
        }

        val oldItemType = holder.itemView.getTag(R.id.adapter_view_wrapper_item_type) as Int?
        val flag: Long? = holder.itemView.getTag(R.id.adapter_view_layout_update_flag) as Long?
        val newFlag: Long =
            viewData.view.getTag(R.id.adapter_view_layout_update_flag) as Long + viewData.view.hashCode()
        if (flag != null && flag == newFlag && oldItemType != null && oldItemType == itemType) {
            // 所在LazyColumn父对应布局参数未改变，且复用的item viewType一致
            return
        }

        holder.itemView.visibility = viewData.view.visibility
        holder.itemView.setTag(R.id.adapter_view_layout_update_flag, newFlag)
        holder.itemView.setTag(R.id.adapter_view_wrapper_item_type, itemType)
        val paddingStart = viewData.view.paddingStart
        val paddingEnd = viewData.view.paddingEnd
        var paddingTop = 0
        var paddingBottom = 0

        val marginStart = viewData.view.marginStart
        val marginEnd = viewData.view.marginEnd
        var marginTop = 0
        var marginBottom = 0

        if (itemType == 0 || itemType == 2) {
            if (itemType == 0) {
                marginTop = viewData.view.marginTop
                paddingTop = viewData.view.paddingTop
            } else {
                marginBottom = viewData.view.marginBottom
                paddingBottom = viewData.view.paddingBottom
            }
        }

        holder.itemView.setPaddingRelative(
            paddingStart,
            paddingTop,
            paddingEnd,
            paddingBottom
        )

        val layoutParams = holder.itemView.layoutParams
        if (orientation == LinearLayout.HORIZONTAL) {
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = viewData.view.layoutParams.height
        } else {
            layoutParams.width = viewData.view.layoutParams.width
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
        }

        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.marginStart = marginStart
            layoutParams.marginEnd = marginEnd
            layoutParams.topMargin = marginTop
            layoutParams.bottomMargin = marginBottom
            holder.itemView.layoutParams = layoutParams
        }

        holder.itemView.background = viewData.view.backgroundClone()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.itemView.backgroundTintMode = viewData.view.backgroundTintMode
            holder.itemView.backgroundTintList = viewData.view.backgroundTintList
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            holder.itemView.backgroundTintBlendMode =
                viewData.view.backgroundTintBlendMode
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