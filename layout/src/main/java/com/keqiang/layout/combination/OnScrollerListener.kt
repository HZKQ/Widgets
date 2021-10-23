package com.keqiang.layout.combination

/**
 * 滑动监听
 *
 * @author Created by wanggaowan on 2021/10/22 21:58
 */
interface OnScrollListener {
    /**
     * [newState]：[SCROLL_STATE_IDLE]、[SCROLL_STATE_DRAGGING]、[SCROLL_STATE_SETTLING]
     */
    fun onScrollStateChanged(newState: Int)

    fun onScrolled(dx: Int, dy: Int)

    companion object {
        const val SCROLL_STATE_IDLE = 0
        const val SCROLL_STATE_DRAGGING = 1
        const val SCROLL_STATE_SETTLING = 2
    }
}