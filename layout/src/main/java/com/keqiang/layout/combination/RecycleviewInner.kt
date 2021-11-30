package com.keqiang.layout.combination

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keqiang.layout.R
import kotlin.math.abs

/**
 * 自定义RecyclerView
 *
 * @author Created by wanggaowan on 2021/11/3 11:16
 */
class RecyclerViewInner(context: Context, attrs: AttributeSet? = null, defStyle: Int = R.attr.recyclerViewStyle)
    : RecyclerView(context, attrs, defStyle) {

    /**
     * 是否可滑动
     */
    private var mCanScroll = true
    internal var layoutManager: GridLayoutManager
    private var oldX = 0f
    private var oldY = 0f
    private var child: View? = null

    init {
        itemAnimator = null
        overScrollMode = View.OVER_SCROLL_NEVER

        layoutManager = object : GridLayoutManager(context, 1) {
            override fun canScrollHorizontally(): Boolean {
                return if (orientation == HORIZONTAL) {
                    mCanScroll
                } else super.canScrollHorizontally()
            }

            override fun canScrollVertically(): Boolean {
                return if (orientation == VERTICAL) {
                    mCanScroll
                } else super.canScrollVertically()
            }

            override fun scrollToPosition(position: Int) {
                mCanScroll = true
                super.scrollToPosition(position)
            }

            override fun scrollToPositionWithOffset(position: Int, offset: Int) {
                mCanScroll = true
                super.scrollToPositionWithOffset(position, offset)
            }

            override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: State?, position: Int) {
                mCanScroll = true
                super.smoothScrollToPosition(recyclerView, state, position)
            }

        }

        setLayoutManager(layoutManager)
    }

    internal fun setOrientation(@Orientation orientation: Int) {
        layoutManager.orientation = orientation
    }

    /**
     * 请求不要滑动
     */
    fun requestNotScroll() {
        mCanScroll = false
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> mCanScroll = true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                oldX = e.x
                oldY = e.y
                child = findChildViewUnder(e.x, e.y)
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = e.x - oldX
                val dy = e.y - oldY
                oldX = e.x
                oldY = e.y

                if (childNeedTouchEvent(dx, dy, child)) {
                    return false
                }
            }
        }

        return super.onInterceptTouchEvent(e)
    }

    private fun childNeedTouchEvent(dx: Float, dy: Float, child: View?): Boolean {
        if (child == null) {
            return false
        }

        val rv = getRecyclerView(child)
        if (rv is RecyclerView) {
            rv.apply {
                if (!this.isEnabled || !this.isNestedScrollingEnabled) {
                    return false
                }

                if (abs(dx) > abs(dy)) {
                    return canScrollHorizontally(if (dx > 0) -1 else 1)
                } else if (abs(dx) < abs(dy)) {
                    return canScrollVertically(if (dy > 0) -1 else 1)
                }
            }
        }

        return false
    }

    private fun getRecyclerView(view: View?): RecyclerView? {
        if (view is RecyclerView) {
            return view
        }

        if (view is DetectLayoutViewGroup) {
            val child = view.getChildAt(0)
            if (child is RecyclerView) {
                return child
            }
        }

        return null
    }
}