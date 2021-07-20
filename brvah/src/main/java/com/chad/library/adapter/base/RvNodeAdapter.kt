package com.chad.library.adapter.base

import android.view.View
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import me.zhouzhuo810.magpiex.utils.SimpleUtil

/**
 * 解除 BaseNodeAdapter 的布局屏幕适配顾虑，继承这个不用考虑屏幕适配问题
 *
 * @author zhouzhuo810
 */
abstract class RvNodeAdapter<BH: BaseViewHolder> : BaseNodeAdapter<BH> {
    constructor() : super() {
        initClickIdsIfNeeded()
    }

    constructor(nodeList: MutableList<BaseNode>?) : super(nodeList) {
        initClickIdsIfNeeded()
    }

    private fun initClickIdsIfNeeded() {
        nestViewIds.forEach {
            addChildClickViewIds(it)
        }
    }

    override fun createBaseViewHolder(view: View): BH {
        val baseViewHolder = super.createBaseViewHolder(view)
        if (!disableScale()) {
            SimpleUtil.scaleView(baseViewHolder.itemView)
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
    abstract val nestViewIds: IntArray
}