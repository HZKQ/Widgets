package com.keqiang.layout.combination

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView

/**
 * 占位符，用于整体添加一组控件到[LazyColumnData]或[LazyRow]，实际运行时，该组件本身将移除不显示，其子节点向上提升。
 * 且该组件任何属性都将不作用于其子节点，比如宽高，背景色等属性。实际运行时排列方式根据添加到[LazyColumn]或[LazyRow]决定。
 * 预览时可通过[setOrientation]指定排列方式。由于最终实现采用[RecyclerView],因此实际运行时，查找xml中对象，
 * 请使用[findViewById2]、[getChildAt2]、[children]方法获取实际运行的View对象
 *
 * @author Created by wanggaowan on 2021/10/13 16:08
 */
class GroupPlaceholder @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private var views: MutableList<View> = mutableListOf()
    val children: List<View> = views
    internal val viewDataList: MutableList<ViewData<*>> = mutableListOf()

    internal var addViewListener: (GroupPlaceholder.(view: View, index: Int) -> Unit)? = null
    internal var removeViewListener: (GroupPlaceholder.(start: Int, count: Int, preventRequestLayout: Boolean) -> Unit)? = null
    internal var scrollListener: ScrollListener? = null

    init {
        if (!isInEditMode) {
            viewMap()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (isInEditMode) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child is AdapterView) {
                    removeView(child)
                    addView(child.createPreviewView(orientation), i)
                }
            }
        }
    }

    private fun viewMap() {
        views.clear()
        (0 until childCount).forEach {
            views.add(getChildAt(it))
        }
        removeAllViews()
    }

    /**
     * 查找[CombinationLayout]布局中的View，由于[CombinationLayout]对布局进行重新组合，因此使用[findViewById]无法查找xml中对应的[View]
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : View?> findViewById2(@IdRes id: Int): T? {
        for (view in views) {
            if (view.id == id) {
                return view as T
            }

            if (view is CombinationLayout) {
                return view.findViewById2(id) ?: continue
            }

            if (view is GroupPlaceholder) {
                return view.findViewById2(id) ?: continue
            }

            if (view is ViewGroup) {
                return view.findViewById(id) ?: continue
            }
        }

        return null
    }

    /**
     * 查找[CombinationLayout]布局中的View，由于[CombinationLayout]对布局进行重新组合，因此使用[getChildAt]无法查找xml中对应的[View]
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : View?> getChildAt2(index: Int): T? {
        for (i in views.indices) {
            if (index == i) {
                return views[i] as T
            }
        }
        return null
    }

    @Deprecated("此对象无法获取实际位置对应的View数量", ReplaceWith("请使用getChildAt2(Int)"))
    override fun getChildAt(index: Int): View {
        return super.getChildAt(index)
    }

    @Deprecated("此对象无法获取实际View数量", ReplaceWith("请使用getChildren()"))
    override fun getChildCount(): Int {
        return super.getChildCount()
    }

    override fun addView(child: View, index: Int) {
        if (isInEditMode) {
            super.addView(child, index)
            return
        }

        if (index == -1) {
            views.add(child)
        } else {
            views.add(index, child)
        }
        addViewListener?.invoke(this, child, index)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (isInEditMode) {
            super.addView(child, index, params)
            return
        }

        val lp = if (checkLayoutParams(params)) params else generateLayoutParams(params)
        child.layoutParams = lp
        addView(child, index)
    }

    override fun addView(child: View, width: Int, height: Int) {
        if (isInEditMode) {
            super.addView(child, width, height)
            return
        }

        val lp = generateDefaultLayoutParams()
        lp.width = width
        lp.height = height
        child.layoutParams = lp
        addView(child)
    }

    override fun removeAllViews() {
        if (isInEditMode) {
            super.removeAllViews()
            return
        }

        if (views.isEmpty()) {
            return
        }

        removeViewListener?.invoke(this, 0, views.size, false)
        removeListener(this)
        views.clear()
    }

    private fun removeListener(group: GroupPlaceholder) {
        group.scrollListener = null
        group.addViewListener = null
        group.removeViewListener = null
        group.views.forEach {
            if (it is GroupPlaceholder) {
                removeListener(it)
            }
        }
    }

    override fun removeView(view: View?) {
        if (isInEditMode) {
            super.removeView(view)
            return
        }

        val index = views.indexOf(view)
        if (index == -1) {
            return
        }

        removeViewListener?.invoke(this, index, 1, false)
        if (view is GroupPlaceholder) {
            removeListener(view)
        }
        views.removeAt(index)
    }

    override fun removeViewAt(index: Int) {
        if (isInEditMode) {
            super.removeViewAt(index)
            return
        }

        if (index < 0 || index >= views.size) {
            return
        }

        removeViewListener?.invoke(this, index, 1, false)
        val view = views[index]
        if (view is GroupPlaceholder) {
            removeListener(view)
        }
        views.removeAt(index)
    }

    override fun removeViews(start: Int, count: Int) {
        if (isInEditMode) {
            super.removeViews(start, count)
            return
        }

        val end = start + count
        if (start < 0 || count < 0 || end > views.size) {
            return
        }

        removeViewListener?.invoke(this, start, count, false)
        for (i in start until end) {
            val view = views[start]
            if (view is GroupPlaceholder) {
                removeListener(view)
            }
            views.removeAt(start)
        }
    }

    override fun removeAllViewsInLayout() {
        if (isInEditMode) {
            super.removeAllViewsInLayout()
            return
        }

        if (views.isEmpty()) {
            return
        }

        removeViewListener?.invoke(this, 0, views.size, true)
        removeListener(this)
        views.clear()
    }

    override fun removeViewInLayout(view: View?) {
        if (isInEditMode) {
            super.removeViewInLayout(view)
            return
        }

        val index = views.indexOf(view)
        if (index == -1) {
            return
        }

        removeViewListener?.invoke(this, index, 1, true)
        if (view is GroupPlaceholder) {
            removeListener(view)
        }
        views.remove(view)
    }

    override fun removeViewsInLayout(start: Int, count: Int) {
        if (isInEditMode) {
            super.removeViewsInLayout(start, count)
            return
        }

        val end = start + count
        if (start < 0 || count < 0 || end > views.size) {
            return
        }

        removeViewListener?.invoke(this, start, count, true)
        for (i in start until end) {
            val view = views[start]
            if (view is GroupPlaceholder) {
                removeListener(view)
            }
            views.removeAt(start)
        }
    }

    @Deprecated("GroupPlaceholder does not support scrolling to an absolute position.",
        ReplaceWith("scrollToPosition"))
    override fun scrollTo(x: Int, y: Int) {

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
}