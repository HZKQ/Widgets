package com.keqiang.layout.combination

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView

/**
 * 延迟加载列布局，可结合[AdapterView]使用，实现[RecyclerView]功能。
 * 正常就相当于横向的[LinearLayout]。
 *
 * 由于对[addView]的视图进行重新组合，因此系统的[findViewById]无法查找对应的View，
 * 请使用[findViewById2]。[getChildAt]同理，建议使用[getChildAt2]
 *
 * @author Created by wanggaowan on 2021/9/18 15:11
 */
class LazyRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CombinationLayout(
    context, attrs, defStyleAttr, HORIZONTAL
)