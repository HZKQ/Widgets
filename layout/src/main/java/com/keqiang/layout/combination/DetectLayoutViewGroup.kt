package com.keqiang.layout.combination

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout

/**
 * 检测布局变更，用于宽高根据子节点自适应大小
 *
 * @author Created by wanggaowan on 2021/11/8 09:06
 */
internal class DetectLayoutViewGroup(context: Context, orientation: Int, child: View) : FrameLayout(context) {

    init {
        val params = if (orientation == LinearLayout.HORIZONTAL) {
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        } else {
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        layoutParams = params
        addView(child)
    }
}