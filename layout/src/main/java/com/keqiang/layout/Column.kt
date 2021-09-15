package com.keqiang.layout

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *
 *
 * @author Created by wanggaowan on 2021/9/15 09:19
 */
class Column @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        super.setOrientation(VERTICAL)
    }

    @Deprecated("call method does not perform any operation")
    override fun setOrientation(orientation: Int) {

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        viewMap()
    }

    private fun viewMap() {
        val children = mutableListOf<ViewData>()
        var viewType = 0
        var lazyViewType = 10000
        var viewData = NormalViewData(context, viewType++, mutableListOf())
        for (i in 0..childCount) {
            val child = getChildAt(i)
            if (child is LazyColumn) {
                if (viewData.children.size > 0) {
                    // 记录列表至上的数据，作为一个分组
                    viewData.setLazyColumnData(child, true)
                    children.add(viewData)
                }

                // 记录列表数据
                val lazyColumnViewData = LazyColumnViewData(context, lazyViewType++, child)
                children.add(lazyColumnViewData)

                // 记录列表之下的数据
                if (viewData.children.size == 0) {
                    viewData.setLazyColumnData(child, false)
                } else {
                    viewData = NormalViewData(context, viewType++, mutableListOf())
                    viewData.setLazyColumnData(child, false)
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
        val adapterProxy = AdapterProxy(children)
        recyclerView.adapter = adapterProxy

        removeAllViews()
        addView(recyclerView)
    }
}

private class AdapterProxy(val viewDataList: List<ViewData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var itemCount2: Int = 0

    init {
        viewDataList.forEach {
            itemCount2 += it.getViewCount()
        }
    }

    override fun getItemViewType(position: Int): Int {
        val (viewDataIndex, adapterIndex) = getActualPos(position)
        return viewDataList[viewDataIndex].getViewType(adapterIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val filter = viewDataList.filter { it.hasViewType(viewType) }
        if (filter.size > 1) {
            throw IllegalArgumentException("more than one view type implementation")
        }
        return filter[0].onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val (viewDataIndex, adapterIndex) = getActualPos(position)
        viewDataList[viewDataIndex].onBindViewHolder(holder, adapterIndex)
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
}
