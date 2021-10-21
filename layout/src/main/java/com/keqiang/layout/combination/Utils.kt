package com.keqiang.layout.combination

import android.view.View
import me.zhouzhuo810.magpiex.utils.SimpleUtil

fun View.scale() {
    if (SimpleUtil.hasScaled(this)) {
        return
    }

    SimpleUtil.scaleView(this)
    if (this is GroupPlaceholder) {
        this.children.forEach {
            it.scale()
        }
    } else if (this is CombinationLayout) {
        this.getChildren().forEach {
            it.scale()
        }
    }
}