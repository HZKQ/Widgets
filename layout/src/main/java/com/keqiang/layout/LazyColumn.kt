package com.keqiang.layout

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Created by wanggaowan on 2021/9/15 09:28
 */
class LazyColumn @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mItemCount = 1
    private var mAdapterChangeListener: MutableList<() -> Unit> = mutableListOf()
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var layoutResId = NO_ID

    init {
        if (attrs != null) {
            var typedArray: TypedArray? = null
            try {
                typedArray =
                    context.obtainStyledAttributes(attrs, R.styleable.LazyColumn, defStyleAttr, 1)
                mItemCount = typedArray.getInt(R.styleable.LazyColumn_itemCount, 0)
                layoutResId = typedArray.getResourceId(R.styleable.LazyColumn_layout, NO_ID)
            } finally {
                typedArray?.recycle()
            }
        }
    }

    /**
     * 注册适配器改变监听
     */
    fun registerAdapterChangeListener(listener: (() -> Unit)?) {
        listener?.apply { mAdapterChangeListener.add(this) }
    }

    /**
     * 移除适配器改变监听
     */
    fun removeAdapterChangeListener(listener: (() -> Unit)?) {
        listener?.apply { mAdapterChangeListener.remove(this) }
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        mAdapter = adapter
        mAdapterChangeListener.forEach { it.invoke() }
    }

    fun <VH : RecyclerView.ViewHolder> getAdapter(): RecyclerView.Adapter<VH>? {
        return mAdapter as RecyclerView.Adapter<VH>?
    }

    /**
     * 创建用于预览的View
     */
    internal fun createPreviewView(): Array<View> {
        if (mItemCount < 1 || layoutResId == NO_ID) {
            return arrayOf()
        }

        val views: Array<View> = Array(mItemCount) {
            LayoutInflater.from(context).inflate(layoutResId, null)
        }

        return views
    }
}
