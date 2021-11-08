package com.keqiang.layout.combination

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.core.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keqiang.layout.R
import kotlin.collections.set
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.abs

/**
 * 实现[RecyclerView]的效果，但是只需要在xml中配置布局就可以实现多类型Item，多[RecyclerView.Adapter]组合的功能。
 * 由于最终实现采用[RecyclerView],因此实际运行时，[getChildAt]与[getChildCount]返回[RecyclerView]供系统调用，要
 * 通过以上两个方法获取添加到当前对象的View，请使用[getChildAt2]与[getChildren]
 *
 * @author Created by wanggaowan on 2021/9/15 09:19
 */
abstract class CombinationLayout constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    internal val orientation: Int
) : LinearLayout(context, attrs, defStyleAttr) {

    internal lateinit var recyclerView: RecyclerViewInner
    private lateinit var onItemTouchListener: OnItemTouchListener
    private lateinit var adapterProxy: AdapterProxy
    private val scrollListenerList: MutableList<OnScrollListener> = mutableListOf()

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

    private val shareAdapterViewTypeMap: MutableMap<String, Int> = mutableMapOf()

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
     * 此方法在[ViewGroup]表示为hide api，但是覆盖此方法，是可以生效的且没有在高版本发生崩溃，测试版本最高为android11
     */
    @Suppress("UNCHECKED_CAST")
    protected fun <T : View> findViewTraversal(@IdRes id: Int): T? {
        if (id == this.id) {
            return this as T
        }

        if (this::adapterProxy.isInitialized) {
            for (view in xmlChildren) {
                return view.findViewById(id) ?: continue
            }
        }

        return null
    }

    /**
     * 查找[CombinationLayout]布局中的View，由于[CombinationLayout]对布局进行重新组合，因此使用[getChildAt]无法查找xml中对应的[View]。
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : View> getChildAt2(index: Int): T? {
        if (this::adapterProxy.isInitialized) {
            return xmlChildren[index] as T?
        }
        return null
    }

    @Deprecated("此对象无法获取实际位置对应的View数量", ReplaceWith("请使用getChildAt2(Int)"))
    override fun getChildAt(index: Int): View? {
        // 不能对getChildAt进行重写，系统通过getChildCount和getChildAt获取ViewGroup中子节点数量
        // 实际运行时，此View只有一个子节点(RecyclerView),通过xml添加的获取addView添加的View，最终均由
        // RecyclerView呈现
        return super.getChildAt(index)
    }

    @Deprecated("此对象无法获取实际View数量", ReplaceWith("请使用getChildren()获取子节点列表再获取数量"))
    override fun getChildCount(): Int {
        return super.getChildCount()
    }

    /**
     * 获取所有子对象
     */
    fun getChildren(): List<View> {
        return xmlChildren
    }

    override fun indexOfChild(child: View?): Int {
        if (isInEditMode) {
            return super.indexOfChild(child)
        }

        if (child == null) {
            return -1
        }

        return xmlChildren.indexOf(child)
    }

    override fun addView(child: View, index: Int) {
        if (this::adapterProxy.isInitialized) {
            if (index != -1 && index > xmlChildren.size) {
                return
            }

            child.scale()
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
            val view = xmlChildren[start]
            if (view is GroupPlaceholder) {
                view.removeListener(view)
            }
            xmlChildren.removeAt(start)
        }
    }

    @Deprecated("CombinationLayout does not support scrolling to an absolute position.",
        ReplaceWith("scrollToPosition"))
    override fun scrollTo(x: Int, y: Int) {

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setOnTouchListener(l: OnTouchListener?) {
        if (this::recyclerView.isInitialized) {
            recyclerView.setOnTouchListener(l)
        } else {
            super.setOnTouchListener(l)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        if (this::recyclerView.isInitialized) {
            recyclerView.setOnClickListener(l)
        } else {
            super.setOnClickListener(l)
        }
    }

    override fun setLongClickable(longClickable: Boolean) {
        if (this::recyclerView.isInitialized) {
            recyclerView.isLongClickable = longClickable
        } else {
            super.setLongClickable(longClickable)
        }
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        if (this::recyclerView.isInitialized) {
            recyclerView.setOnLongClickListener(l)
        } else {
            super.setOnLongClickListener(l)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun setOnScrollChangeListener(l: OnScrollChangeListener?) {
        if (this::recyclerView.isInitialized) {
            recyclerView.setOnScrollChangeListener(l)
        } else {
            super.setOnScrollChangeListener(l)
        }
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

    fun addOnScrollListener(scrollListener: OnScrollListener) {
        scrollListenerList.add(scrollListener)
    }

    fun removeOnScrollListener(scrollListener: OnScrollListener) {
        scrollListenerList.remove(scrollListener)
    }

    /**
     * 解决焦点冲突时，导致界面滑动。 如：布局中有EditText，当EditText获取焦点时，此布局可能滑动到获取焦点EditText位置，
     * 此时如果不想滑动，可在EditText获取焦点时，调用该方法
     */
    fun requestNotScroll() {
        if (this::recyclerView.isInitialized) {
            recyclerView.requestNotScroll()
        }
    }

    /**
     * 查找可见项目位置，是否要查找[isComplete]和[isFirst]状态的View
     */
    internal fun findVisibleItemPosition(view: View, isFirst: Boolean, isComplete: Boolean = false): Int {
        if (!this::recyclerView.isInitialized) {
            return RecyclerView.NO_POSITION
        }

        val index = adapterProxy.viewToViewDataListIndex(view)
        if (index == -1) {
            return RecyclerView.NO_POSITION
        }

        val viewData = adapterProxy.viewDataList[index]
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val firstPos = if (isComplete) {
            layoutManager.findFirstCompletelyVisibleItemPosition()
        } else {
            layoutManager.findFirstVisibleItemPosition()
        }

        if (isFirst && firstPos == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        val lastPos = if (isComplete) {
            layoutManager.findLastCompletelyVisibleItemPosition()
        } else {
            layoutManager.findLastVisibleItemPosition()
        }

        if (!isFirst && lastPos == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        val startIndex = adapterProxy.viewDataToAdapterStartIndex(viewData)
        val endIndex = startIndex + viewData.getViewCount() - 1
        if (isFirst) {
            if (startIndex in firstPos..lastPos) {
                return 0
            }

            if (firstPos in (startIndex + 1)..endIndex) {
                return viewData.getViewCount() - 1 - (endIndex - firstPos)
            }
        } else {
            if (endIndex in firstPos..lastPos) {
                return viewData.getViewCount() - 1
            }

            if (lastPos in startIndex until endIndex) {
                return lastPos - startIndex
            }
        }

        return RecyclerView.NO_POSITION
    }

    /**
     * 查找指定位置的View
     */
    internal fun findViewByPosition(view: View, position: Int): View? {
        if (!this::recyclerView.isInitialized) {
            return null
        }

        val index = adapterProxy.viewToViewDataListIndex(view)
        if (index == -1) {
            return null
        }

        val viewData = adapterProxy.viewDataList[index]
        val startIndex = adapterProxy.viewDataToAdapterStartIndex(viewData)
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        return layoutManager.findViewByPosition(startIndex + position)
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
            for (i in 0 until super.getChildCount()) {
                val child = super.getChildAt(i)
                if (child is AdapterView) {
                    child.recyclerViewLayoutManager.orientation = orientation
                } else if (child is GroupPlaceholder) {
                    child.setParentOrientation(orientation)
                }
            }
            return
        }

        val children = mutableListOf<ViewData<*>>()
        xmlChildren.clear()
        for (i in 0 until super.getChildCount()) {
            val child = super.getChildAt(i) ?: continue
            xmlChildren.add(child.apply { scale() })
            if (child is GroupPlaceholder) {
                parseGroupPlaceholder(child, children)
                continue
            }

            children.add(viewToViewData(child))
        }

        // 最终以RecyclerView展示内容
        recyclerView = RecyclerViewInner(context)
        recyclerView.setLinearLayoutManager(orientation)
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        recyclerView.layoutParams = params

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                scrollListenerList.forEach {
                    it.onScrollStateChanged(newState)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollListenerList.forEach {
                    it.onScrolled(dx, dy)
                }
            }
        })

        super.removeAllViews()
        super.addView(recyclerView)

        adapterProxy = AdapterProxy(children, recyclerView)
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
            return GroupPlaceholderViewData(context, child, orientation)
        }

        if (child !is AdapterView && child !is LazyColumn && child !is LazyRow) {
            return NormalViewData(context, viewType, child, orientation)
        }

        return when (child) {
            is AdapterView -> {
                // 记录列表数据
                child.combinationLayout = this
                val viewData = AdapterViewData(
                    context,
                    if (child.isolateViewTypes) adapterViewType else {
                        if (!shareAdapterViewTypeMap.containsKey(child.typeFlag)) {
                            shareAdapterViewTypeMap[child.typeFlag] = shareAdapterViewType
                        }
                        shareAdapterViewTypeMap[child.typeFlag]!!
                    }, child, orientation
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

            is LazyColumn -> LazyColumnData(context, adapterViewType, child, orientation)

            else -> LazyRowData(context, adapterViewType, child as LazyRow, orientation)
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
     * 实现[CombinationLayout] View的重新组合
     */
    private inner class AdapterProxy(var viewDataList: MutableList<ViewData<*>>,
                                     val recyclerView: RecyclerView) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var itemCount2: Int = 0
        private var mAttachedToRecyclerView = false

        init {
            viewDataList.forEach {
                itemCount2 += it.getViewCount()
                if (isAdapterViewData(it)) {
                    it.setAdapterChangeListener { _, adapter ->
                        // adapter类型变更
                        if (adapter != null
                            // 隔离AdapterView,不与其它AdapterView共享视图
                            && it.view.isolateViewTypes) {
                            // 变更itemType，防止不同Adapter Item复用
                            it.type = adapterViewType
                        }
                    }

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
                viewData.setAdapterChangeListener(null)
                viewData.view.scrollListener = null
                viewData.view.combinationLayout = null
            }
            viewDataList.remove(viewData)
        }

        override fun getItemViewType(position: Int): Int {
            val (viewDataIndex, adapterIndex) = adapterIndexToViewDataListIndex(position)
            return viewDataList[viewDataIndex].getViewType(adapterIndex)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val filter = viewDataList.filter { it.hasViewType(viewType) }
            if (viewType != AdapterViewData.BG_ITEM_TYPE) {
                checkCouldCreateViewHolder(filter)
            }

            return filter[0].createViewHolder(parent, viewType)
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
            viewData.bindViewHolder(holder, adapterIndex)
        }

        override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            val (viewDataIndex, adapterIndex) = adapterIndexToViewDataListIndex(position)
            val viewData = viewDataList[viewDataIndex]
            viewData.bindViewHolder(holder, adapterIndex, payloads)
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
            if (holder is AdapterViewData.ViewHolderWrapper) {
                holder.viewData?.onViewAttachedToWindow(holder)
            }
        }

        override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
            super.onViewDetachedFromWindow(holder)
            if (holder is AdapterViewData.ViewHolderWrapper) {
                holder.viewData?.onViewDetachedFromWindow(holder)
            }
        }

        override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
            if (holder is AdapterViewData.ViewHolderWrapper) {
                return holder.viewData?.onFailedToRecycleView(holder)
                    ?: super.onFailedToRecycleView(holder)
            }

            return super.onFailedToRecycleView(holder)
        }

        override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
            super.onViewRecycled(holder)
            if (holder is AdapterViewData.ViewHolderWrapper) {
                holder.viewData?.onViewRecycled(holder)
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
        fun adapterIndexToViewDataListIndex(position: Int): Pair<Int, Int> {
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
                val allItemCount = adapterViewData.getViewCount()
                // 插入之后的数据下标都发生变更
                val changeCount = allItemCount - (positionStart + itemCount)
                if (changeCount > 0) {
                    notifyItemRangeChanged(actualStartPos + positionStart + itemCount, changeCount)
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
                val allItemCount = adapterViewData.getViewCount()
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
                notifyItemRangeChanged(actualStartPos + fromPosition)
                notifyItemRangeChanged(actualStartPos + toPosition)
            }

            @SuppressLint("NotifyDataSetChanged")
            private fun notifyItemRangeChanged(positionStart: Int, itemCount: Int = 1, payload: Any? = null) {
                notify {
                    this@AdapterProxy.notifyItemRangeChanged(positionStart, itemCount, payload)
                }

                // val firstPos = layoutManager.findFirstVisibleItemPosition()
                // if (firstPos == RecyclerView.NO_POSITION) {
                //     return
                // }
                //
                // val lastPos = layoutManager.findLastVisibleItemPosition()
                // if (lastPos == RecyclerView.NO_POSITION) {
                //     return
                // }
                //
                // val endIndex = positionStart + itemCount - 1
                // if (positionStart >= firstPos && endIndex <= lastPos) {
                //     this@AdapterProxy.notifyItemRangeChanged(positionStart, itemCount, payload)
                // } else if (firstPos in (positionStart + 1)..endIndex && endIndex <= lastPos) {
                //     val count = endIndex - firstPos + 1
                //     if (count > 0) {
                //         this@AdapterProxy.notifyItemRangeChanged(firstPos, count, payload)
                //     }
                // } else if (lastPos in positionStart until endIndex && positionStart >= firstPos) {
                //     val count = lastPos - positionStart + 1
                //     if (count > 0) {
                //         this@AdapterProxy.notifyItemRangeChanged(positionStart, count, payload)
                //     }
                // } else if (positionStart < firstPos && endIndex > lastPos) {
                //     val count = lastPos - firstPos + 1
                //     if (count > 0) {
                //         this@AdapterProxy.notifyItemRangeChanged(firstPos, count, payload)
                //     }
                // }
            }

            private fun notifyItemRangeInserted(positionStart: Int, itemCount: Int) {
                notify {
                    this@AdapterProxy.notifyItemRangeInserted(positionStart, itemCount)
                }
            }

            private fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int) {
                notify {
                    this@AdapterProxy.notifyItemRangeRemoved(positionStart, itemCount)
                }
            }

            private fun notifyItemMoved(fromPosition: Int, toPosition: Int) {
                notify {
                    this@AdapterProxy.notifyItemMoved(fromPosition, toPosition)
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            private fun notify(func: () -> Unit) {
                if (recyclerView.isComputingLayout || recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
                    recyclerView.post {
                        if (recyclerView.isComputingLayout || recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
                            return@post
                        }

                        notifyDataSetChanged()
                    }
                } else {
                    func.invoke()
                }
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

                    val combinationLayout = getCombinationLayout(child)
                    if (combinationLayout != null) {
                        combinationLayout.apply {
                            if (layoutParams == null || layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
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

        private fun getCombinationLayout(view: View?): CombinationLayout? {
            if (view is CombinationLayout) {
                return view
            }

            if (view is DetectLayoutViewGroup) {
                val child = view.getChildAt(0)
                if (child is CombinationLayout) {
                    return child
                }
            }

            return null
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
