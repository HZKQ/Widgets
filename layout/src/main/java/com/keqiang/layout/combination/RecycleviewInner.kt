package com.keqiang.layout.combination

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

    init {
        itemAnimator = null
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    internal fun setLinearLayoutManager(@Orientation orientation: Int) {
        val layoutManager: LinearLayoutManager = object : LinearLayoutManager(context, orientation, false) {
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
}