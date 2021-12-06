package com.keqiang.chart.utils

import android.graphics.Paint
import android.graphics.Rect

// Paint的扩展类
// Created by wanggaowan on 2021/12/3 10:49

/**
 * 获取文本高度，取值[Paint.FontMetrics.descent] - [Paint.FontMetrics.ascent]
 */
fun Paint.getTextHeight(textSize: Float): Float {
    this.textSize = textSize
    val fm: Paint.FontMetrics = this.fontMetrics
    return fm.descent - fm.ascent
}

/**
 * 获取文本高度，取值[Paint.FontMetrics.bottom] - [Paint.FontMetrics.top],此高度比[getTextHeight]高
 */
fun Paint.getTextHeight2(textSize: Float): Float {
    this.textSize = textSize
    val fm: Paint.FontMetrics = this.fontMetrics
    return fm.bottom - fm.top
}

/**
 * 获取文本绘制矩形区域
 */
fun Paint.getTextBounds(textSize: Float, text: String): Rect {
    this.textSize = textSize
    val rect = Rect()
    getTextBounds(text, 0, text.length, rect)
    return rect
}

/**
 * 获取居中baseLine，如果需要将文本与y轴某一点（a）水平居中对齐，则只需将需要 a + 此值 = 绘制文本的y轴坐标
 */
fun Paint.getCenterBaseline(textSize: Float): Float {
    this.textSize = textSize
    val fm = this.fontMetrics
    return (fm.bottom - fm.top) / 2 - fm.bottom
}

