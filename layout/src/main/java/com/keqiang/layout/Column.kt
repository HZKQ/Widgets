package com.keqiang.layout

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 *
 *
 * @author Created by wanggaowan on 2021/9/15 09:19
 */
class Column @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var adapterProxy: AdapterProxy

    init {
        super.setOrientation(VERTICAL)
    }

    @Deprecated("call method does not perform any operation")
    override fun setOrientation(orientation: Int) {

    }

    fun <T : View?> findViewById2(@IdRes id: Int): T? {
        if (this::adapterProxy.isInitialized) {
            for (data in adapterProxy.viewDataList) {
                return data.findViewById(id) ?: continue
            }
        }
        return null
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        viewMap()
    }

    private fun viewMap() {
        if (isInEditMode) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child is LazyColumn) {
                    removeView(child)
                    addView(child.createPreviewView(this), i)
                }
            }

            return
        }

        val children = mutableListOf<ViewData>()
        var viewType = 0
        var lazyViewType = 10000
        var viewData = NormalViewData(context, viewType++, mutableListOf())
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is LazyColumn) {
                if (viewData.children.size > 0) {
                    // 记录列表至上的数据，作为一个分组
                    children.add(viewData)
                }

                // 记录列表数据
                val lazyColumnViewData = LazyColumnViewData(context, lazyViewType++, child)
                children.add(lazyColumnViewData)

                // 记录新分组数据
                if (viewData.children.size > 0) {
                    viewData = NormalViewData(context, viewType++, mutableListOf())
                }
            } else {
                viewData.children.add(child)
                if (i >= childCount - 1) {
                    children.add(viewData)
                }
            }
        }

        val recyclerView = RecyclerView(context)
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        recyclerView.layoutParams = params
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapterProxy = AdapterProxy(children)
        recyclerView.adapter = adapterProxy

        removeAllViews()
        addView(recyclerView)
    }
}

