package com.keqiang.layout.combination

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.core.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keqiang.layout.R
import kotlin.collections.set
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.abs

/**
 * 实现[RecyclerView]的效果，但是只需要在xml中配置布局就可以实现多类型Item，多[RecyclerView.Adapter]组合的功能
 *
 * @author Created by wanggaowan on 2021/9/15 09:19
 */
abstract class CombinationLayout constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    internal val orientation: Int
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var onItemTouchListener: OnItemTouchListener
    private lateinit var adapterProxy: AdapterProxy

    private var viewType = 0
        get() {
            val type = field
            field++
            return type
        }

    private var adapterViewType = 100000
        get() {
            val type = field
            // 通过100000的间距，防止两个AdapterView itemType相同
            // 但是如果AdapterView设置的adapter数量超过100000，
            // 那么将会出现问题，通常不会有如此大量的数据
            field += 100000
            return type
        }

    private var shareAdapterViewType = 10000000
        get() {
            val type = field
            field += 100000
            return type
        }

    private val shareLazyViewTypeMap: MutableMap<String, Int> = mutableMapOf()

    // 记录xml布局中view的顺序
    private var xmlChildren: MutableList<View> = mutableListOf()

    /**
     * 当布局自身滑动到边界，不可再滑动时，是否将滑动事件传递给父布局
     */
    var dispatchTouchToParentOnNotScroll: Boolean = false

    init {
        super.setOrientation(orientation)
        if (attrs != null) {
            var typedArray: TypedArray? = null
            try {
                typedArray =
                    context.obtainStyledAttributes(attrs, R.styleable.CombinationLayout, defStyleAttr, 0)
                dispatchTouchToParentOnNotScroll =
                    typedArray.getBoolean(R.styleable.CombinationLayout_dispatchTouchToParentOnNoScroll, false)
            } finally {
                typedArray?.recycle()
            }
        }
    }

    @Deprecated("call method does not perform any operation")
    override fun setOrientation(orientation: Int) {

    }

    /**
     * 查找[CombinationLayout]布局中的View，由于[CombinationLayout]对布局进行重新组合，因此使用[findViewById]无法查找xml中对应的[View]
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : View> findViewById2(@IdRes id: Int): T? {
        if (this::adapterProxy.isInitialized) {
            for (view in xmlChildren) {
                if (view.id == id) {
                    return view as T
                }

                if (view is CombinationLayout) {
                    return view.findViewById2(id) ?: continue
                }

                if (view is GroupPlaceholder) {
                    return view.findViewById2(id) ?: continue
                }

                if (view is ViewGroup) {
                    return view.findViewById(id) ?: continue
                }
            }
        }
        return null
    }

    /**
     * 查找[CombinationLayout]布局中的View，由于[CombinationLayout]对布局进行重新组合，因此使用[getChildAt]无法查找xml中对应的[View]
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : View> getChildAt2(index: Int): T? {
        if (this::adapterProxy.isInitialized) {
            return xmlChildren[index] as T?
        }
        return null
    }

    override fun addView(child: View, index: Int) {
        if (this::adapterProxy.isInitialized) {
            if (index != -1 && index > xmlChildren.size) {
                return
            }

            val viewData = viewToViewData(child)
            if (index == -1 || index == xmlChildren.size) {
                xmlChildren.add(child)
                adapterProxy.addView(viewData, -1)
            } else {
                // 新数据插入位置的原View
                val insertView = xmlChildren[index]
                xmlChildren.add(index, child)
                val insertIndex = adapterProxy.viewToViewDataListIndex(insertView)
                adapterProxy.addView(viewData, insertIndex)
            }
        } else {
            super.addView(child, index)
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (this::adapterProxy.isInitialized) {
            val lp = if (checkLayoutParams(params)) params else generateLayoutParams(params)
            child.layoutParams = lp
            addView(child, index)
        } else {
            super.addView(child, index, params)
        }
    }

    override fun addView(child: View, width: Int, height: Int) {
        if (this::adapterProxy.isInitialized) {
            val lp = generateDefaultLayoutParams()
            lp.width = width
            lp.height = height
            child.layoutParams = lp
            addView(child)
        } else {
            super.addView(child, width, height)
        }
    }

    override fun removeAllViews() {
        if (this::adapterProxy.isInitialized) {
            adapterProxy.removeAllViews(false)
        } else {
            super.removeAllViews()
        }
    }

    override fun removeView(view: View?) {
        if (this::adapterProxy.isInitialized) {
            val index = xmlChildren.indexOf(view)
            if (index == -1) {
                return
            }

            removeView(index, 1, false)
        } else {
            super.removeView(view)
        }
    }

    override fun removeViewAt(index: Int) {
        if (this::adapterProxy.isInitialized) {
            removeView(index, 1, false)
        } else {
            super.removeViewAt(index)
        }
    }

    override fun removeViews(start: Int, count: Int) {
        if (!this::adapterProxy.isInitialized) {
            super.removeViews(start, count)
            return
        }

        removeView(start, count, false)
    }

    override fun removeAllViewsInLayout() {
        if (this::adapterProxy.isInitialized) {
            adapterProxy.removeAllViews(true)
        } else {
            super.removeAllViewsInLayout()
        }
    }

    override fun removeViewInLayout(view: View?) {
        if (this::adapterProxy.isInitialized) {
            val index = xmlChildren.indexOf(view)
            if (index == -1) {
                return
            }

            removeView(index, 1, true)
        } else {
            super.removeViewInLayout(view)
        }
    }

    override fun removeViewsInLayout(start: Int, count: Int) {
        if (!this::adapterProxy.isInitialized) {
            super.removeViewsInLayout(start, count)
            return
        }

        removeView(start, count, true)
    }

    private fun removeView(start: Int, count: Int, preventRequestLayout: Boolean) {
        if (start < 0 || count < 0 || start + count > xmlChildren.size) {
            return
        }

        val firstChild = xmlChildren[start]
        val firstIndex = if (firstChild !is GroupPlaceholder) {
            adapterProxy.viewToViewDataListIndex(firstChild)
        } else {
            getGroupPlaceholderStartIndex(firstChild)
        }

        val endIndex = if (count == 1) {
            if (firstChild !is GroupPlaceholder) {
                firstIndex + 1
            } else {
                getGroupPlaceholderEndIndex(firstChild) + 1
            }
        } else {
            val endChild = xmlChildren[start + count - 1]
            if (endChild !is GroupPlaceholder) {
                adapterProxy.viewToViewDataListIndex(endChild) + 1
            } else {
                getGroupPlaceholderEndIndex(endChild) + 1
            }
        }

        if (firstIndex == -1 || endIndex - firstIndex <= 0) {
            return
        }

        adapterProxy.removeViewAt(firstIndex, endIndex - firstIndex, preventRequestLayout)
        for (index in start until start + count) {
            xmlChildren.removeAt(start)
        }
    }

    @Deprecated("CombinationLayout does not support scrolling to an absolute position.",
        ReplaceWith("scrollToPosition"))
    override fun scrollTo(x: Int, y: Int) {

    }

    /**
     * 滑动到[position]对应View所在位置，如果有足够空间，则View将置顶显示
     */
    fun scrollToPosition(position: Int) {
        scrollTo(position, false, 0)
    }

    /**
     * 滑动到[position]对应View所在位置，如果有足够空间，则View将置顶显示。
     * [offset]用于置顶距离顶部的距离
     */
    fun scrollToPositionWithOffset(position: Int, offset: Int) {
        scrollTo(position, false, offset)
    }

    /**
     * 顺滑的滑动到[position]对应View所在位置，view首次进入屏幕即停止，不置顶显示
     */
    fun smoothScrollToPosition(position: Int) {
        scrollTo(position, true, 0)
    }

    private fun scrollTo(position: Int, smooth: Boolean, offset: Int) {
        if (position < 0 || position >= xmlChildren.size) {
            return
        }

        val child = xmlChildren[position]
        val pos = if (child is GroupPlaceholder) {
            getGroupPlaceholderAdapterStartIndex(child)
        } else {
            val index = adapterProxy.viewToViewDataListIndex(child)
            if (index == -1) {
                -1
            } else {
                val viewData = adapterProxy.viewDataList[index]
                adapterProxy.viewDataToAdapterStartIndex(viewData)
            }
        }

        if (pos == -1) {
            return
        }

        when {
            smooth -> recyclerView.smoothScrollToPosition(pos)

            else -> (recyclerView.layoutManager as LinearLayoutManager).apply {
                scrollToPositionWithOffset(pos, offset)
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        viewMap()
    }

    /**
     * 将XML布局转化为实际显示内容
     */
    private fun viewMap() {
        if (isInEditMode) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child is AdapterView) {
                    removeView(child)
                    addView(child.createPreviewView(orientation), i)
                }
            }
            return
        }

        val children = mutableListOf<ViewData<*>>()
        xmlChildren.clear()
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            xmlChildren.add(child)
            if (child is GroupPlaceholder) {
                parseGroupPlaceholder(child, children)
                continue
            }

            children.add(viewToViewData(child))
        }

        // 最终以RecyclerView展示内容
        recyclerView = RecyclerView(context)
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        recyclerView.layoutParams = params
        recyclerView.layoutManager = LinearLayoutManager(context, orientation, false)
        removeAllViews()
        addView(recyclerView)

        recyclerView.itemAnimator = null
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER

        adapterProxy = AdapterProxy(children)
        recyclerView.adapter = adapterProxy
        onItemTouchListener = OnItemTouchListener()
        recyclerView.addOnItemTouchListener(onItemTouchListener)
    }

    /**
     * 解析[GroupPlaceholder]对象，[parent]用于存储所有[GroupPlaceholder]子节点提升后展开状态[ViewData]数据
     */
    private fun parseGroupPlaceholder(group: GroupPlaceholder, parent: MutableList<ViewData<*>>) {
        group.viewDataList.clear()
        group.addViewListener = Listener@{ view, index ->
            // 当前View需要插入位置的原数据
            val insertPosData = if (index == -1) {
                viewDataList[viewDataList.size - 1]
            } else {
                viewDataList[index]
            }

            val listIndex = if (insertPosData is GroupPlaceholderViewData) {
                if (index == -1) {
                    getGroupPlaceholderEndIndex(insertPosData.view)
                } else {
                    getGroupPlaceholderStartIndex(insertPosData.view)
                }
            } else {
                adapterProxy.viewDataList.indexOf(insertPosData)
            }

            if (listIndex == -1) {
                return@Listener
            }

            val viewData = viewToViewData(view)
            if (index == -1) {
                viewDataList.add(viewData)
                val pos = if (listIndex + 1 >= adapterProxy.viewDataList.size) -1 else listIndex + 1
                adapterProxy.addView(viewData, pos)
            } else {
                viewDataList.add(index, viewData)
                adapterProxy.addView(viewData, listIndex)
            }
        }

        group.removeViewListener = Listener@{ start, count, preventRequestLayout ->
            if (viewDataList.isEmpty()) {
                return@Listener
            }

            val firstChild = children[start]
            val firstIndex = if (firstChild !is GroupPlaceholder) {
                adapterProxy.viewDataList.indexOf(viewDataList[start])
            } else {
                getGroupPlaceholderStartIndex(firstChild)
            }

            val endIndex = if (count == 1) {
                if (firstChild !is GroupPlaceholder) {
                    firstIndex + 1
                } else {
                    getGroupPlaceholderEndIndex(firstChild) + 1
                }
            } else {
                val endChild = children[start + count - 1]
                if (endChild !is GroupPlaceholder) {
                    adapterProxy.viewDataList.indexOf(viewDataList[start + count - 1]) + 1
                } else {
                    getGroupPlaceholderEndIndex(endChild) + 1
                }
            }

            if (firstIndex == -1 || endIndex - firstIndex <= 0) {
                return@Listener
            }

            adapterProxy.removeViewAt(firstIndex, endIndex - firstIndex, preventRequestLayout)
            // 清除groupPlaceholderList中已移除View的ViewData数据
            for (index in start until start + count) {
                viewDataList.removeAt(start)
            }
        }

        group.scrollListener = Listener@{ position, smooth, offset ->
            if (position < 0 || position >= group.viewDataList.size) {
                return@Listener
            }

            val viewData = group.viewDataList[position]
            val pos = if (viewData !is GroupPlaceholderViewData) {
                adapterProxy.viewDataToAdapterStartIndex(viewData)
            } else {
                getGroupPlaceholderAdapterStartIndex(viewData.view)
            }

            if (pos == -1) {
                return@Listener
            }

            when {
                smooth -> recyclerView.smoothScrollToPosition(pos)

                else -> (recyclerView.layoutManager as LinearLayoutManager).apply {
                    scrollToPositionWithOffset(pos, offset)
                }
            }
        }

        group.children.forEach {
            val viewData = viewToViewData(it)
            group.viewDataList.add(viewData)
            if (it !is GroupPlaceholder) {
                parent.add(viewData)
            } else {
                parseGroupPlaceholder(it, parent)
            }
        }
    }

    /**
     * 获取[GroupPlaceholder]在[AdapterProxy.viewDataList]中的起始位置
     */
    private tailrec fun getGroupPlaceholderStartIndex(group: GroupPlaceholder): Int {
        val list = group.viewDataList
        if (list.isEmpty()) {
            return -1
        }

        val child = group.children[0]
        if (child !is GroupPlaceholder) {
            return adapterProxy.viewDataList.indexOf(list[0])
        }
        return getGroupPlaceholderStartIndex(child)
    }

    /**
     * 获取[GroupPlaceholder]在[AdapterProxy]中的起始位置
     */
    private tailrec fun getGroupPlaceholderAdapterStartIndex(group: GroupPlaceholder): Int {
        val list = group.viewDataList
        if (list.isEmpty()) {
            return -1
        }

        val child = group.children[0]
        if (child !is GroupPlaceholder) {
            return adapterProxy.viewDataToAdapterStartIndex(list[0])
        }
        return getGroupPlaceholderAdapterStartIndex(child)
    }

    /**
     * 获取[GroupPlaceholder]在[AdapterProxy.viewDataList]中的结束位置
     */
    private tailrec fun getGroupPlaceholderEndIndex(group: GroupPlaceholder): Int {
        val list = group.viewDataList
        if (list.isEmpty()) {
            return -1
        }

        val child = group.children[group.children.size - 1]
        if (child !is GroupPlaceholder) {
            return adapterProxy.viewDataList.indexOf(list[list.size - 1])
        }
        return getGroupPlaceholderEndIndex(child)
    }

    /**
     * 将View转化为对应的[ViewData]数据
     */
    private fun viewToViewData(child: View): ViewData<*> {
        if (child is GroupPlaceholder) {
            return GroupPlaceholderViewData(context, child)
        }

        if (child !is AdapterView && child !is LazyColumn && child !is LazyRow) {
            return NormalViewData(context, viewType, child)
        }

        return when (child) {
            is AdapterView -> {
                // 记录列表数据
                child.recyclerViewLayoutManager.orientation = orientation
                val viewData = AdapterViewData(
                    context,
                    if (child.isolateViewTypes) adapterViewType else {
                        if (!shareLazyViewTypeMap.containsKey(child.typeFlag)) {
                            shareLazyViewTypeMap[child.typeFlag] = shareAdapterViewType
                        }
                        shareLazyViewTypeMap[child.typeFlag]!!
                    }, child
                )

                child.scrollListener = { position, smooth, offset ->
                    val pos = adapterProxy.viewDataToAdapterStartIndex(viewData) + position

                    when {
                        smooth -> recyclerView.smoothScrollToPosition(pos)

                        else -> (recyclerView.layoutManager as LinearLayoutManager).apply {
                            scrollToPositionWithOffset(pos, offset)
                        }
                    }
                }

                viewData
            }

            is LazyColumn -> LazyColumnData(context, adapterViewType, child)

            else -> LazyRowData(context, adapterViewType, child as LazyRow)
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun isAdapterViewData(viewData: ViewData<*>?): Boolean {
        contract {
            returns(true) implies (viewData is AdapterViewData)
        }
        return viewData is AdapterViewData
    }

    /**
     * 对[AdapterView.getAdapter]生成的ViewHolder包装器，主要应用[AdapterView]设置的相关布局参数
     */
    private class ViewHolderWrapper(
        view: View,
        val viewHolder: RecyclerView.ViewHolder,
    ) : RecyclerView.ViewHolder(view) {
        var viewData: ViewData<AdapterView>? = null
    }

    /**
     * 实现[CombinationLayout] View的重新组合
     */
    private inner class AdapterProxy(var viewDataList: MutableList<ViewData<*>>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var itemCount2: Int = 0
        private var mAttachedToRecyclerView = false

        init {
            viewDataList.forEach {
                itemCount2 += it.getViewCount()
                if (isAdapterViewData(it)) {
                    it.registerAdapterDataObserver(AdapterViewAdapterDataObserver(it))
                }
            }
        }

        private fun addViewData(viewData: List<ViewData<*>>, index: Int = -1) {
            viewData.forEach {
                if (isAdapterViewData(it)) {
                    it.registerAdapterDataObserver(AdapterViewAdapterDataObserver(it))
                    if (mAttachedToRecyclerView && this@CombinationLayout::recyclerView.isInitialized) {
                        it.view.onAttachedToRecyclerView(recyclerView)
                    }
                }
            }

            if (index == -1) {
                viewDataList.addAll(viewData)
            } else {
                viewDataList.addAll(index, viewData)
            }
        }

        private fun removeViewData(viewData: ViewData<*>) {
            if (isAdapterViewData(viewData)) {
                viewData.unRegisterAdapterDataObserver()
                viewData.view.scrollListener = null
            }
            viewDataList.remove(viewData)
        }

        override fun getItemViewType(position: Int): Int {
            val (viewDataIndex, adapterIndex) = adapterIndexToViewDataListIndex(position)
            return viewDataList[viewDataIndex].getViewType(adapterIndex)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val filter = viewDataList.filter { it.hasViewType(viewType) }
            checkCouldCreateViewHolder(filter)
            val viewData = filter[0]
            val viewHolder: RecyclerView.ViewHolder = viewData.createViewHolder(parent, viewType)
            if (isAdapterViewData(viewData)) {
                val frameLayout = FrameLayout(parent.context)
                val layoutParams = viewData.view.layoutParams
                val params = if (orientation == VERTICAL) {
                    MarginLayoutParams(layoutParams.width, LayoutParams.WRAP_CONTENT)
                } else {
                    MarginLayoutParams(LayoutParams.WRAP_CONTENT, layoutParams.height)
                }
                frameLayout.layoutParams = params
                frameLayout.addView(viewHolder.itemView)
                return ViewHolderWrapper(frameLayout, viewHolder)
            }
            return viewHolder
        }

        /**
         * 检查是否有满足条件的创建ViewHolder的对应[ViewData]对象
         */
        private fun checkCouldCreateViewHolder(viewDataList: List<ViewData<*>>) {
            if (viewDataList.size > 1) {
                var isShare = true
                var typeFlag: String? = null
                for (data in viewDataList) {
                    if (data !is AdapterViewData) {
                        isShare = false
                        break
                    } else if (data.view.isolateViewTypes) {
                        isShare = false
                        break
                    } else {
                        if (typeFlag == null) {
                            typeFlag = data.view.typeFlag
                        } else if (typeFlag != data.view.typeFlag) {
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
                @Suppress("UNCHECKED_CAST")
                holder.viewData = viewData as ViewData<AdapterView>
                applyAdapterViewLayoutParams(holder, adapterIndex, viewData as AdapterViewData)
                setOwnerRecyclerView(holder.viewHolder, viewData.view.recyclerView)
                // 清除BindingAdapter，调用viewData.bindViewHolder时会重新给position赋值
                setBindingAdapter(holder.viewHolder, null)
                viewData.bindViewHolder(holder.viewHolder, adapterIndex)
            } else {
                viewData.bindViewHolder(holder, adapterIndex)
            }
        }

        private fun setOwnerRecyclerView(viewHolder: RecyclerView.ViewHolder, recyclerView: RecyclerView) {
            val declaredField = RecyclerView.ViewHolder::class.java.getDeclaredField("mOwnerRecyclerView")
            declaredField.isAccessible = true
            declaredField.set(viewHolder, recyclerView)
        }

        private fun setBindingAdapter(viewHolder: RecyclerView.ViewHolder,
                                      adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>?) {
            val declaredField = RecyclerView.ViewHolder::class.java.getDeclaredField("mBindingAdapter")
            declaredField.isAccessible = true
            declaredField.set(viewHolder, adapter)
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

        override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            if (holder is ViewHolderWrapper) {
                setPayloads(holder.viewHolder, payloads)
            }
            onBindViewHolder(holder, position)
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

            val oldItemType = holder.itemView.getTag(R.id.lazy_column_wrapper_item_type) as Int?
            val flag: Long? = holder.itemView.getTag(R.id.lazy_column_layout_update_flag) as Long?
            val newFlag: Long =
                viewData.view.getTag(R.id.lazy_column_layout_update_flag) as Long + viewData.view.hashCode()
            if (flag != null && flag == newFlag && oldItemType != null && oldItemType == itemType) {
                // 所在LazyColumn父对应布局参数未改变，且复用的item viewType一致
                return
            }

            holder.itemView.setTag(R.id.lazy_column_layout_update_flag, newFlag)
            holder.itemView.setTag(R.id.lazy_column_wrapper_item_type, itemType)
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
            if (orientation == HORIZONTAL) {
                layoutParams.width = LayoutParams.WRAP_CONTENT
                layoutParams.height = viewData.view.layoutParams.height
            } else {
                layoutParams.width = viewData.view.layoutParams.width
                layoutParams.height = LayoutParams.WRAP_CONTENT
            }

            if (layoutParams is MarginLayoutParams) {
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

        override fun getItemCount(): Int = itemCount2

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            mAttachedToRecyclerView = true
            viewDataList.forEach {
                if (isAdapterViewData(it)) {
                    it.view.onAttachedToRecyclerView(recyclerView)
                }
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            mAttachedToRecyclerView = false
            viewDataList.forEach {
                if (isAdapterViewData(it)) {
                    it.view.onDetachedFromRecyclerView(recyclerView)
                }
            }
        }

        override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
            super.onViewAttachedToWindow(holder)
            if (holder is ViewHolderWrapper) {
                val viewData = holder.viewData
                if (isAdapterViewData(viewData)) {
                    viewData.view.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()
                        ?.onViewAttachedToWindow(holder.viewHolder)
                }
            }
        }

        override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
            super.onViewDetachedFromWindow(holder)
            if (holder is ViewHolderWrapper) {
                val viewData = holder.viewData
                if (isAdapterViewData(viewData)) {
                    viewData.view.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()
                        ?.onViewDetachedFromWindow(holder.viewHolder)
                }
            }
        }

        override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
            if (holder is ViewHolderWrapper) {
                val viewData = holder.viewData
                if (isAdapterViewData(viewData)) {
                    return viewData.view.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()
                        ?.onFailedToRecycleView(holder.viewHolder)
                        ?: super.onFailedToRecycleView(holder)
                }
            }

            return super.onFailedToRecycleView(holder)
        }

        override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
            super.onViewRecycled(holder)
            if (holder is ViewHolderWrapper) {
                val viewData = holder.viewData
                if (isAdapterViewData(viewData)) {
                    viewData.view.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()
                        ?.onViewRecycled(holder.viewHolder)
                }
            }
        }

        private fun dataCountChange() {
            itemCount2 = 0
            viewDataList.forEach {
                itemCount2 += it.getViewCount()
            }
        }

        /**
         * 新增View,插入到[viewDataList]中的[index]下标处，如果插入的View非[GroupPlaceholder],
         * 则返回创建的[ViewData]
         */
        fun addView(viewData: ViewData<*>, index: Int): ViewData<*>? {
            if (index != -1 && index > viewDataList.size) {
                return null
            }

            val list = mutableListOf<ViewData<*>>()
            if (viewData is GroupPlaceholderViewData) {
                parseGroupPlaceholder(viewData.view, list)
            } else {
                list.add(viewData)
            }

            val dataCount = viewData.getViewCount()
            if (dataCount == 0) {
                return null
            }

            if (index == -1 || index == viewDataList.size) {
                addViewData(list)
                val preCount = itemCount2
                itemCount2 += dataCount
                notifyItemRangeInserted(preCount, dataCount)
            } else {
                addViewData(list, index)
                itemCount2 += dataCount
                notifyItemRangeInserted(
                    viewDataToAdapterStartIndex(list[0]),
                    dataCount
                )
            }
            return viewData
        }

        @SuppressLint("NotifyDataSetChanged")
        fun removeAllViews(preventRequestLayout: Boolean) {
            viewDataList.clear()
            if (!preventRequestLayout) {
                notifyDataSetChanged()
            }
        }

        fun removeViewAt(start: Int, count: Int, preventRequestLayout: Boolean) {
            val viewData = viewDataList[start]
            val startPos = viewDataToAdapterStartIndex(viewData)
            var dataCount = 0
            for (index in start until start + count) {
                val data = viewDataList[index]
                dataCount += data.getViewCount()
            }

            for (index in start until start + count) {
                val data = viewDataList[start]
                removeViewData(data)
            }

            if (!preventRequestLayout) {
                notifyItemRangeRemoved(startPos, dataCount)
                itemCount2 -= dataCount
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
         * 查找布局文件中view对象在[viewDataList]列表中对应数据下标以及数据在[ViewData]中真实的下标位置
         */
        fun viewToViewDataListIndex(view: View): Int {
            for (i in viewDataList.indices) {
                val data = viewDataList[i]
                if (data.view == view) {
                    return i
                }
            }

            return -1
        }

        /**
         * 获取指定[viewData]在[AdapterProxy]的真实起始位置,未找到对应数据，则返回-1
         */
        fun viewDataToAdapterStartIndex(viewData: ViewData<*>): Int {
            var index = 0
            for (data in viewDataList) {
                if (data == viewData) {
                    return index
                }
                index += data.getViewCount()
            }

            return -1
        }

        /**
         * [AdapterView] Adapter数据变更监听
         */
        inner class AdapterViewAdapterDataObserver(private val adapterViewData: AdapterViewData) :
            RecyclerView.AdapterDataObserver() {

            override fun onChanged() {
                val actualStartPos = viewDataToAdapterStartIndex(adapterViewData)
                if (actualStartPos == -1) {
                    return
                }

                val preCount = itemCount2
                dataCountChange()
                val newCount = itemCount2
                val viewDataCount = adapterViewData.getViewCount()

                adapterViewData.view.resetChange()
                if (preCount == newCount) {
                    // 数量无变化
                    if (viewDataCount > 0) {
                        notifyItemRangeChanged(actualStartPos, viewDataCount)
                    }
                } else if (preCount > newCount) {
                    // 说明移除了部分数据
                    if (viewDataCount > 0) {
                        notifyItemRangeChanged(actualStartPos, viewDataCount)
                    }

                    notifyItemRangeRemoved(actualStartPos + viewDataCount,
                        preCount - newCount)
                } else {
                    // 说明新增了部分数据
                    val changeCount = viewDataCount - (newCount - preCount)
                    if (changeCount > 0) {
                        notifyItemRangeChanged(actualStartPos, changeCount)
                    }

                    notifyItemRangeInserted(actualStartPos + changeCount,
                        newCount - preCount)
                }
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                val actualStartPos = viewDataToAdapterStartIndex(adapterViewData)
                if (actualStartPos == -1) {
                    return
                }

                notifyItemRangeChanged(actualStartPos + positionStart, itemCount)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                val actualStartPos = viewDataToAdapterStartIndex(adapterViewData)
                if (actualStartPos == -1) {
                    return
                }

                notifyItemRangeChanged(actualStartPos + positionStart, itemCount, payload)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                val actualStartPos = viewDataToAdapterStartIndex(adapterViewData)
                if (actualStartPos == -1) {
                    return
                }

                adapterViewData.view.resetChange()
                itemCount2 += itemCount
                notifyItemRangeInserted(actualStartPos + positionStart, itemCount)
                // 插入之后的总数量
                val allItemCount = adapterViewData.view.getAdapter<RecyclerView.Adapter<*>>()?.itemCount
                    ?: return
                // 插入之后的数据下标都发生变更
                val changeCount = allItemCount - (positionStart + itemCount)
                if (changeCount > 0) {
                    notifyItemChanged(actualStartPos + positionStart + itemCount, changeCount)
                }
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                val actualStartPos = viewDataToAdapterStartIndex(adapterViewData)
                if (actualStartPos == -1) {
                    return
                }

                adapterViewData.view.resetChange()
                itemCount2 -= itemCount
                notifyItemRangeRemoved(actualStartPos + positionStart, itemCount)
                // 移除之后的总数量
                val allItemCount = adapterViewData.view.getAdapter<RecyclerView.Adapter<*>>()?.itemCount
                    ?: return
                // 移除数据之后的数据下标都发生变更
                if (positionStart < allItemCount + itemCount - 1
                    && positionStart + itemCount < allItemCount + itemCount) {
                    notifyItemRangeChanged(actualStartPos + positionStart, allItemCount - positionStart)
                }
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                require(itemCount == 1) { "Moving more than 1 item is not supported yet" }

                val actualStartPos = viewDataToAdapterStartIndex(adapterViewData)
                if (actualStartPos == -1) {
                    return
                }

                adapterViewData.view.resetChange()
                notifyItemMoved(actualStartPos + fromPosition, actualStartPos + toPosition)
                // 交换后，两个数据的下标发生变更
                notifyItemChanged(actualStartPos + fromPosition)
                notifyItemChanged(actualStartPos + toPosition)
            }
        }
    }

    private inner class OnItemTouchListener : RecyclerView.OnItemTouchListener {

        var oldX = 0f
        var oldY = 0f
        var child: View? = null

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    oldX = e.x
                    oldY = e.y
                    child = rv.findChildViewUnder(e.x, e.y)
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = e.x - oldX
                    val dy = e.y - oldY
                    oldX = e.x
                    oldY = e.y

                    if (child is CombinationLayout) {
                        (child as CombinationLayout).apply {
                            if (layoutParams?.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                                return false
                            }

                            if (!this::recyclerView.isInitialized) {
                                return false
                            }

                            if (abs(dx) > abs(dy)) {
                                if (orientation == HORIZONTAL) {
                                    if (recyclerView.canScrollHorizontally(if (dx > 0) -1 else 1)) {
                                        requestDisallowInterceptTouchEvent(true)
                                    } else {
                                        requestDisallowInterceptTouchEvent(!dispatchTouchToParentOnNotScroll)
                                    }
                                }
                            } else if (abs(dx) < abs(dy)) {
                                if (orientation == VERTICAL) {
                                    if (recyclerView.canScrollVertically(if (dy > 0) -1 else 1)) {
                                        requestDisallowInterceptTouchEvent(true)
                                    } else {
                                        requestDisallowInterceptTouchEvent(!dispatchTouchToParentOnNotScroll)
                                    }
                                }
                            }
                        }
                    } else if (abs(dx) > abs(dy)) {
                        if (orientation == HORIZONTAL) {
                            if (dispatchTouchToParentOnNotScroll &&
                                !recyclerView.canScrollHorizontally(if (dx > 0) -1 else 1)) {
                                requestDisallowInterceptTouchEvent(false)
                            }
                        }
                    } else if (abs(dx) < abs(dy)) {
                        if (orientation == VERTICAL) {
                            if (dispatchTouchToParentOnNotScroll &&
                                !recyclerView.canScrollVertically(if (dy > 0) -1 else 1)) {
                                requestDisallowInterceptTouchEvent(false)
                            }
                        }
                    }
                }
            }

            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

        }
    }
}

/**
 * 滑动监听
 */
internal typealias ScrollListener = (position: Int, smooth: Boolean, offset: Int) -> Unit
