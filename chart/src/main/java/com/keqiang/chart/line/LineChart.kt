package com.keqiang.chart.line

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.annotation.Px
import me.zhouzhuo810.magpiex.utils.SimpleUtil

/**
 * 折线图
 *
 * @author Created by wanggaowan on 2021/11/12 15:03
 */
class LineChart @JvmOverloads constructor(context: Context,
                                          attrs: AttributeSet?,
                                          defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    /**
     * x轴
     */
    var xAxis: Axis = Axis.copy()
        private set

    /**
     * y轴
     */
    var yAxis: Axis = Axis.copy()
        private set

    /**
     * 如果X与Y轴0值内容、文字大小、文字颜色一致时，是否绘制公共0值，而不单独在x轴、y轴各绘制一个
     */
    var drawPublicZero = true

    private var lineDataList: List<LineData>? = null

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tempRectF = RectF()

    init {
        if (!isInEditMode) {
            xAxis.labelSize(SimpleUtil.getScaledValue(24f))
            yAxis.labelSize(SimpleUtil.getScaledValue(24f))
        } else {
            xAxis.valueFormat { it.toString() }
                .labelWidth(100)
                .labelOffset(10)
                .drawGridLine(true)
                .topOffset(30)
                .gridUseTopOffset(true)


            yAxis.labelOffset(10)
                .drawGridLine(true)
                .topOffset(30)
                .gridUseTopOffset(true)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (yAxis.max - yAxis.min <= 0
            || xAxis.max - xAxis.min <= 0) {
            return
        }

        initAxisLabel(yAxis)
        initAxisLabel(xAxis)

        // 默认顶部和右边加上10px的padding
        val paddingOffset = if (!isInEditMode) {
            SimpleUtil.getScaledValue(10f)
        } else {
            10f
        }

        val yTextWidthOffset = if (!isInEditMode) {
            SimpleUtil.getScaledValue(5f)
        } else {
            5f
        }

        val yMaxTextWidth = getTextMaxWidth(yAxis, true)
        val xMaxTextWidth = getTextMaxWidth(xAxis)

        val xStart = paddingStart + yMaxTextWidth + yTextWidthOffset
        val xEnd = width - paddingEnd - paddingOffset
        val xTextHeight = getTextHeight(xAxis.labelSize)
        val xBottom = height - paddingBottom - xTextHeight - xAxis.labelOffset
        val xTop = xBottom - xAxis.lineWidth

        val yTop = paddingTop + paddingOffset

        val xZeroLabel = if (xAxis.labelList.isNullOrEmpty()) "" else xAxis.labelList!![0]
        val yZeroLabel = if (yAxis.labelList.isNullOrEmpty()) "" else yAxis.labelList!![0]
        val jumpZeroLabel = drawPublicZero && xZeroLabel == yZeroLabel && xAxis.labelSize == yAxis.labelSize
            && xAxis.labelColor == yAxis.labelColor && yMaxTextWidth > 0

        drawXAxis(canvas, xAxis, xStart, xEnd, xTop, xBottom, xTextHeight, xMaxTextWidth.toFloat(), jumpZeroLabel, xTop - yTop)
        drawYAxis(canvas, yAxis, xStart, (xStart + yAxis.lineWidth), yTop, xTop,
            yMaxTextWidth.toFloat(), getTextHeight(yAxis.labelSize), yTextWidthOffset, jumpZeroLabel, xEnd - xStart)
        if (jumpZeroLabel) {
            drawPublicZeroLabel(canvas, xZeroLabel, xStart, xBottom, yMaxTextWidth, yTextWidthOffset, xTextHeight)
        }
    }

    /**
     * 如果X/Y轴0值一致，则绘制公共零轴值
     */
    private fun drawPublicZeroLabel(canvas: Canvas, zeroLabel: String, xStart: Float, xBottom: Float, maxTextWidth: Int,
                                    yTextWidthOffset: Float, textHeight: Float) {
        textPaint.textSize = xAxis.labelSize
        textPaint.color = xAxis.labelColor
        val width = textPaint.measureText(zeroLabel) + yAxis.labelOffset
        if (width > maxTextWidth) {
            canvas.save()
            tempRectF.set(xStart - maxTextWidth - yTextWidthOffset, xBottom + xAxis.labelOffset,
                xStart - yTextWidthOffset, xBottom + xAxis.labelOffset + textHeight)
            canvas.clipRect(tempRectF)
            canvas.drawText(zeroLabel, tempRectF.left, tempRectF.bottom, textPaint)
            canvas.restore()
        } else {
            canvas.drawText(zeroLabel, xStart - maxTextWidth - yTextWidthOffset, xBottom + xAxis.labelOffset + textHeight, textPaint)
        }
    }

    private fun getTextHeight(size: Float): Float {
        textPaint.textSize = size
        val fontMetrics: Paint.FontMetrics = textPaint.fontMetrics
        return fontMetrics.descent - fontMetrics.ascent
    }

    private fun drawXAxis(canvas: Canvas, axis: Axis,
                          start: Float, end: Float,
                          top: Float, bottom: Float,
                          textHeight: Float, maxTextWidth: Float,
                          jumpZeroLabel: Boolean, yRange: Float) {
        // 绘制轴线
        paint.color = axis.lineColor
        canvas.drawLine(start, top, end, bottom, paint)

        var tempStart = start
        val step = (end - start - axis.topOffset) / axis.labelCount
        textPaint.textSize = axis.labelSize
        textPaint.color = axis.labelColor
        paint.color = axis.gridLineColor

        axis.labelList?.let {
            for (index in it.indices) {
                if (maxTextWidth > 0) {
                    // 绘制x轴文本
                    if (!jumpZeroLabel || index != 0) {
                        val text = it[index]
                        if (text.isNotEmpty()) {
                            val width = textPaint.measureText(text)
                            if (width > maxTextWidth) {
                                canvas.save()
                                tempRectF.set(tempStart - maxTextWidth / 2f, bottom + axis.labelOffset,
                                    tempStart + maxTextWidth / 2f, bottom + textHeight + axis.labelOffset)
                                canvas.clipRect(tempRectF)
                                canvas.drawText(text, tempRectF.left, tempRectF.bottom, textPaint)
                                canvas.restore()
                            } else {
                                canvas.drawText(text, tempStart - width / 2f, bottom + textHeight + axis.labelOffset, textPaint)
                            }
                        }
                    }
                }

                if (index > 0 && axis.drawGridLine) {
                    canvas.drawLine(tempStart - axis.gridLineWidth / 2f,
                        if (axis.gridUseTopOffset) top - yRange + axis.topOffset else top - yRange,
                        tempStart + axis.gridLineWidth / 2f, top, paint)
                }

                tempStart += step
            }
        }
    }

    private fun drawYAxis(canvas: Canvas, axis: Axis,
                          start: Float, end: Float,
                          top: Float, bottom: Float,
                          textHeight: Float, maxTextWidth: Float,
                          textWidthOffset: Float, jumpZeroLabel: Boolean,
                          xRange: Float) {

        // 绘制轴线
        paint.color = axis.lineColor
        canvas.drawLine(start, top, end, bottom, paint)

        // 绘制y轴文本
        var tempBottom = bottom
        val step = (bottom - top - axis.topOffset) / axis.labelCount
        textPaint.textSize = axis.labelSize
        textPaint.color = axis.labelColor
        paint.color = axis.gridLineColor

        axis.labelList?.let {
            for (index in it.indices) {
                if (maxTextWidth > 0) {
                    if (!jumpZeroLabel || index != 0) {
                        val text = it[index]
                        if (it.isNotEmpty()) {
                            val width = textPaint.measureText(text) + axis.labelOffset
                            if (width > maxTextWidth) {
                                canvas.save()
                                tempRectF.set(start - maxTextWidth - textWidthOffset, tempBottom - textHeight / 2f, start - textWidthOffset, tempBottom + textHeight / 2f)
                                canvas.clipRect(tempRectF)
                                canvas.drawText(text, tempRectF.left, tempRectF.bottom, textPaint)
                                canvas.restore()
                            } else {
                                canvas.drawText(text, start - width - textWidthOffset, tempBottom + textHeight / 2f, textPaint)
                            }
                        }
                    }
                }

                if (index > 0 && axis.drawGridLine) {
                    canvas.drawLine(start, tempBottom - axis.gridLineWidth / 2f,
                        if (axis.gridUseTopOffset) start + xRange - axis.topOffset else start + xRange,
                        tempBottom + axis.gridLineWidth / 2f, paint)
                }

                tempBottom -= step
            }
        }
    }

    /**
     * 初始化轴上要绘制标签列表
     */
    private fun initAxisLabel(axis: Axis) {
        if (axis.labelList != null) {
            return
        }

        val labelList = mutableListOf<String>()
        for (i in 0 until axis.labelCount + 1) {
            val label = axis.valueFormat?.invoke(i) ?: Axis.DEFAULT_VALUE_FORMAT.invoke(i)
            labelList.add(label)
        }
        axis.labelList = labelList
    }

    /**
     * 获取轴上文本绘制的最大宽度,[useLabelOffset]指定计算宽度时，是否加上label绘制距离轴线的偏移值
     */
    private fun getTextMaxWidth(axis: Axis, useLabelOffset: Boolean = false): Int {
        val max = axis.labelMinWidth.coerceAtLeast(axis.labelMaxWidth ?: 0)
        if (axis.labelWidth != null) {
            val aclWidth = if (useLabelOffset) axis.labelWidth!! + axis.labelOffset else axis.labelWidth!!
            return if (aclWidth <= max || axis.labelMaxWidth == null) {
                aclWidth
            } else {
                max
            }
        }

        var maxLengthText: String? = null
        axis.labelList?.forEach {
            if (maxLengthText == null || it.length > maxLengthText!!.length) {
                maxLengthText = it
            }
        }

        if (maxLengthText == null) {
            return if (useLabelOffset) {
                axis.labelOffset.coerceAtLeast(max)
            } else {
                max
            }
        }

        textPaint.textSize = axis.labelSize
        var width = textPaint.measureText(maxLengthText!!).toInt()
        if (useLabelOffset) {
            width += axis.labelOffset
        }

        return width.coerceAtLeast(max)
    }

    private fun drawValues(canvas: Canvas) {

    }
}

/**
 * 配置X、Y轴值内容
 */
class Axis private constructor() {
    var lineColor: Int = Color.GRAY
        private set
    var lineWidth: Int = 1
        private set
    var topOffset: Int = 0
        private set
    var min: Float = 0f
        private set
    var max: Float = 100f
        private set

    var labelCount: Int = 5
        private set
    var labelSize: Float = 24f
        private set
    var labelColor: Int = Color.BLACK
        private set
    var labelWidth: Int? = null
        private set
    var labelMinWidth: Int = 0
        private set
    var labelMaxWidth: Int? = null
        private set
    var labelOffset: Int = 0
        private set
    var valueFormat: ValueFormat? = DEFAULT_VALUE_FORMAT
        private set

    var drawGridLine: Boolean = false
        private set
    var gridLineColor: Int = Color.GRAY
        private set
    var gridLineWidth: Int = 1
        private set
    var gridUseTopOffset: Boolean = false
        private set

    internal var labelList: List<String>? = null

    /**
     * X/Y轴的颜色
     */
    fun lineColor(@ColorInt color: Int): Axis {
        lineColor = color
        return this
    }

    /**
     * X/Y轴的宽度
     */
    fun lineWidth(@Px width: Int): Axis {
        lineWidth = width
        return this
    }

    /**
     * X/Y轴顶部绘制偏移值，比如控件高度100，设置offset为10，那么Y轴高度为100，折线图实际可用绘制高度90
     */
    fun topOffset(@Px offset: Int): Axis {
        topOffset = offset
        return this
    }

    /**
     * 绘制坐标起始值
     */
    fun min(min: Float): Axis {
        this.min = min
        return this
    }

    /**
     * 绘制坐标结束值
     */
    fun max(max: Float): Axis {
        this.max = max
        return this
    }

    /**
     * 轴上标签数量，不包含0值坐标，比如设置X轴最小最大值为0、100，labelCount设置为5，
     * 则X轴标签值为0，20，40，60，80，100
     */
    fun labelCount(@IntRange(from = 1) count: Int): Axis {
        if (count == labelCount) {
            return this
        }

        if (count < 1) {
            this.labelCount = 1
        } else {
            this.labelCount = count
        }
        labelList = null
        return this
    }

    /**
     * 标签值颜色
     */
    fun labelColor(@ColorInt color: Int): Axis {
        this.labelColor = color
        return this
    }

    /**
     * 标签值文本大小
     */
    fun labelSize(@Px size: Float): Axis {
        this.labelSize = size
        return this
    }

    /**
     * 标签绘制宽度，超出此宽度则裁剪标签.设置为null则根据文本宽度及[labelMaxWidth]自适应
     */
    fun labelWidth(@Px @IntRange(from = 0) width: Int?): Axis {
        if (width != null && width < 0) {
            return this
        }

        this.labelWidth = width
        return this
    }

    /**
     * 标签绘制最小宽度
     */
    fun labelMinWidth(@Px @IntRange(from = 0) min: Int): Axis {
        if (min < 0) {
            return this
        }

        this.labelMinWidth = min
        return this
    }

    /**
     * 标签绘制最大宽度，超出此值则裁剪标签，设置为null则无限制
     */
    fun labelMaxWidth(@Px @IntRange(from = 0) max: Int?): Axis {
        if (max != null && max < 0) {
            return this
        }

        this.labelMaxWidth = max
        return this
    }

    /**
     * 标签绘制距离轴线偏移值
     */
    fun labelOffset(@Px @IntRange(from = 0) offset: Int): Axis {
        if (offset < 0) {
            return this
        }

        this.labelOffset = offset
        return this
    }

    /**
     * 标签值格式化
     */
    fun valueFormat(valueFormat: ValueFormat?): Axis {
        if (this.valueFormat == valueFormat) {
            return this
        }

        this.valueFormat = valueFormat
        labelList = null
        return this
    }

    /**
     * 是否绘制网格线
     */
    fun drawGridLine(isDraw: Boolean): Axis {
        this.drawGridLine = isDraw
        return this
    }

    /**
     * 网格线颜色
     */
    fun gridLineColor(@ColorInt color: Int): Axis {
        this.gridLineColor = color
        return this
    }

    /**
     * 网格线宽度
     */
    fun gridLineWidth(@Px @IntRange(from = 0) width: Int): Axis {
        if (width < 1) {
            return this
        }

        this.gridLineWidth = width
        return this
    }

    /**
     * [topOffset]属性是否作用于网格线，如果此值为true,那么X轴网格线绘制的高度将<=y轴线高度，y轴网格线同理
     */
    fun gridUseTopOffset(use: Boolean): Axis {
        this.gridUseTopOffset = use
        return this
    }

    /**
     * 应用其它轴配置数据
     */
    fun apply(axis: Axis) {
        this.lineColor = axis.lineColor
        this.lineWidth = axis.lineWidth
        this.topOffset = axis.topOffset
        this.min = axis.min
        this.max = axis.max

        this.labelCount = axis.labelCount
        this.labelSize = axis.labelSize
        this.labelColor = axis.labelColor
        this.labelWidth = axis.labelWidth
        this.labelMinWidth = axis.labelMinWidth
        this.labelMaxWidth = axis.labelMaxWidth
        this.labelOffset = axis.labelOffset
        this.valueFormat = axis.valueFormat

        this.drawGridLine = axis.drawGridLine
        this.gridLineColor = axis.gridLineColor
        this.gridLineWidth = axis.gridLineWidth
        this.gridUseTopOffset = axis.gridUseTopOffset
    }

    companion object {
        fun copy(): Axis = Axis()

        val DEFAULT_VALUE_FORMAT = object : ValueFormat {
            override fun invoke(position: Int): String {
                return position.toString()
            }
        }
    }
}

class LineData(
    /**
     * 需要绘制的值
     */
    var values: List<Entity>
) {

    /**
     * 绘制折线类型
     */
    @setparam:LineStyle
    var lineStyle: Int = LINER

    /**
     * 绘制折线宽度
     */
    @setparam:Px
    @setparam:IntRange(from = 1)
    var lineWidth: Int = 1

    /**
     * 绘制折线颜色
     */
    @setparam:ColorInt
    var lineColor: Int = Color.GRAY

    /**
     * 是否填充背景
     */
    var fillBg: Boolean = false

    /**
     * 填充的背景颜色，如果设置了此值也设置了[bgDrawable],则优先使用[bgDrawable]
     */
    var bgColor: Int = 0

    /**
     * 填充的背景图像
     */
    var bgDrawable: Drawable? = null

    @IntDef(LINER, CUBIC_BEZIER, HORIZONTAL_BEZIER)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    private annotation class LineStyle

    companion object {
        const val LINER = 1
        const val CUBIC_BEZIER = 2
        const val HORIZONTAL_BEZIER = 3
    }
}

class Entity(var value: Float = 0f) {
    /**
     * 是否绘制此数据
     */
    var isDraw: Boolean = true

    /**
     * 是否绘制文本值
     */
    var drawValue: Boolean = false

    /**
     * 文本值字体大小
     */
    var valueTextSize: Float = 24f

    /**
     * 文本值字体颜色
     */
    var valueTextColor: Int = Color.BLACK

    /**
     * 是否绘制数值指示器
     */
    var drawValueIndicator: Boolean = false

    /**
     * 指示器图标
     */
    var valueIndicatorDrawable: Drawable? = null

    /**
     * 指示器大小
     */
    var valueIndicatorSize: Int = 10

    /**
     * 绘制线条类型
     */
    @setparam:LineStyle
    var lineStyle: Int = SOLID_LINE


    /**
     * 附加信息
     */
    var data: Any? = null

    @IntDef(DOTTED_LINE, SOLID_LINE)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    private annotation class LineStyle

    companion object {
        /**
         * 虚线
         */
        const val DOTTED_LINE = 0

        /**
         * 实线
         */
        const val SOLID_LINE = 1
    }
}

/**
 * 文本格式化
 */
typealias ValueFormat = (position: Int) -> String
