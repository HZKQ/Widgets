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
import androidx.recyclerview.widget.RecyclerView
import com.keqiang.layout.R

/**
 * @author Created by wanggaowan on 2021/9/15 09:28
 */
class LazyColumn @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mItemCount = 1
    private var mAdapterChangeListener: MutableList<AdapterChangeListener> = mutableListOf()
    private var mLayoutChangeListener: LayoutChangeListener? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutResId = NO_ID
    private var mBackgroundResId = NO_ID

    /**
     * 是否隔离视图，如果设置为true，那么不同[LazyColumn]的adapter视图不相互复用，
     * 如果设置为false，那么多个[LazyColumn]的adapter视图可复用，只要[RecyclerView.Adapter.getItemViewType]一致
     */
    var isolateViewTypes: Boolean = true

    /**
     * 用来标识复用相同adapter item布局的多个[LazyColumn]
     */
    var typeFlag: String = ""

    init {
        if (attrs != null) {
            var typedArray: TypedArray? = null
            try {
                typedArray =
                    context.obtainStyledAttributes(attrs, R.styleable.LazyColumn, defStyleAttr, 1)
                mItemCount = typedArray.getInt(R.styleable.LazyColumn_itemCount, 0)
                mLayoutResId = typedArray.getResourceId(R.styleable.LazyColumn_layout, NO_ID)
                mBackgroundResId =
                    typedArray.getResourceId(R.styleable.LazyColumn_android_background, NO_ID)
                isolateViewTypes =
                    typedArray.getBoolean(R.styleable.LazyColumn_isolateViewTypes, true)
                typeFlag = typedArray.getString(R.styleable.LazyColumn_type_flag) ?: ""
            } finally {
                typedArray?.recycle()
            }
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
     * 注册适配器改变监听
     */
    fun registerAdapterChangeListener(listener: AdapterChangeListener?) {
        listener?.apply { mAdapterChangeListener.add(this) }
    }

    /**
     * 移除适配器改变监听
     */
    fun removeAdapterChangeListener(listener: AdapterChangeListener?) {
        listener?.apply { mAdapterChangeListener.remove(this) }
    }

    /**
     * 注册适配器改变监听
     */
    fun registerLayoutChangeListener(listener: LayoutChangeListener?) {
        mLayoutChangeListener = listener
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        val oldAdapter = mAdapter
        mAdapter = adapter
        mAdapterChangeListener.forEach { it.invoke(oldAdapter, mAdapter) }
    }

    @Suppress("UNCHECKED_CAST")
    fun <VH : RecyclerView.ViewHolder> getAdapter(): RecyclerView.Adapter<VH>? {
        return mAdapter as RecyclerView.Adapter<VH>?
    }

    /**
     * 创建用于预览的View
     */
    internal fun createPreviewView(parent: ViewGroup): View {
        if (mItemCount < 1 || mLayoutResId == NO_ID) {
            return this
        }

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
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
}

/**
 * 适配器改变监听
 */
typealias AdapterChangeListener = (oldAdapter: RecyclerView.Adapter<*>?, adapter: RecyclerView.Adapter<*>?) -> Unit

/**
 * 布局数据改变监听
 */
typealias LayoutChangeListener = () -> Unit
