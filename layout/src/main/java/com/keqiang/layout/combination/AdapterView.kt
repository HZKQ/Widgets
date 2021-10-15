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
) : View(context, attrs, defStyleAttr) {

    private var mItemCount = 1
    private var mAdapterChangeListener: MutableList<AdapterChangeListener> = mutableListOf()
    private var mLayoutChangeListener: LayoutChangeListener? = null
    internal var scrollListener: ScrollListener? = null
    internal val recyclerView: RecyclerView = RecyclerView(context)
    internal val recyclerViewLayoutManager = LinearLayoutManager(context)
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutResId = NO_ID
    private var mBackgroundResId = NO_ID
    private var mAttachedToRecyclerView = false

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
    }

    override fun invalidate() {
        ViewGroup.LayoutParams.WRAP_CONTENT
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
        if (mAttachedToRecyclerView) {
            recyclerView.adapter = mAdapter
        }
        mAdapterChangeListener.forEach { it.invoke(oldAdapter, mAdapter) }
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

    /**
     * 创建用于预览的View
     */
    internal fun createPreviewView(orientation: Int): View {
        if (mItemCount < 1 || mLayoutResId == NO_ID) {
            return this
        }

        val layout = LinearLayout(context)
        layout.orientation = orientation
        layout.layoutParams = layoutParams
        layout.setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)
        layout.background = background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            layout.backgroundTintMode = backgroundTintMode
            layout.backgroundTintList = backgroundTintList
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            layout.backgroundTintBlendMode = backgroundTintBlendMode
        }

        for (i in 0 until mItemCount) {
            LayoutInflater.from(context).inflate(mLayoutResId, layout, true)
        }

        return layout
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

    internal fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mAttachedToRecyclerView = true
        mAdapter?.onAttachedToRecyclerView(this.recyclerView)
    }

    internal fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        mAttachedToRecyclerView = false
        mAdapter?.onDetachedFromRecyclerView(this.recyclerView)
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

