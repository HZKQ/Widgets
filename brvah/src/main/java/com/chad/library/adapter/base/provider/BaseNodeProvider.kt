package com.chad.library.adapter.base.provider

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.viewholder.BaseViewHolder

abstract class BaseNodeProvider<T: BaseNode,BH: BaseViewHolder> : BaseItemProvider<T, BH>() {

    override fun getAdapter(): BaseNodeAdapter<BH>? {
        return super.getAdapter() as? BaseNodeAdapter<BH>
    }
}