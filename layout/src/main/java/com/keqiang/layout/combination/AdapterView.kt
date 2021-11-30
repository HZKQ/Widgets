package com.keqiang.layout.combination

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.IntRange
import androidx.core.view.isGone
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keqiang.layout.R

/**
 * 适配器View，用于结合[LazyColumn]、[LazyColumn]使用，单独使用相当于普通[View].
 * 结合[LazyColumn]使用时，高度始终都是[ViewGroup.LayoutParams.WRAP_CONTENT].
 * 结合[LazyRow]使用时，宽度始终都是[ViewGroup.LayoutParams.WRAP_CONTENT].
 *
 * @author Created by wanggaowan on 2021/9/15 09:28
 */
class AdapterView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var mItemCount = 1
    private var mAdapterChangeListener: MutableList<AdapterChangeListener> = mutableListOf()
    private var mLayoutChangeListener: LayoutChangeListener? = null
    internal var scrollListener: ScrollListener? = null
    internal val recyclerView: RecyclerView = RecyclerView(context)
    internal val layoutManager = GridLayoutManager(context, 1)
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutResId = NO_ID
    private var mBackgroundResId = NO_ID

    /**
     * 实际运行时，此界面展示内容的父对象
     */
    internal var combinationLayout: CombinationLayout? = null

    /**
     * 是否隔离视图，如果设置为true，那么不同[AdapterView]的adapter视图不相互复用，
     * 如果设置为false，那么多个[AdapterView]的adapter视图可复用，只要[RecyclerView.Adapter.getItemViewType]一致
     */
    var isolateViewTypes: Boolean = true

    /**
     * 用来标识复用相同adapter item布局的多个[AdapterView]
     */
    var typeFlag: String = ""

    /**
     * 通过设置此值实现网格布局和线性布局。1：线性布局，>1:网格布局
     */
    @setparam:IntRange(from = 1)
    var spanCount: Int = 1
        set(value) {
            if (value == field) {
                return
            }

            if (value < 1 && field == 1) {
                return
            }

            field = value
            if (field < 1) {
                field = 1
            }

            combinationLayout?.spanCountChange(this)
        }

    /**
     * 如果[spanCount] 大于1，此值用于实现Item 占用的跨距数
     */
    var spanSizeLookup: GridLayoutManager.SpanSizeLookup = GridLayoutManager.DefaultSpanSizeLookup()
        set(value) {
            if (field == value) {
                return
            }

            field = value
            field.isSpanIndexCacheEnabled = false
            combinationLayout?.spanCountChange(this)
        }

    /**
     * Adapter item是否会超出[AdapterView]尺寸值，一般只在指定Item宽高为固定值时，才会出现此情况。
     * 如果此值设置设置为true，那么将对超出的Item进行裁剪
     */
    var itemOverSize: Boolean = false

    init {
        recyclerView.layoutManager = layoutManager
        recyclerView.visibility = GONE

        if (attrs != null) {
            var typedArray: TypedArray? = null
            try {
                typedArray =
                    context.obtainStyledAttributes(attrs, R.styleable.AdapterView, defStyleAttr, 0)
                mLayoutResId = typedArray.getResourceId(R.styleable.AdapterView_layout, NO_ID)
                mBackgroundResId =
                    typedArray.getResourceId(R.styleable.AdapterView_android_background, NO_ID)
                isolateViewTypes =
                    typedArray.getBoolean(R.styleable.AdapterView_isolateViewTypes, true)
                typeFlag = typedArray.getString(R.styleable.AdapterView_type_flag) ?: ""
                spanCount = typedArray.getInt(R.styleable.AdapterView_spanCount, 1)
                mItemCount = typedArray.getInt(R.styleable.AdapterView_itemCount, spanCount * 2)
            } finally {
                typedArray?.recycle()
            }
        }

        if (isInEditMode) {
            layoutManager.spanCount = spanCount
            val adapter = PreviewAdapter()
            recyclerView.adapter = adapter
            recyclerView.visibility = VISIBLE
            val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            addView(recyclerView, layoutParams)
        }
    }

    override fun invalidate() {
        super.invalidate()
        mLayoutChangeListener?.invoke()
    }

    override fun requestLayout() {
        super.requestLayout()
        mLayoutChangeListener?.invoke()
    }

    override fun setVisibility(visibility: Int) {
        if (visibility == INVISIBLE && this.visibility == VISIBLE) {
            // 此时不会触发requestLayout
            mLayoutChangeListener?.invoke()
        }
        super.setVisibility(visibility)
    }

    override fun setBackgroundColor(color: Int) {
        mBackgroundResId = NO_ID
        val isColorDrawable = background is ColorDrawable
        super.setBackgroundColor(color)
        if (isColorDrawable) {
            // 此时super不会调用invalidate()
            mLayoutChangeListener?.invoke()
        }
    }

    override fun setBackgroundResource(resid: Int) {
        mBackgroundResId = resid
        super.setBackgroundResource(resid)
    }

    override fun setBackground(background: Drawable?) {
        mBackgroundResId = NO_ID
        super.setBackground(background)
    }

    /**
     * 注册[RecyclerView.Adapter]变更监听
     */
    fun registerAdapterChangeListener(listener: AdapterChangeListener?) {
        listener?.apply { mAdapterChangeListener.add(this) }
    }

    /**
     * 移除[RecyclerView.Adapter]变更监听
     */
    fun removeAdapterChangeListener(listener: AdapterChangeListener?) {
        listener?.apply { mAdapterChangeListener.remove(this) }
    }

    /**
     * 注册[AdapterView]布局改变监听
     */
    internal fun registerLayoutChangeListener(listener: LayoutChangeListener?) {
        mLayoutChangeListener = listener
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        if (adapter == mAdapter) {
            return
        }

        val oldAdapter = mAdapter
        mAdapter = adapter
        mAdapterChangeListener.forEach { it.invoke(oldAdapter, mAdapter) }
    }

    override fun getChildAt(index: Int): View? {
        return if (isInEditMode) {
            super.getChildAt(index)
        } else {
            null
        }
    }

    override fun getChildCount(): Int {
        return if (isInEditMode) {
            super.getChildCount()
        } else {
            0
        }
    }

    override fun indexOfChild(child: View?): Int {
        if (isInEditMode) {
            return super.indexOfChild(child)
        }

        return -1
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        if (isInEditMode) {
            super.addView(child, index, params)
        }
    }

    override fun removeAllViews() {
        if (isInEditMode) {
            super.removeAllViews()
        }
    }

    override fun removeView(view: View?) {
        if (isInEditMode) {
            super.removeView(view)
        }
    }

    override fun removeViewAt(index: Int) {
        if (isInEditMode) {
            super.removeViewAt(index)
        }
    }

    override fun removeViews(start: Int, count: Int) {
        if (isInEditMode) {
            super.removeViews(start, count)
        }
    }

    override fun removeAllViewsInLayout() {
        if (isInEditMode) {
            super.removeAllViewsInLayout()
        }
    }

    override fun removeViewInLayout(view: View?) {
        if (isInEditMode) {
            super.removeViewInLayout(view)
        }
    }

    override fun removeViewsInLayout(start: Int, count: Int) {
        if (isInEditMode) {
            super.removeViewsInLayout(start, count)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : RecyclerView.Adapter<*>> getAdapter(): T? {
        return mAdapter as T?
    }

    /**
     * 滑动到[position]对应View所在位置，如果有足够空间，则View将置顶显示
     */
    fun scrollToPosition(position: Int) {
        if (isGone) {
            return
        }

        if (position < 0 || position >= getViewCount() - 2) {
            return
        }

        scrollListener?.invoke(position + 1, false, 0)
    }

    /**
     * 滑动到[position]对应View所在位置，如果有足够空间，则View将置顶显示。
     * [offset]用于置顶距离顶部的距离
     */
    fun scrollToPositionWithOffset(position: Int, offset: Int) {
        if (isGone) {
            return
        }

        if (position < 0 || position >= getViewCount() - 2) {
            return
        }

        scrollListener?.invoke(position + 1, false, offset)
    }

    /**
     * 顺滑的滑动到[position]对应View所在位置，view首次进入屏幕即停止，不置顶显示
     */
    fun smoothScrollToPosition(position: Int) {
        if (isGone) {
            return
        }

        if (position < 0 || position >= getViewCount() - 2) {
            return
        }

        scrollListener?.invoke(position + 1, true, 0)
    }

    @Deprecated("AdapterView does not support scrolling to an absolute position.",
        ReplaceWith("scrollToPosition"))
    override fun scrollTo(x: Int, y: Int) {

    }

    /**
     * 参数[recyclerView]为[AdapterView]实际运行时用于展示界面的组件
     */
    internal fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mAdapter?.onAttachedToRecyclerView(this.recyclerView)
    }

    /**
     * 参数[recyclerView]为[AdapterView]实际运行时用于展示界面的组件
     */
    internal fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        mAdapter?.onDetachedFromRecyclerView(this.recyclerView)
    }

    /**
     * 消除RecyclerView未执行的更新，防止获取position时位置不正确
     * 主要目的就是使RecyclerView中AdapterHelper的mPendingUpdates清空
     */
    internal fun resetChange() {
        recyclerView.adapter = mAdapter
    }

    internal fun getViewCount(): Int {
        if (isGone) {
            return 0
        }

        // +2 用于绘制view的padding、margin数据
        return (getAdapter<RecyclerView.Adapter<*>>()?.itemCount ?: 0) + 2
    }

    /**
     * 查找第一个可见Item位置
     */
    fun findFirstVisibleItemPosition(): Int {
        if (getViewCount() <= 2) {
            // 说明未设置Adapter或Adapter数据为0
            return RecyclerView.NO_POSITION
        }

        return findVisibleItemPosition(isFirst = true)
    }

    /**
     * 查找最后一个可见Item位置
     */
    fun findLastVisibleItemPosition(): Int {
        if (getViewCount() <= 2) {
            // 说明未设置Adapter或Adapter数据为0
            return RecyclerView.NO_POSITION
        }

        return findVisibleItemPosition(isFirst = false)
    }

    /**
     * 查找第一个完全展示Item位置，该Item完整展示在界面，无任何部分滑出界面边界
     */
    fun findFirstCompletelyVisibleItemPosition(): Int {
        if (getViewCount() <= 2) {
            // 说明未设置Adapter或Adapter数据为0
            return RecyclerView.NO_POSITION
        }

        return findVisibleItemPosition(isFirst = true, isComplete = true)
    }

    /**
     * 查找最后一个完全展示Item位置，该Item完整展示在界面，无任何部分滑出界面边界
     */
    fun findLastCompletelyVisibleItemPosition(): Int {
        if (getViewCount() <= 2) {
            // 说明未设置Adapter或Adapter数据为0
            return RecyclerView.NO_POSITION
        }

        return findVisibleItemPosition(isFirst = false, isComplete = true)
    }

    /**
     * 查找可见项目位置，是否要查找[isComplete]和[isFirst]状态的View
     */
    private fun findVisibleItemPosition(isFirst: Boolean, isComplete: Boolean = false): Int {
        val firstPos = combinationLayout?.findVisibleItemPosition(this, true, isComplete)
            ?: RecyclerView.NO_POSITION

        if (isFirst && firstPos == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        val lastPos = combinationLayout?.findVisibleItemPosition(this, false, isComplete)
            ?: RecyclerView.NO_POSITION

        if (!isFirst && lastPos == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        // 当AdapterView显示时，顶部和底部各有一个Item用于绘制AdapterView的padding、margin等数据
        val startIndex = 1
        val viewCount = getViewCount()
        val endIndex = viewCount - 2
        if (isFirst) {
            if (startIndex in firstPos..lastPos) {
                return 0
            }

            if (firstPos in startIndex..endIndex) {
                return firstPos - 1
            }
        } else {
            if (endIndex in firstPos..lastPos) {
                return endIndex - 1
            }

            if (lastPos in startIndex until endIndex) {
                return lastPos - startIndex
            }
        }

        return RecyclerView.NO_POSITION
    }

    /**
     * 获取指定[position]位置的View，仅当前位置数据在界面展示时返回，否则返回null
     */
    fun findViewByPosition(position: Int): View? {
        if (getViewCount() <= 2) {
            // 说明未设置Adapter或Adapter数据为0
            return null
        }

        if (position < 0 || position >= getViewCount() - 2) {
            return null
        }

        return combinationLayout?.findViewByPosition(this, position + 1)
    }

    /**
     * 获取指定position位置item所占一行/一列的跨度
     */
    fun getSpanSize(position: Int): Int {
        val spanSize = spanSizeLookup.getSpanSize(position)
        if (spanSize > spanCount) {
            throw IllegalArgumentException("Item at position $position requires $spanSize spans but GridLayoutManager has only $spanCount spans.")
        }
        return spanSize
    }

    /**
     * 如果[spanCount] > 1,获取指定position位置Item在一行/一列中的下标
     */
    fun getSpanIndex(position: Int): Int {
        return spanSizeLookup.getSpanIndex(position, spanCount)
    }

    /**
     * 用于预览的Adapter
     */
    private inner class PreviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(mLayoutResId, parent, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        }

        override fun getItemCount(): Int = if (mLayoutResId == NO_ID) 0 else mItemCount

    }
}

/**
 * 适配器改变监听
 */
typealias AdapterChangeListener = (oldAdapter: RecyclerView.Adapter<*>?, adapter: RecyclerView.Adapter<*>?) -> Unit

/**
 * 布局数据改变监听
 */
typealias LayoutChangeListener = () -> Unit

