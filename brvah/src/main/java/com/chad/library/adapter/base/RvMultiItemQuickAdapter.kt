package com.chad.library.adapter.base

import android.view.View
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import me.zhouzhuo810.magpiex.utils.SimpleUtil

/**
 * 解除 BaseMultiItemQuickAdapter 的布局屏幕适配顾虑，继承这个不用考虑屏幕适配问题
 *
 * @author zhouzhuo810
 */
abstract class RvMultiItemQuickAdapter<T : MultiItemEntity, K : BaseViewHolder> : BaseMultiItemQuickAdapter<T, K> {
    constructor() : super() {
        initClickIdsIfNeeded()
    }

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    constructor(data: MutableList<T>?) : super(data) {
        initClickIdsIfNeeded()
    }

    private fun initClickIdsIfNeeded() {
        nestViewIds?.forEach {
            addChildClickViewIds(it)
        }
    }

    override fun createBaseViewHolder(view: View): K {
        val baseViewHolder = super.createBaseViewHolder(view)
        if (!disableScale()) {
            SimpleUtil.scaleView(baseViewHolder!!.itemView)
        }
        return baseViewHolder
    }

    /**
     * 是否禁用缩放
     *
     * @return 是否，默认false
     */
    protected fun disableScale(): Boolean {
        return false
    }

    /**
     * Item中需要设置点击或长按事件的子View的Id
     *
     * @return ids
     */
    abstract val nestViewIds: IntArray?
}