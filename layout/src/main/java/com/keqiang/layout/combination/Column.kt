package com.keqiang.layout.combination

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keqiang.layout.R
import kotlin.collections.set

/**
 *
 *
 * @author Created by wanggaowan on 2021/9/15 09:19
 */
class Column @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var adapterProxy: AdapterProxy
    private var viewType = 0
    private var lazyViewType = 10000
    private var shareLazyViewType = 100000
    private val shareLazyViewTypeMap: MutableMap<String, Int> = mutableMapOf()

    init {
        super.setOrientation(VERTICAL)
    }

    @Deprecated("call method does not perform any operation")
    override fun setOrientation(orientation: Int) {

    }

    /**
     * 查找Column布局中的View，由于[Column]对布局进行重新组合，因此使用[findViewById]无法查找xml中对应的[View]
     */
    fun <T : View?> findViewById2(@IdRes id: Int): T? {
        if (this::adapterProxy.isInitialized) {
            for (data in adapterProxy.viewDataList) {
                return data.findViewById(id) ?: continue
            }
        }
        return null
    }

    override fun addView(child: View, index: Int) {
        adapterProxy.addView(child, index, false)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        val lp = if (checkLayoutParams(params)) params else generateLayoutParams(params)
        child.layoutParams = lp
        addView(child, index)
    }

    override fun addView(child: View, width: Int, height: Int) {
        val lp = generateDefaultLayoutParams()
        lp.width = width
        lp.height = height
        child.layoutParams = lp
        addView(child)
    }

    override fun addViewInLayout(
        child: View,
        index: Int,
        params: ViewGroup.LayoutParams,
        preventRequestLayout: Boolean
    ): Boolean {
        val lp = if (checkLayoutParams(params)) params else generateLayoutParams(params)
        child.layoutParams = lp
        adapterProxy.addView(child, index, preventRequestLayout)
        return true
    }

    override fun removeAllViews() {
        super.removeAllViews()
    }

    override fun removeView(view: View?) {
        super.removeView(view)
    }

    override fun removeViewAt(index: Int) {
        super.removeViewAt(index)
    }

    override fun removeViews(start: Int, count: Int) {
        super.removeViews(start, count)
    }

    override fun removeAllViewsInLayout() {
        super.removeAllViewsInLayout()
    }

    override fun removeViewInLayout(view: View?) {
        super.removeViewInLayout(view)
    }

    override fun removeDetachedView(child: View?, animate: Boolean) {
        super.removeDetachedView(child, animate)
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
        var viewData = NormalViewData(context, viewType++, mutableListOf())
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is LazyColumn) {
                if (viewData.children.size > 0) {
                    // 记录列表至上的数据，作为一个分组
                    children.add(viewData)
                }

                // 记录列表数据
                val lazyColumnViewData = LazyColumnViewData(
                    context,
                    if (child.isolateViewTypes) lazyViewType++ else {
                        if (!shareLazyViewTypeMap.containsKey(child.typeFlag)) {
                            shareLazyViewTypeMap[child.typeFlag] = shareLazyViewType++
                        }
                        shareLazyViewTypeMap[child.typeFlag]!!
                    }, child
                )
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

    class ViewHolderWrapper(
        view: View,
        val viewHolder: RecyclerView.ViewHolder,
        val type: Int
    ) : RecyclerView.ViewHolder(view)

    inner class AdapterProxy(var viewDataList: MutableList<ViewData>) :
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
            val (viewDataIndex, adapterIndex) = adapterIndexToViewDataListIndex(position)
            return viewDataList[viewDataIndex].getViewType(adapterIndex)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val filter = viewDataList.filter { it.hasViewType(viewType) }
            checkCouldCreateViewHolder(filter)
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

        private fun checkCouldCreateViewHolder(viewDataList: List<ViewData>) {
            if (viewDataList.size > 1) {
                var isShare = true
                var typeFlag: String? = null
                for (data in viewDataList) {
                    if (data !is LazyColumnViewData) {
                        isShare = false
                        break
                    } else if (data.lazyColumn.isolateViewTypes) {
                        isShare = false
                        break
                    } else {
                        if (typeFlag == null) {
                            typeFlag = data.lazyColumn.typeFlag
                        } else if (typeFlag != data.lazyColumn.typeFlag) {
                            isShare = false
                            break
                        }
                    }
                }

                if (!isShare) {
                    throw IllegalArgumentException("more than one view type implementation")
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val (viewDataIndex, adapterIndex) = adapterIndexToViewDataListIndex(position)
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
            val (viewDataIndex, adapterIndex) = adapterIndexToViewDataListIndex(position)
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
            val newFlag: Long =
                viewData.lazyColumn.getTag(R.id.lazy_column_layout_update_flag) as Long + viewData.lazyColumn.hashCode()
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
                holder.itemView.backgroundTintBlendMode =
                    viewData.lazyColumn.backgroundTintBlendMode
            }
        }

        override fun getItemCount(): Int = itemCount2

        private fun dataCountChange() {
            itemCount2 = 0
            viewDataList.forEach {
                itemCount2 += it.getViewCount()
            }
        }

        fun addView(child: View, index: Int, preventRequestLayout: Boolean) {
            if (index == -1) {
                val viewData = viewDataList[viewDataList.size - 1]
                if (child is LazyColumn || viewData is LazyColumnViewData) {
                    val childData: ViewData
                    if (child !is LazyColumn) {
                        childData = NormalViewData(context, viewType++, mutableListOf()).apply {
                            children.add(child)
                        }
                    } else {
                        childData = LazyColumnViewData(
                            context,
                            if (child.isolateViewTypes) lazyViewType++ else {
                                if (!shareLazyViewTypeMap.containsKey(child.typeFlag)) {
                                    shareLazyViewTypeMap[child.typeFlag] = shareLazyViewType++
                                }
                                shareLazyViewTypeMap[child.typeFlag]!!
                            }, child
                        )
                    }

                    viewDataList.add(childData)
                    dataCountChange()
                    notifyItemInserted(itemCount)
                } else {
                    (viewDataList[viewDataList.size - 1] as NormalViewData).apply {
                        children.add(child)
                    }
                }
            } else {
                val (viewDataIndex, dataIndex) = viewIndexToViewDataListIndex(index)
                if (viewDataIndex != -1) {
                    val viewData = viewDataList[viewDataIndex]
                    when {
                        viewData is NormalViewData -> {
                            viewData.apply {
                                children.add(dataIndex, child)
                            }
                        }

                        viewDataIndex == 0 -> {
                            val childData =
                                NormalViewData(context, viewType++, mutableListOf()).apply {
                                    children.add(child)
                                }
                            viewDataList.add(0, childData)
                            dataCountChange()
                            notifyItemInserted(0)
                        }

                        viewDataList[viewDataIndex - 1] is NormalViewData -> {
                            (viewDataList[viewDataIndex - 1] as NormalViewData).apply {
                                children.add(dataIndex, child)
                            }
                        }

                        else -> {
                            val childData =
                                NormalViewData(context, viewType++, mutableListOf()).apply {
                                    children.add(child)
                                }
                            viewDataList.add(viewDataIndex, childData)
                            dataCountChange()
                            notifyItemInserted(viewDataIndex)
                        }
                    }
                }
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun removeAllViews() {
            viewDataList.clear()
            notifyDataSetChanged()
        }

        fun removeView(view: View?) {
            view?.apply {
                val index = viewToViewDataListIndex(view)
                if (index == -1) {
                    return
                }

                val viewData = viewDataList[index]
                if (viewData is LazyColumnViewData) {
                    viewDataList.removeAt(index)
                    val startPos = viewDataToAdapterStartIndex(viewData)
                    notifyItemRangeRemoved(startPos, viewData.getViewCount())
                } else {
                    (viewData as NormalViewData).apply { removeView(view) }
                }
            }
        }

        fun removeViewAt(index: Int) {
            val (viewDataIndex, dataIndex) = viewIndexToViewDataListIndex(index)
            if (viewDataIndex != -1) {
                val viewData = viewDataList[viewDataIndex]
                if (viewData is LazyColumnViewData) {
                    viewDataList.removeAt(viewDataIndex)
                    val startPos = viewDataToAdapterStartIndex(viewData)
                    notifyItemRangeRemoved(startPos, viewData.getViewCount())
                } else {
                    (viewData as NormalViewData).apply { removeView(dataIndex) }
                }
            }
        }


        /**
         * [AdapterProxy]下标转化为[viewDataList]列表中对应数据下标以及数据在[ViewData]中真实的下标位置
         */
        private fun adapterIndexToViewDataListIndex(position: Int): Pair<Int, Int> {
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

        /**
         * 布局文件中view的下标转化为[viewDataList]列表中对应数据下标以及数据在[ViewData]中真实的下标位置
         */
        private fun viewIndexToViewDataListIndex(position: Int): Pair<Int, Int> {
            var index = 0
            for (i in viewDataList.indices) {
                val data = viewDataList[i]
                val viewCount = if (data is NormalViewData) {
                    data.children.size
                } else {
                    1
                }
                if (position <= index + viewCount - 1) {
                    return Pair(i, position - index)
                }
                index += viewCount
            }

            return Pair(-1, -1)
        }

        /**
         * 查找布局文件中view对象在[viewDataList]列表中对应数据下标以及数据在[ViewData]中真实的下标位置
         */
        private fun viewToViewDataListIndex(view: View): Int {
            for (i in viewDataList.indices) {
                val data = viewDataList[i]
                if (data == view) {
                    return i
                } else if (data is NormalViewData) {
                    for (child in data.children) {
                        if (child == view) {
                            return i
                        }
                    }
                }
            }

            return -1
        }

        /**
         * 获取指定[viewData]在[AdapterProxy]的真实起始位置,未找到对应数据，则返回-1
         */
        private fun viewDataToAdapterStartIndex(viewData: ViewData): Int {
            var index = 0
            for (data in viewDataList) {
                if (data == viewData) {
                    return index
                }
                index += data.getViewCount()
            }

            return -1
        }

        inner class LazyColumnAdapterDataObserver(private val lazyColumnViewData: LazyColumnViewData) :
            RecyclerView.AdapterDataObserver() {

            override fun onChanged() {
                val actualStartPos = viewDataToAdapterStartIndex(lazyColumnViewData)
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
                val actualStartPos = viewDataToAdapterStartIndex(lazyColumnViewData)
                if (actualStartPos == -1) {
                    return
                }

                notifyItemRangeChanged(actualStartPos + positionStart, itemCount)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                val actualStartPos = viewDataToAdapterStartIndex(lazyColumnViewData)
                if (actualStartPos == -1) {
                    return
                }

                notifyItemRangeChanged(actualStartPos + positionStart, itemCount, payload)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                val actualStartPos = viewDataToAdapterStartIndex(lazyColumnViewData)
                if (actualStartPos == -1) {
                    return
                }

                dataCountChange()
                notifyItemRangeInserted(actualStartPos + positionStart, itemCount)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                val actualStartPos = viewDataToAdapterStartIndex(lazyColumnViewData)
                if (actualStartPos == -1) {
                    return
                }

                dataCountChange()
                notifyItemRangeRemoved(actualStartPos + positionStart, itemCount)
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                require(itemCount == 1) { "Moving more than 1 item is not supported yet" }

                val actualStartPos = viewDataToAdapterStartIndex(lazyColumnViewData)
                if (actualStartPos == -1) {
                    return
                }

                dataCountChange()
                notifyItemMoved(actualStartPos + fromPosition, actualStartPos + toPosition)
            }
        }
    }
}
