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
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
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
    private var lazyViewType = 10000
    private var shareLazyViewType = 100000
    private val shareLazyViewTypeMap: MutableMap<String, Int> = mutableMapOf()

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
    fun <T : View> findViewById2(@IdRes id: Int): T? {
        if (this::adapterProxy.isInitialized) {
            for (data in adapterProxy.viewDataList) {
                return data.findViewById(id) ?: continue
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
            return adapterProxy.getChildAt(index) as T?
        }
        return null
    }

    override fun addView(child: View, index: Int) {
        if (this::adapterProxy.isInitialized) {
            adapterProxy.addView(child, index)
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
            adapterProxy.removeView(view, false)
        } else {
            super.removeView(view)
        }
    }

    override fun removeViewAt(index: Int) {
        if (this::adapterProxy.isInitialized) {
            adapterProxy.removeViewAt(index, false)
        } else {
            super.removeViewAt(index)
        }
    }

    override fun removeViews(start: Int, count: Int) {
        if (!this::adapterProxy.isInitialized) {
            super.removeViews(start, count)
            return
        }

        super.removeViews(start, count)
        val end = start + count
        if (start < 0 || count < 0 || end > adapterProxy.getAllViewCount()) {
            throw IndexOutOfBoundsException()
        }

        for (i in start until end) {
            adapterProxy.removeViewAt(i, false)
        }
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
            adapterProxy.removeView(view, true)
        } else {
            super.removeViewInLayout(view)
        }
    }

    override fun removeViewsInLayout(start: Int, count: Int) {
        if (!this::adapterProxy.isInitialized) {
            super.removeViewsInLayout(start, count)
            return
        }

        val end = start + count
        if (start < 0 || count < 0 || end > adapterProxy.getAllViewCount()) {
            throw IndexOutOfBoundsException()
        }

        for (i in start until end) {
            adapterProxy.removeViewAt(i, true)
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

        val children = mutableListOf<ViewData>()
        var viewData = NormalViewData(context, viewType++, orientation, mutableListOf())
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            if (isNonNormalView(child)) {
                if (viewData.children.size > 0) {
                    // 记录列表至之上的数据，作为一个分组
                    children.add(viewData)
                }

                when (child) {
                    is AdapterView -> {
                        // 记录列表数据
                        child.recyclerViewLayoutManager.orientation = orientation
                        val lazyViewData = AdapterViewData(
                            context,
                            if (child.isolateViewTypes) lazyViewType++ else {
                                if (!shareLazyViewTypeMap.containsKey(child.typeFlag)) {
                                    shareLazyViewTypeMap[child.typeFlag] = shareLazyViewType++
                                }
                                shareLazyViewTypeMap[child.typeFlag]!!
                            }, child
                        )
                        children.add(lazyViewData)
                    }

                    is LazyColumn -> {
                        val columnLayoutData = LazyColumnData(context, lazyViewType++, child)
                        children.add(columnLayoutData)
                    }

                    else -> {
                        val rowLayoutData =
                            LazyRowData(context, lazyViewType++, child as LazyRow)
                        children.add(rowLayoutData)
                    }
                }

                // 记录新分组数据
                if (viewData.children.size > 0) {
                    viewData = NormalViewData(context, viewType++, orientation, mutableListOf())
                }
            } else {
                viewData.children.add(child)
                if (i >= childCount - 1) {
                    children.add(viewData)
                }
            }
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

        recyclerView.itemAnimator?.apply {
            addDuration = 0
            changeDuration = 0
            moveDuration = 0
            removeDuration = 0
        }
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER

        adapterProxy = AdapterProxy(children)
        recyclerView.adapter = adapterProxy
        onItemTouchListener = OnItemTouchListener()
        recyclerView.addOnItemTouchListener(onItemTouchListener)
    }

    /**
     * 是否非普通视图
     */
    private fun isNonNormalView(view: View?): Boolean {
        return view is AdapterView || view is LazyColumn || view is LazyRow
    }

    /**
     * 是否非普通视图数据
     */
    @OptIn(ExperimentalContracts::class)
    private fun isNonNormalViewData(viewData: ViewData?): Boolean {
        contract {
            returns(true) implies (viewData is NormalViewData?)
        }
        return viewData is AdapterViewData || viewData is LazyColumnData || viewData is LazyRowData
    }

    /**
     * 是否是普通视图数据
     */
    @OptIn(ExperimentalContracts::class)
    private fun isNormalViewData(viewData: ViewData?): Boolean {
        contract {
            returns(true) implies (viewData is NormalViewData)
        }
        return viewData is NormalViewData
    }

    @OptIn(ExperimentalContracts::class)
    private fun isAdapterViewData(viewData: ViewData?): Boolean {
        contract {
            returns(true) implies (viewData is AdapterViewData)
        }
        return viewData is AdapterViewData
    }

    @OptIn(ExperimentalContracts::class)
    private fun isLazyColumnData(viewData: ViewData?): Boolean {
        contract {
            returns(true) implies (viewData is LazyColumnData)
        }
        return viewData is LazyColumnData
    }

    @OptIn(ExperimentalContracts::class)
    private fun isLazyRowData(viewData: ViewData?): Boolean {
        contract {
            returns(true) implies (viewData is LazyRowData)
        }
        return viewData is LazyRowData
    }

    /**
     * 对[AdapterView.getAdapter]生成的ViewHolder包装器，主要应用[AdapterView]设置的相关布局参数
     */
    private class ViewHolderWrapper(
        view: View,
        val viewHolder: RecyclerView.ViewHolder,
    ) : RecyclerView.ViewHolder(view) {
        var viewData: ViewData? = null
    }

    /**
     * 实现[CombinationLayout] View的重新组合
     */
    private inner class AdapterProxy(var viewDataList: MutableList<ViewData>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var itemCount2: Int = 0

        init {
            viewDataList.forEach {
                itemCount2 += it.getViewCount()
                if (isAdapterViewData(it)) {
                    it.registerAdapterDataObserver(LazyColumnAdapterDataObserver(it))
                }
            }
        }

        private fun addViewData(viewData: ViewData, index: Int = -1) {
            if (isAdapterViewData(viewData)) {
                viewData.registerAdapterDataObserver(LazyColumnAdapterDataObserver(viewData))
            }

            if (index == -1) {
                viewDataList.add(viewData)
            } else {
                viewDataList.add(index, viewData)
            }
        }

        private fun removeViewData(viewData: ViewData) {
            if (isAdapterViewData(viewData)) {
                viewData.unRegisterAdapterDataObserver()
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
                val layoutParams = viewData.adapterView.layoutParams
                val params = MarginLayoutParams(layoutParams.width, layoutParams.height)
                frameLayout.layoutParams = params
                frameLayout.addView(viewHolder.itemView)
                return ViewHolderWrapper(frameLayout, viewHolder)
            }
            return viewHolder
        }

        /**
         * 检查是否有满足条件的创建ViewHolder的对应[ViewData]对象
         */
        private fun checkCouldCreateViewHolder(viewDataList: List<ViewData>) {
            if (viewDataList.size > 1) {
                var isShare = true
                var typeFlag: String? = null
                for (data in viewDataList) {
                    if (data !is AdapterViewData) {
                        isShare = false
                        break
                    } else if (data.adapterView.isolateViewTypes) {
                        isShare = false
                        break
                    } else {
                        if (typeFlag == null) {
                            typeFlag = data.adapterView.typeFlag
                        } else if (typeFlag != data.adapterView.typeFlag) {
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
                holder.viewData = viewData
                applyLazyColumnLayoutParams(holder, adapterIndex, viewData as AdapterViewData)
                setOwnerRecyclerView(holder.viewHolder, viewData.adapterView.recyclerView)
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
        private fun applyLazyColumnLayoutParams(
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
                viewData.adapterView.getTag(R.id.lazy_column_layout_update_flag) as Long + viewData.adapterView.hashCode()
            if (flag != null && flag == newFlag && oldItemType != null && oldItemType == itemType) {
                // 所在LazyColumn父对应布局参数未改变，且复用的item viewType一致
                return
            }

            holder.itemView.setTag(R.id.lazy_column_layout_update_flag, newFlag)
            holder.itemView.setTag(R.id.lazy_column_wrapper_item_type, itemType)
            val paddingStart = viewData.adapterView.paddingStart
            val paddingEnd = viewData.adapterView.paddingEnd
            var paddingTop = 0
            var paddingBottom = 0

            val marginStart = viewData.adapterView.marginStart
            val marginEnd = viewData.adapterView.marginEnd
            var marginTop = 0
            var marginBottom = 0

            if (itemType == 0 || itemType == 2) {
                if (itemType == 0) {
                    marginTop = viewData.adapterView.marginTop
                    paddingTop = viewData.adapterView.paddingTop
                } else {
                    marginBottom = viewData.adapterView.marginBottom
                    paddingBottom = viewData.adapterView.paddingBottom
                }
            }

            holder.itemView.setPaddingRelative(
                paddingStart,
                paddingTop,
                paddingEnd,
                paddingBottom
            )

            val layoutParams = holder.itemView.layoutParams
            layoutParams.width = viewData.adapterView.layoutParams.width
            layoutParams.height = viewData.adapterView.layoutParams.height
            if (layoutParams is MarginLayoutParams) {
                layoutParams.marginStart = marginStart
                layoutParams.marginEnd = marginEnd
                layoutParams.topMargin = marginTop
                layoutParams.bottomMargin = marginBottom
                holder.itemView.layoutParams = layoutParams
            }

            holder.itemView.background = viewData.adapterView.backgroundClone()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.itemView.backgroundTintMode = viewData.adapterView.backgroundTintMode
                holder.itemView.backgroundTintList = viewData.adapterView.backgroundTintList
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                holder.itemView.backgroundTintBlendMode =
                    viewData.adapterView.backgroundTintBlendMode
            }
        }

        override fun getItemCount(): Int = itemCount2

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            viewDataList.forEach {
                if (isAdapterViewData(it)) {
                    it.adapterView.onAttachedToRecyclerView(recyclerView)
                }
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            viewDataList.forEach {
                if (isAdapterViewData(it)) {
                    it.adapterView.onDetachedFromRecyclerView(recyclerView)
                }
            }
        }

        override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
            super.onViewAttachedToWindow(holder)
            if (holder is ViewHolderWrapper) {
                val viewData = holder.viewData
                if (isAdapterViewData(viewData)) {
                    viewData.adapterView.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()
                        ?.onViewAttachedToWindow(holder.viewHolder)
                }
            }
        }

        override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
            super.onViewDetachedFromWindow(holder)
            if (holder is ViewHolderWrapper) {
                val viewData = holder.viewData
                if (isAdapterViewData(viewData)) {
                    viewData.adapterView.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()
                        ?.onViewDetachedFromWindow(holder.viewHolder)
                }
            }
        }

        override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
            if (holder is ViewHolderWrapper) {
                val viewData = holder.viewData
                if (isAdapterViewData(viewData)) {
                    return viewData.adapterView.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()
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
                    viewData.adapterView.getAdapter<RecyclerView.Adapter<RecyclerView.ViewHolder>>()
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

        fun addView(child: View, index: Int) {
            val (viewDataIndex, dataIndex) = viewIndexToViewDataListIndex(index)
            if (isNonNormalView(child)) {
                val viewData = if (viewDataIndex == -1) null else viewDataList[viewDataIndex]
                if (isNormalViewData(viewData)) {
                    // 插入到普通视图区间
                    if (dataIndex == 0) {
                        // 插入到整个普通视图顶端，直接插入
                        addNonNormalView(child, index, viewDataIndex)
                    } else {
                        // 插入到整个普通视图中间，将普通视图根据插入的child位置分为上下两部分
                        // 移除需要放到child之后的视图
                        val removeChildren = mutableListOf<View>()
                        val count = viewData.children.size
                        for (i in dataIndex until count) {
                            val view = viewData.children[dataIndex]
                            viewData.removeView(view, true)
                            removeChildren.add(view)
                        }
                        viewData.requestLayout()
                        viewData.invalidate()

                        addNonNormalView(child, index, viewDataIndex + 1)

                        // 将移除的View重新插入
                        val viewData2 = NormalViewData(
                            context,
                            viewType++,
                            orientation,
                            mutableListOf()
                        ).apply {
                            children.addAll(removeChildren)
                        }
                        addViewData(viewData2, viewDataIndex + 2)
                        itemCount2++
                        notifyItemInserted(viewDataToAdapterStartIndex(viewData2))
                    }
                } else {
                    addNonNormalView(child, index, viewDataIndex)
                }
                return
            }

            if (index == -1 || (viewDataIndex == -1 && index == getAllViewCount())) {
                // 插入到最后面
                val viewData = viewDataList[viewDataList.size - 1]
                if (isNonNormalViewData(viewData)) {
                    val childData =
                        NormalViewData(context, viewType++, orientation, mutableListOf()).apply {
                            children.add(child)
                        }
                    addViewData(childData)
                    itemCount2++
                    notifyItemInserted(itemCount)
                } else {
                    (viewData as NormalViewData).addView(child, -1)
                }
            } else if (viewDataIndex != -1) {
                val viewData = viewDataList[viewDataIndex]
                when {
                    isNormalViewData(viewData) -> {
                        viewData.addView(child, dataIndex)
                    }

                    viewDataIndex == 0 -> {
                        // 插入位置是非NormalView，且前面无其它数据
                        val childData =
                            NormalViewData(
                                context,
                                viewType++,
                                orientation,
                                mutableListOf()
                            ).apply {
                                children.add(child)
                            }
                        addViewData(childData, 0)
                        itemCount2++
                        notifyItemInserted(0)
                    }

                    isNormalViewData(viewDataList[viewDataIndex - 1]) -> {
                        // 插入位置是非NormalView,但是前一个位置是NormalViewData，因此不创建新ViewData，而是插入该NormalViewData尾部
                        (viewDataList[viewDataIndex - 1] as NormalViewData).apply {
                            addView(child, -1)
                        }
                    }

                    else -> {
                        val childData =
                            NormalViewData(
                                context,
                                viewType++,
                                orientation,
                                mutableListOf()
                            ).apply {
                                children.add(child)
                            }
                        addViewData(childData, viewDataIndex)
                        itemCount2++
                        notifyItemInserted(viewDataToAdapterStartIndex(childData))
                    }
                }
            } else {
                throw IndexOutOfBoundsException("length=${getAllViewCount()}; index=$index")
            }
        }

        private fun addNonNormalView(child: View, index: Int, viewDataIndex: Int) {
            val childData = when (child) {
                is AdapterView -> {
                    // 记录列表数据
                    AdapterViewData(
                        context,
                        if (child.isolateViewTypes) lazyViewType++ else {
                            if (!shareLazyViewTypeMap.containsKey(child.typeFlag)) {
                                shareLazyViewTypeMap[child.typeFlag] = shareLazyViewType++
                            }
                            shareLazyViewTypeMap[child.typeFlag]!!
                        }, child
                    )
                }

                is LazyColumn -> LazyColumnData(context, lazyViewType++, child)

                else -> LazyRowData(context, lazyViewType++, child as LazyRow)
            }

            if (index == -1 || (viewDataIndex == -1 && index == getAllViewCount())) {
                addViewData(childData)
                val preCount = itemCount2
                itemCount2 += childData.getViewCount()
                notifyItemRangeInserted(preCount, childData.getViewCount())
            } else if (viewDataIndex != -1) {
                addViewData(childData, viewDataIndex)
                itemCount2 += childData.getViewCount()
                notifyItemRangeInserted(
                    viewDataToAdapterStartIndex(childData),
                    childData.getViewCount()
                )
            } else {
                throw IndexOutOfBoundsException("length=${getAllViewCount()}; index=$index")
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun removeAllViews(preventRequestLayout: Boolean) {
            viewDataList.clear()
            if (!preventRequestLayout) {
                notifyDataSetChanged()
            }
        }

        fun removeView(view: View?, preventRequestLayout: Boolean) {
            view?.apply {
                val index = viewToViewDataListIndex(view)
                if (index == -1) {
                    return
                }

                val viewData = viewDataList[index]
                if (isNonNormalViewData(viewData)) {
                    val startPos = viewDataToAdapterStartIndex(viewData)
                    itemCount2 -= viewData.getViewCount()
                    removeViewData(viewData)
                    if (!preventRequestLayout) {
                        notifyItemRangeRemoved(startPos, viewData.getViewCount())
                    }
                } else {
                    (viewData as NormalViewData).apply { removeView(view, preventRequestLayout) }
                }
            }
        }

        fun removeViewAt(index: Int, preventRequestLayout: Boolean) {
            val (viewDataIndex, dataIndex) = viewIndexToViewDataListIndex(index)
            if (viewDataIndex != -1) {
                val viewData = viewDataList[viewDataIndex]
                if (isNonNormalViewData(viewData)) {
                    val startPos = viewDataToAdapterStartIndex(viewData)
                    itemCount2 -= viewData.getViewCount()
                    removeViewData(viewData)
                    if (!preventRequestLayout) {
                        notifyItemRangeRemoved(startPos, viewData.getViewCount())
                    }
                } else {
                    (viewData as NormalViewData).apply {
                        removeView(
                            dataIndex,
                            preventRequestLayout
                        )
                    }
                }
            }
        }

        fun getChildAt(index: Int): View? {
            val (viewDataIndex, dataIndex) = viewIndexToViewDataListIndex(index)
            if (viewDataIndex == -1) {
                return null
            }

            val viewData = viewDataList[viewDataIndex]
            if (isAdapterViewData(viewData)) {
                return viewData.adapterView
            }

            if (isNormalViewData(viewData)) {
                return viewData.children[dataIndex]
            }

            if (isLazyColumnData(viewData)) {
                return viewData.lazyColumn
            }

            if (isLazyRowData(viewData)) {
                return viewData.lazyRow
            }

            return null
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
            if (position < 0) {
                return Pair(-1, -1)
            }

            var index = 0
            for (i in viewDataList.indices) {
                val data = viewDataList[i]
                val viewCount = if (isNormalViewData(data)) {
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
                } else if (isNormalViewData(data)) {
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

        /**
         * 获取所有View的Count，该数量非Adapter Item数量
         */
        fun getAllViewCount(): Int {
            var count = 0
            for (i in viewDataList.indices) {
                val data = viewDataList[i]
                val viewCount = if (isNormalViewData(data)) {
                    data.children.size
                } else {
                    1
                }
                count += viewCount
            }

            return count
        }

        inner class LazyColumnAdapterDataObserver(private val adapterViewData: AdapterViewData) :
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

                    notifyItemRangeInserted(actualStartPos + adapterViewData.getViewCount(),
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

                itemCount2 += itemCount
                notifyItemRangeInserted(actualStartPos + positionStart, itemCount)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                val actualStartPos = viewDataToAdapterStartIndex(adapterViewData)
                if (actualStartPos == -1) {
                    return
                }

                itemCount2 -= itemCount
                notifyItemRangeRemoved(actualStartPos + positionStart, itemCount)
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                require(itemCount == 1) { "Moving more than 1 item is not supported yet" }

                val actualStartPos = viewDataToAdapterStartIndex(adapterViewData)
                if (actualStartPos == -1) {
                    return
                }

                notifyItemMoved(actualStartPos + fromPosition, actualStartPos + toPosition)
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
