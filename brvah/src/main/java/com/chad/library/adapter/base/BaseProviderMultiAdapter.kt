package com.chad.library.adapter.base

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 当有多种条目的时候，避免在convert()中做太多的业务逻辑，把逻辑放在对应的 ItemProvider 中。
 * 适用于以下情况：
 * 1、实体类不方便扩展，此Adapter的数据类型可以是任意类型，只需要在[getItemType]中返回对应类型
 * 2、item 类型较多，在convert()中管理起来复杂
 *
 * ViewHolder 由 [BaseItemProvider] 实现，并且每个[BaseItemProvider]可以拥有自己类型的ViewHolder类型。
 *
 * @constructor
 */
abstract class BaseProviderMultiAdapter<BH : BaseViewHolder>(data: List<BaseNode>? = null) :
    BaseQuickAdapter<BaseNode, BH>(0, data) {

    private val mItemProviders by lazy(LazyThreadSafetyMode.NONE) { SparseArray<BaseItemProvider<*, BH>>() }

    /**
     * 返回 item 类型
     * @param data List<T>
     * @param position Int
     * @return Int
     */
    protected abstract fun getItemType(data: List<BaseNode>, position: Int): Int

    /**
     * 必须通过此方法，添加 provider
     * @param provider BaseItemProvider
     */
    open fun <T : BaseNode> addItemProvider(provider: BaseItemProvider<T, BH>) {
        provider.setAdapter(this)
        mItemProviders.put(provider.itemViewType, provider)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BH {
        val provider = getItemProvider<BaseNode>(viewType)
        checkNotNull(provider) { "ViewType: $viewType no such provider found，please use addItemProvider() first!" }
        provider.context = parent.context
        return provider.onCreateViewHolder(parent, viewType).apply {
            provider.onViewHolderCreated(this, viewType)
        }
    }

    override fun getDefItemViewType(position: Int): Int {
        return getItemType(data, position)
    }

    override fun convert(holder: BH, item: BaseNode) {
        getItemProvider<BaseNode>(holder.itemViewType)!!.convert(holder, item)
    }

    override fun convert(holder: BH, item: BaseNode, payloads: List<Any>) {
        getItemProvider<BaseNode>(holder.itemViewType)!!.convert(holder, item, payloads)
    }

    override fun bindViewClickListener(viewHolder: BH, viewType: Int) {
        super.bindViewClickListener(viewHolder, viewType)
        bindClick(viewHolder, viewType)
        bindChildClick(viewHolder, viewType)
    }

    /**
     * 通过 ViewType 获取 BaseItemProvider
     * 例如：如果ViewType经过特殊处理，可以重写此方法，获取正确的Provider
     * （比如 ViewType 通过位运算进行的组合的）
     *
     * @param viewType Int
     * @return BaseItemProvider
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun <T : BaseNode> getItemProvider(viewType: Int): BaseItemProvider<T, BH>? {
        return mItemProviders.get(viewType) as? BaseItemProvider<T, BH>
    }

    override fun onViewAttachedToWindow(holder: BH) {
        super.onViewAttachedToWindow(holder)
        getItemProvider<BaseNode>(holder.itemViewType)?.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: BH) {
        super.onViewDetachedFromWindow(holder)
        getItemProvider<BaseNode>(holder.itemViewType)?.onViewDetachedFromWindow(holder)
    }

    protected open fun bindClick(viewHolder: BH, viewType: Int) {
        if (getOnItemClickListener() == null) {
            //如果没有设置点击监听，则回调给 itemProvider
            //Callback to itemProvider if no click listener is set
            val provider = getItemProvider<BaseNode>(viewType)
            if (provider != null && provider.itemCouldClick) {
                viewHolder.itemView.setOnClickListener {
                    var position = viewHolder.adapterPosition
                    if (position == RecyclerView.NO_POSITION) {
                        return@setOnClickListener
                    }
                    position -= headerLayoutCount
                    provider.onClick(viewHolder, it, data[position], position)
                }
            }
        }

        if (getOnItemLongClickListener() == null) {
            //如果没有设置长按监听，则回调给itemProvider
            // If you do not set a long press listener, callback to the itemProvider
            val provider = getItemProvider<BaseNode>(viewType)
            if (provider != null && provider.itemCouldLongClick) {
                viewHolder.itemView.setOnLongClickListener {
                    var position = viewHolder.adapterPosition
                    if (position == RecyclerView.NO_POSITION) {
                        return@setOnLongClickListener false
                    }
                    position -= headerLayoutCount
                    provider.onLongClick(viewHolder, it, data[position], position)
                }
            }
        }
    }

    protected open fun bindChildClick(viewHolder: BH, viewType: Int) {
        if (getOnItemChildClickListener() == null) {
            val provider = getItemProvider<BaseNode>(viewType) ?: return
            val ids = provider.getChildClickViewIds()
            ids.forEach { id ->
                viewHolder.itemView.findViewById<View>(id)?.let {
                    if (!it.isClickable) {
                        it.isClickable = true
                    }
                    it.setOnClickListener { v ->
                        var position: Int = viewHolder.adapterPosition
                        if (position == RecyclerView.NO_POSITION) {
                            return@setOnClickListener
                        }
                        position -= headerLayoutCount
                        provider.onChildClick(viewHolder, v, data[position], position)
                    }
                }
            }
        }
        if (getOnItemChildLongClickListener() == null) {
            val provider = getItemProvider<BaseNode>(viewType) ?: return
            val ids = provider.getChildLongClickViewIds()
            ids.forEach { id ->
                viewHolder.itemView.findViewById<View>(id)?.let {
                    if (!it.isLongClickable) {
                        it.isLongClickable = true
                    }
                    it.setOnLongClickListener { v ->
                        var position: Int = viewHolder.adapterPosition
                        if (position == RecyclerView.NO_POSITION) {
                            return@setOnLongClickListener false
                        }
                        position -= headerLayoutCount
                        provider.onChildLongClick(viewHolder, v, data[position], position)
                    }
                }
            }
        }
    }
}