private class AdapterProxy(val viewDataList: List<ViewData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemCount2: Int = 0

    init {
        viewDataList.forEach {
            itemCount2 += it.getViewCount()
            if (it is LazyColumnViewData) {
                it.registerAdapterDataObserver(LazyColumnAdapterDataObserver(it))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val (viewDataIndex, adapterIndex) = getActualPos(position)
        return viewDataList[viewDataIndex].getViewType(adapterIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val filter = viewDataList.filter { it.hasViewType(viewType) }
        if (filter.size > 1) {
            throw IllegalArgumentException("more than one view type implementation")
        }

        val viewData = filter[0]
        val viewHolder: RecyclerView.ViewHolder = viewData.onCreateViewHolder(parent, viewType)
        if (viewData is LazyColumnViewData) {
            val frameLayout = FrameLayout(parent.context)
            val layoutParams = viewData.lazyColumn.layoutParams
            val params = ViewGroup.MarginLayoutParams(layoutParams.width, layoutParams.height)
            frameLayout.layoutParams = params
            frameLayout.addView(viewHolder.itemView)
            return ViewHolderWrapper(frameLayout, viewHolder, viewType)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val (viewDataIndex, adapterIndex) = getActualPos(position)
        val viewData = viewDataList[viewDataIndex]
        if (holder is ViewHolderWrapper) {
            setWrapperViewData(holder, adapterIndex, viewData as LazyColumnViewData)
            viewData.onBindViewHolder(holder.viewHolder, adapterIndex)
        } else {
            viewData.onBindViewHolder(holder, adapterIndex)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        val (viewDataIndex, adapterIndex) = getActualPos(position)
        val viewData = viewDataList[viewDataIndex]
        if (holder is ViewHolderWrapper) {
            setWrapperViewData(holder, adapterIndex, viewData as LazyColumnViewData)
            viewData.onBindViewHolder(holder.viewHolder, adapterIndex, payloads)
        } else {
            viewData.onBindViewHolder(holder, adapterIndex, payloads)
        }
    }

    private fun setWrapperViewData(
        holder: ViewHolderWrapper,
        adapterIndex: Int,
        viewData: LazyColumnViewData
    ) {

        // 0：顶部，1：中间，2：底部
        val itemType = when (adapterIndex) {
            0 -> 0
            viewData.getViewCount() - 1 -> 2
            else -> 1
        }

        val oldItemType = holder.itemView.getTag(R.id.lazy_column_wrapper_item_type) as Int?
        val flag: Long? = holder.itemView.getTag(R.id.lazy_column_layout_update_flag) as Long?
        val newFlag: Long = viewData.lazyColumn.getTag(R.id.lazy_column_layout_update_flag) as Long
        if (flag != null && flag == newFlag && oldItemType != null && oldItemType == itemType) {
            return
        }

        holder.itemView.setTag(R.id.lazy_column_layout_update_flag, newFlag)
        holder.itemView.setTag(R.id.lazy_column_wrapper_item_type, itemType)
        val paddingStart = viewData.lazyColumn.paddingStart
        val paddingEnd = viewData.lazyColumn.paddingEnd
        var paddingTop = 0
        var paddingBottom = 0

        val marginStart = viewData.lazyColumn.marginStart
        val marginEnd = viewData.lazyColumn.marginEnd
        var marginTop = 0
        var marginBottom = 0

        if (itemType == 0 || itemType == 2) {
            if (itemType == 0) {
                marginTop = viewData.lazyColumn.marginTop
                paddingTop = viewData.lazyColumn.paddingTop
            } else {
                marginBottom = viewData.lazyColumn.marginBottom
                paddingBottom = viewData.lazyColumn.paddingBottom
            }
        }

        holder.itemView.setPaddingRelative(
            paddingStart,
            paddingTop,
            paddingEnd,
            paddingBottom
        )

        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = viewData.lazyColumn.layoutParams.width
        layoutParams.height = viewData.lazyColumn.layoutParams.height
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.marginStart = marginStart
            layoutParams.marginEnd = marginEnd
            layoutParams.topMargin = marginTop
            layoutParams.bottomMargin = marginBottom
            holder.itemView.layoutParams = layoutParams
        }

        holder.itemView.background = viewData.lazyColumn.backgroundClone()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.itemView.backgroundTintMode = viewData.lazyColumn.backgroundTintMode
            holder.itemView.backgroundTintList = viewData.lazyColumn.backgroundTintList
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            holder.itemView.backgroundTintBlendMode = viewData.lazyColumn.backgroundTintBlendMode
        }
    }

    override fun getItemCount(): Int = itemCount2

    private fun getActualPos(position: Int): Pair<Int, Int> {
        var index = 0
        for (i in viewDataList.indices) {
            val viewCount = viewDataList[i].getViewCount()
            if (position <= index + viewCount - 1) {
                return Pair(i, position - index)
            }
            index += viewCount
        }
        return Pair(-1, -1)
    }

    private fun dataCountChange() {
        itemCount2 = 0
        viewDataList.forEach {
            itemCount2 += it.getViewCount()
        }
    }

    class ViewHolderWrapper(view: View, val viewHolder: RecyclerView.ViewHolder, val type: Int) :
        RecyclerView.ViewHolder(view)

    inner class LazyColumnAdapterDataObserver(val lazyColumnViewData: LazyColumnViewData) :
        RecyclerView.AdapterDataObserver() {

        override fun onChanged() {
            val actualStartPos = getActualStartPos()
            if (actualStartPos == -1) {
                return
            }

            dataCountChange()
            notifyItemRangeChanged(
                actualStartPos,
                lazyColumnViewData.getViewCount()
            )
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            val actualStartPos = getActualStartPos()
            if (actualStartPos == -1) {
                return
            }

            notifyItemRangeChanged(actualStartPos + positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            val actualStartPos = getActualStartPos()
            if (actualStartPos == -1) {
                return
            }

            notifyItemRangeChanged(actualStartPos + positionStart, itemCount, payload)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            val actualStartPos = getActualStartPos()
            if (actualStartPos == -1) {
                return
            }

            dataCountChange()
            notifyItemRangeInserted(actualStartPos + positionStart, itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            val actualStartPos = getActualStartPos()
            if (actualStartPos == -1) {
                return
            }

            dataCountChange()
            notifyItemRangeRemoved(actualStartPos + positionStart, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            require(itemCount == 1) { "Moving more than 1 item is not supported yet" }

            val actualStartPos = getActualStartPos()
            if (actualStartPos == -1) {
                return
            }

            dataCountChange()
            notifyItemMoved(actualStartPos + fromPosition, actualStartPos + toPosition)
        }

        /**
         * 获取当前数据在列表的真实起始位置,为找到对应数据，则返回-1
         */
        private fun getActualStartPos(): Int {
            var index = 0
            for (data in viewDataList) {
                if (data == lazyColumnViewData) {
                    return index
                }
                index += data.getViewCount()
            }

            return -1
        }
    }
}
