package com.keqiang.layout.combination

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
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
    internal val recyclerViewLayoutManager = LinearLayoutManager(context)
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

    init {
        recyclerView.layoutManager = recyclerViewLayoutManager
        recyclerView.visibility = GONE

        if (attrs != null) {
            var typedArray: TypedArray? = null
            try {
                typedArray =
                    context.obtainStyledAttributes(attrs, R.styleable.AdapterView, defStyleAttr, 0)
                mItemCount = typedArray.getInt(R.styleable.AdapterView_itemCount, 1)
                mLayoutResId = typedArray.getResourceId(R.styleable.AdapterView_layout, NO_ID)
                mBackgroundResId =
                    typedArray.getResourceId(R.styleable.AdapterView_android_background, NO_ID)
                isolateViewTypes =
                    typedArray.getBoolean(R.styleable.AdapterView_isolateViewTypes, true)
                typeFlag = typedArray.getString(R.styleable.AdapterView_type_flag) ?: ""
            } finally {
                typedArray?.recycle()
            }
        }

        if (isInEditMode) {
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
    fun registerLayoutChangeListener(listener: LayoutChangeListener?) {
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
        scrollListener?.invoke(position, false, 0)
    }

    /**
     * 滑动到[position]对应View所在位置，如果有足够空间，则View将置顶显示。
     * [offset]用于置顶距离顶部的距离
     */
    fun scrollToPositionWithOffset(position: Int, offset: Int) {
        scrollListener?.invoke(position, false, offset)
    }

    /**
     * 顺滑的滑动到[position]对应View所在位置，view首次进入屏幕即停止，不置顶显示
     */
    fun smoothScrollToPosition(position: Int) {
        scrollListener?.invoke(position, true, 0)
    }

    @Deprecated("AdapterView does not support scrolling to an absolute position.",
        ReplaceWith("scrollToPosition"))
    override fun scrollTo(x: Int, y: Int) {

    }

    internal fun backgroundClone(): Drawable? {
        return when {
            mBackgroundResId != NO_ID -> ContextCompat.getDrawable(context, mBackgroundResId)

            background is ColorDrawable -> colorDrawableCopy(background as ColorDrawable)

            else -> null
        }
    }

    private fun colorDrawableCopy(colorDrawable: ColorDrawable): ColorDrawable {
        val drawable = ColorDrawable(colorDrawable.color)
        drawable.state = colorDrawable.state
        drawable.alpha = colorDrawable.alpha
        drawable.callback = colorDrawable.callback
        drawable.bounds = colorDrawable.bounds
        drawable.changingConfigurations = drawable.changingConfigurations
        drawable.level = colorDrawable.level
        drawable.setVisible(colorDrawable.isVisible, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            drawable.isAutoMirrored = colorDrawable.isAutoMirrored
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.colorFilter = colorDrawable.colorFilter
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            drawable.isFilterBitmap = colorDrawable.isFilterBitmap
            drawable.layoutDirection = colorDrawable.layoutDirection
        }

        return drawable
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

    /**
     * 查找第一个可见Item位置
     */
    fun findFirstVisibleItemPosition(): Int {
        return combinationLayout?.findVisibleItemPosition(this, isFirst = true) ?: -1
    }

    /**
     * 查找最后一个可见Item位置
     */
    fun findLastVisibleItemPosition(): Int {
        return combinationLayout?.findVisibleItemPosition(this, isFirst = false) ?: -1
    }

    /**
     * 查找第一个完全展示Item位置，该Item完整展示在界面，无任何部分滑出界面边界
     */
    fun findFirstCompletelyVisibleItemPosition(): Int {
        return combinationLayout?.findVisibleItemPosition(this, isFirst = true, isComplete = true)
            ?: -1
    }

    /**
     * 查找最后一个完全展示Item位置，该Item完整展示在界面，无任何部分滑出界面边界
     */
    fun findLastCompletelyVisibleItemPosition(): Int {
        return combinationLayout?.findVisibleItemPosition(this, isFirst = false, isComplete = true)
            ?: -1
    }

    /**
     * 获取指定[position]位置的View，仅当前位置数据在界面展示时返回，否则返回null
     */
    fun findViewByPosition(position: Int): View? {
        return combinationLayout?.findViewByPosition(this, position)
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

