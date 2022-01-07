package com.keqiang.chart.line

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import com.keqiang.chart.R
import com.keqiang.chart.utils.getCenterBaseline
import com.keqiang.chart.utils.getTextHeight
import me.zhouzhuo810.magpiex.utils.SimpleUtil
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 折线图
 *
 * @author Created by wanggaowan on 2021/11/12 15:03
 */
open class LineChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * x轴
     */
    var xAxis: Axis = Axis.copy()
        private set

    /**
     * y轴
     */
    var yAxis: YAxis = YAxis.copy()
        private set

    /**
     * 如果X与Y轴0值内容、文字大小、文字颜色一致时，是否绘制公共0值，而不单独在x轴、y轴各绘制一个
     */
    var drawPublicZero = true

    var lineDataList: List<LineData>? = null
        private set(value) {
            field = value
            invalidate()
        }

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tempRectF = RectF()
    private val tempPath = Path()
    private val bgPath = Path()

    // 用于虚线的绘制
    private lateinit var dashPathEffect: DashPathEffect

    private inline val Int.px: Float get() = if (isInEditMode) this.toFloat() else SimpleUtil.getScaledValue(this.toFloat())
    private inline val Float.px: Float get() = if (isInEditMode) this else SimpleUtil.getScaledValue(this)

    init {
        xAxis.labelSize(24f.px)
        yAxis.labelSize(24f.px)

        if (isInEditMode) {
            xAxis.valueFormat { if (it == 0) "0" else "中PGpgAB12国NO.1哈哈haha" }
                .labelOffset(10.px)
                .drawGridLine(true)
                .topOffset(30.px)
                .gridUseTopOffset(true)
                .labelAlign(Paint.Align.LEFT)

            yAxis.labelOffset(0.px)
                .drawGridLine(true)
                .topOffset(30.px)
                .gridUseTopOffset(true)
                .valueFormat {
                    when (it) {
                        0 -> "0"
                        1 -> "11"
                        2 -> "222"
                        3 -> "3333"
                        4 -> "44444"
                        5 -> "555555"
                        6 -> "6666666"
                        else -> ""
                    }
                }
                .labelAlign(Paint.Align.CENTER)

            val entityList = mutableListOf<Entity>()
            for (i in 0..xAxis.labelCount) {
                val entity = Entity((Math.random() * 100).toInt().toFloat())
                // entity.drawValue = true
                // entity.drawValueIndicator = true
                if (i == 3) {
                    entity.lineStyle = Entity.DOTTED_LINE
                }
                entity.valueIndicatorDrawable =
                    ContextCompat.getDrawable(context, R.drawable.ic_baseline_grade_24)
                entity.valueIndicatorSize = 30.px
                entityList.add(entity)
            }

            val lineData = LineData(entityList)
            lineData.lineWidth = 2f.px
            lineData.fillBg = true
            lineData.fillBgColor = (0x335283C9).toInt()
            lineData.lineStyle = LineData.LINER

            val addedLines = mutableListOf<AddedLine>()
            val addedLine = AddedLine(80f)
            addedLine.lineWidth = 1f.px
            addedLine.labelTextSize = 50.px
            addedLine.label = "ABCabcPGJpgj中国"
            addedLine.labelGravity = Gravity.TOP or Gravity.RIGHT
            addedLines.add(addedLine)

            val addedLine2 = AddedLine(60f)
            addedLine2.lineWidth = 1f.px
            addedLine2.labelTextSize = 50.px
            addedLine2.label = "ABCabcPGJpgj中国"
            addedLine2.labelGravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            addedLines.add(addedLine2)

            val addedLine3 = AddedLine(4f)
            addedLine3.lineWidth = 1f.px
            addedLine3.labelTextSize = 50.px
            addedLine3.label = "ABCabcPGJpgj中国"
            addedLine3.labelGravity = Gravity.BOTTOM or Gravity.START
            addedLines.add(addedLine3)

            lineData.addedLines = addedLines
            lineDataList = mutableListOf(lineData)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (yAxis.max - yAxis.min <= 0) {
            return
        }

        initAxisLabel(yAxis)
        initAxisLabel(xAxis)

        // 默认顶部和右边加上10px的padding
        val paddingOffset = 10.px

        val yMaxTextWidth = getTextMaxWidth(yAxis)

        val xStart = paddingStart + yMaxTextWidth + getYLabelOffset()
        val xEnd = width - paddingEnd - paddingOffset
        val xBaseline = textPaint.getCenterBaseline(xAxis.labelSize)
        val xTextHeight = textPaint.getTextHeight(xAxis.labelSize)
        val xBottom = height - paddingBottom - getXLabelOffset() - xTextHeight
        val xTop = xBottom - xAxis.lineWidth

        val yTop = paddingTop + paddingOffset

        val xZeroLabel = if (xAxis.labelList.isNullOrEmpty()) "" else xAxis.labelList!![0]
        val yZeroLabel = if (yAxis.labelList.isNullOrEmpty()) "" else yAxis.labelList!![0]
        val jumpZeroLabel =
            drawPublicZero && xZeroLabel == yZeroLabel && xAxis.labelSize == yAxis.labelSize
                && xAxis.labelColor == yAxis.labelColor && yMaxTextWidth > 0

        drawXAxis(canvas, xStart, xEnd, xTop, xBottom, xTextHeight, xBaseline, jumpZeroLabel, xTop - yTop)
        drawYAxis(canvas, xStart, (xStart + yAxis.lineWidth), yTop, xTop, yMaxTextWidth, jumpZeroLabel, xEnd - xStart)
        if (jumpZeroLabel) {
            drawPublicZeroLabel(canvas, xZeroLabel, xStart, xBottom, xBaseline, yMaxTextWidth)
        }

        drawValues(canvas, xStart + yAxis.lineWidth, xEnd - xAxis.topOffset, yTop + yAxis.topOffset, xTop)
    }

    private fun getXLabelOffset(): Float {
        return xAxis.labelOffset ?: 10.px
    }

    private fun getYLabelOffset(): Float {
        return yAxis.labelOffset ?: 10.px
    }

    private fun getXLabelAlign(): Paint.Align {
        return xAxis.labelAlign ?: Paint.Align.CENTER
    }

    private fun getYLabelAlign(): Paint.Align {
        return yAxis.labelAlign ?: Paint.Align.RIGHT
    }

    private fun setPaint(color: Int,
                         strokeWidth: Float = 0f,
                         style: Paint.Style = Paint.Style.STROKE,
                         pathEffect: PathEffect? = null) {
        paint.color = color
        paint.strokeWidth = strokeWidth
        paint.style = style
        paint.pathEffect = pathEffect
    }

    /**
     * 如果X/Y轴0值一致，则绘制公共零轴值
     */
    private fun drawPublicZeroLabel(canvas: Canvas, zeroLabel: String, xStart: Float, xBottom: Float, xBaseline: Float,
                                    yMaxTextWidth: Float) {
        textPaint.textSize = xAxis.labelSize
        textPaint.color = xAxis.labelColor
        val labelAlign = getYLabelAlign()
        textPaint.textAlign = labelAlign
        val textStart = xStart - getYLabelOffset()
        if (labelAlign == Paint.Align.RIGHT) {
            canvas.drawText(zeroLabel, textStart, xBottom + getXLabelOffset() + xBaseline * 2, textPaint)
        } else {
            canvas.save()
            tempRectF.set(textStart - yMaxTextWidth, xBottom, xStart, xBottom + getXLabelOffset() + xBaseline * 2)
            canvas.clipRect(tempRectF)
            val startX = if (labelAlign == Paint.Align.LEFT) {
                tempRectF.left
            } else {
                tempRectF.left + tempRectF.width() / 2f
            }

            canvas.drawText(zeroLabel, startX, tempRectF.bottom, textPaint)
            canvas.restore()
        }
    }

    /**
     * 绘制x轴，[start]、[end]、[top]、[bottom]指定轴线绘制坐标，[jumpZeroLabel]表示是否跳过0值文本的绘制，[yRange]则为y轴线的长度
     */
    private fun drawXAxis(canvas: Canvas,
                          start: Float, end: Float,
                          top: Float, bottom: Float,
                          textHeight: Float, baseline: Float,
                          jumpZeroLabel: Boolean, yRange: Float) {
        // 绘制轴线
        setPaint(xAxis.lineColor, xAxis.lineWidth)
        val y = top + (bottom - top) / 2f
        canvas.drawLine(start, y, end, y, paint)

        var tempStart = start
        val step = (end - start - xAxis.topOffset) / xAxis.labelCount
        textPaint.textSize = xAxis.labelSize
        textPaint.color = xAxis.labelColor
        textPaint.textAlign = getXLabelAlign()
        setPaint(xAxis.gridLineColor, xAxis.gridLineWidth)

        xAxis.labelList?.let {
            val labelOffset = getXLabelOffset()
            val textStep = step - 20.px
            for (index in it.indices) {
                val maxTextWidth = when (index) {
                    0 -> textStep / 2f + start - paddingStart
                    it.size - 1 -> textStep / 2f + xAxis.topOffset
                    else -> textStep
                }

                if (maxTextWidth > 0) {
                    // 绘制x轴文本
                    if (!jumpZeroLabel || index != 0) {
                        val text = it[index]
                        if (text.isNotEmpty()) {
                            val width = textPaint.measureText(text)
                            if (width > maxTextWidth) {
                                canvas.save()

                                when (index) {
                                    // 起始点可以向左延伸
                                    0 -> tempRectF.set(paddingStart.toFloat(), bottom,
                                                       tempStart + textStep / 2f, bottom + labelOffset + textHeight)

                                    // 中间点可绘制宽度固定
                                    it.size - 1 -> tempRectF.set(tempStart - textStep / 2f, bottom,
                                                                 tempStart + xAxis.topOffset, bottom + labelOffset + textHeight)

                                    // 结束点向右延伸
                                    else -> tempRectF.set(tempStart - maxTextWidth / 2f, bottom,
                                                          tempStart + maxTextWidth / 2f, bottom + labelOffset + textHeight)
                                }

                                canvas.clipRect(tempRectF)
                                val startX = when (getXLabelAlign()) {
                                    Paint.Align.CENTER -> tempStart
                                    Paint.Align.LEFT -> tempRectF.left
                                    else -> tempRectF.right
                                }

                                // bottom + labelOffset + baseline,此时文本绘制与bottom + labelOffset坐标水平垂直居中
                                // 而文本要绘制到bottom + labelOffset之下，因此还需加一次baseline
                                canvas.drawText(text, startX, bottom + labelOffset + baseline * 2, textPaint)
                                canvas.restore()
                            } else {
                                val startX = when (getXLabelAlign()) {
                                    Paint.Align.CENTER -> tempStart

                                    Paint.Align.LEFT -> {
                                        when (index) {
                                            0 -> {
                                                if (width > textStep) {
                                                    tempStart + textStep / 2f - width
                                                } else {
                                                    tempStart - width / 2f
                                                }
                                            }

                                            else -> tempStart - width / 2f
                                        }
                                    }

                                    else -> {
                                        when (index) {
                                            it.size - 1 -> {
                                                if (width / 2 > xAxis.topOffset) {
                                                    tempStart + xAxis.topOffset
                                                } else {
                                                    tempStart + width / 2f
                                                }
                                            }

                                            else -> tempStart + width / 2f
                                        }
                                    }
                                }

                                canvas.drawText(text, startX, bottom + labelOffset + baseline * 2, textPaint)
                            }
                        }
                    }
                }

                if (index > 0 && xAxis.drawGridLine) {
                    canvas.drawLine(tempStart,
                                    if (xAxis.gridUseTopOffset) top - yRange + xAxis.topOffset else top - yRange,
                                    tempStart, top, paint)
                }

                tempStart += step
            }
        }
    }

    /**
     * 绘制y轴，[start]、[end]、[top]、[bottom]指定轴线绘制坐标，
     * [maxTextWidth]为y轴最大文本的宽度,[jumpZeroLabel]表示是否跳过0值文本的绘制，[xRange]则为x轴线的长度
     */
    private fun drawYAxis(canvas: Canvas,
                          start: Float, end: Float,
                          top: Float, bottom: Float,
                          maxTextWidth: Float,
                          jumpZeroLabel: Boolean,
                          xRange: Float) {

        // 绘制轴线
        setPaint(yAxis.lineColor, yAxis.lineWidth)
        val x = start + (end - start) / 2f
        canvas.drawLine(x, top, x, bottom, paint)

        // 绘制y轴文本
        var tempBottom = bottom
        val step = (bottom - top - yAxis.topOffset) / yAxis.labelCount.toFloat()
        textPaint.textSize = yAxis.labelSize
        textPaint.color = yAxis.labelColor
        val labelAlign = getYLabelAlign()
        textPaint.textAlign = labelAlign
        setPaint(yAxis.gridLineColor, yAxis.gridLineWidth)

        yAxis.labelList?.let {
            val baseLine = textPaint.getCenterBaseline(yAxis.labelSize)
            val textStart = start - getYLabelOffset()
            for (index in it.indices) {
                if (maxTextWidth > 0) {
                    if (!jumpZeroLabel || index != 0) {
                        val text = it[index]
                        if (text.isNotEmpty()) {
                            if (labelAlign == Paint.Align.RIGHT) {
                                if (index == 0) {
                                    canvas.drawText(text, textStart, tempBottom, textPaint)
                                } else {
                                    canvas.drawText(text, textStart, tempBottom + baseLine, textPaint)
                                }
                            } else {
                                canvas.save()
                                tempRectF.set(textStart - maxTextWidth, top, textStart, bottom)
                                canvas.clipRect(tempRectF)

                                val startX = if (labelAlign == Paint.Align.LEFT) {
                                    tempRectF.left
                                } else {
                                    tempRectF.left + tempRectF.width() / 2f
                                }

                                if (index == 0) {
                                    canvas.drawText(text, startX, tempBottom, textPaint)
                                } else {
                                    canvas.drawText(text, startX, tempBottom + baseLine, textPaint)
                                }

                                canvas.restore()
                            }
                        }
                    }
                }

                if (index > 0 && yAxis.drawGridLine) {
                    var stopX =
                        if (yAxis.gridUseTopOffset) start + xRange - yAxis.topOffset else start + xRange
                    if (xAxis.drawGridLine && index == it.size - 1) {
                        stopX += xAxis.gridLineWidth / 2
                    }

                    canvas.drawLine(start, tempBottom, stopX, tempBottom, paint)
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
            val label = axis.mAxisValueFormat?.invoke(i) ?: Axis.DEFAULT_VALUE_FORMAT.invoke(i)
            labelList.add(label)
        }
        axis.labelList = labelList
    }

    /**
     * 获取轴上文本绘制的最大宽度
     */
    private fun getTextMaxWidth(axis: YAxis): Float {
        if (axis.labelWidth != null) {
            axis.labelWidth?.let {
                return if (it < axis.labelMinWidth) {
                    axis.labelMinWidth
                } else if (axis.labelMaxWidth == null || it < axis.labelMaxWidth!!) {
                    it
                } else {
                    axis.labelMaxWidth!!
                }
            }
        }

        var maxLengthText: String? = null
        axis.labelList?.forEach {
            if (maxLengthText == null || it.length > maxLengthText!!.length) {
                maxLengthText = it
            }
        }

        if (maxLengthText == null) {
            return axis.labelMinWidth.coerceAtLeast(axis.labelMaxWidth ?: 0f)
        }

        textPaint.textSize = axis.labelSize
        val width = textPaint.measureText(maxLengthText!!)
        return if (width < axis.labelMinWidth) {
            axis.labelMinWidth
        } else if (axis.labelMaxWidth == null || width < axis.labelMaxWidth!!) {
            width
        } else {
            axis.labelMaxWidth!!
        }
    }

    /**
     * [end]： 折线图X轴绘制区域结束点，不包括[Axis.topOffset]
     *
     * [top]： 折线图Y轴绘制区域起始点，不包括[Axis.topOffset]
     */
    private fun drawValues(canvas: Canvas,
                           start: Float, end: Float,
                           top: Float, bottom: Float) {

        lineDataList?.forEach {
            if (it.values.size <= 1) {
                if (it.values.size == 1) {
                    getEntityPosition(0, it.values[0], start, end, top, bottom)
                }
            } else {
                when (it.lineStyle) {
                    LineData.CUBIC_BEZIER -> drawCubicBezier(canvas, it, start, end, top, bottom)

                    else -> drawLiner(canvas, it, start, end, top, bottom)
                }
            }

            drawValueTextAndIndicator(canvas, it)
            drawAddedLine(canvas, it, start, end, top, bottom)
        }
    }

    /**
     * 计算[Entity]在坐标系中的坐标位置
     */
    private fun getEntityPosition(
        pos: Int,
        entity: Entity,
        start: Float,
        end: Float,
        top: Float,
        bottom: Float
    ) {
        val x = start + ((end - start) / xAxis.labelCount * pos)
        val y = bottom - ((entity.value - yAxis.min) / (yAxis.max - yAxis.min) * (bottom - top))
        entity.x = x
        entity.y = y
    }

    /**
     * 绘制线性的折线图
     */
    private fun drawLiner(canvas: Canvas, lineData: LineData, start: Float, end: Float, top: Float, bottom: Float) {
        setPaint(lineData.lineColor, lineData.lineWidth)
        tempPath.reset()
        bgPath.reset()

        var lineStyle: Int? = null

        canvas.save()
        canvas.clipRect(start, top, end, bottom)

        for (index in lineData.values.indices) {
            val entity = lineData.values[index]
            getEntityPosition(index, entity, start, end, top, bottom)

            if (entity.isDraw) {
                if (tempPath.isEmpty) {
                    tempPath.moveTo(entity.x, entity.y)
                } else {
                    tempPath.lineTo(entity.x, entity.y)
                }

                if (lineData.fillBg) {
                    if (bgPath.isEmpty) {
                        bgPath.moveTo(entity.x, bottom)
                    }
                    bgPath.lineTo(entity.x, entity.y)
                }

                if (lineStyle != null && lineStyle != entity.lineStyle) {
                    if (!bgPath.isEmpty) {
                        bgPath.lineTo(entity.x, bottom)
                        drawFillBg(canvas, bgPath, lineData, start, end, top, bottom)
                        bgPath.reset()
                        bgPath.moveTo(entity.x, bottom)
                        bgPath.lineTo(entity.x, entity.y)
                    }

                    if (!tempPath.isEmpty) {
                        setPathEffect(lineStyle, paint)
                        canvas.drawPath(tempPath, paint)
                        tempPath.reset()
                        tempPath.moveTo(entity.x, entity.y)
                    }
                }

                lineStyle = entity.lineStyle
            } else {
                if (!bgPath.isEmpty) {
                    if (index > 0) {
                        val preEntity = lineData.values[index - 1]
                        bgPath.lineTo(preEntity.x, bottom)
                        drawFillBg(canvas, bgPath, lineData, start, end, top, bottom)
                        bgPath.reset()
                    }
                }

                if (!tempPath.isEmpty) {
                    setPathEffect(lineStyle, paint)
                    canvas.drawPath(tempPath, paint)
                    tempPath.reset()
                }
            }

            if (index == lineData.values.size - 1 && !bgPath.isEmpty) {
                bgPath.lineTo(entity.x, bottom)
            }
        }

        if (!bgPath.isEmpty) {
            drawFillBg(canvas, bgPath, lineData, start, end, top, bottom)
        }

        if (!tempPath.isEmpty) {
            setPathEffect(lineStyle, paint)
            canvas.drawPath(tempPath, paint)
        }

        canvas.restore()
    }

    /**
     * 绘制三次贝塞尔曲线平滑折线图
     */
    private fun drawCubicBezier(canvas: Canvas, lineData: LineData, start: Float, end: Float, top: Float, bottom: Float) {
        setPaint(lineData.lineColor, lineData.lineWidth)
        tempPath.reset()
        bgPath.reset()

        var lineStyle: Int? = null
        var prevDx: Float
        var prevDy: Float
        var curDx: Float
        var curDy: Float

        var prevPrev: Entity
        var cur: Entity = lineData.values[0]
        var prev: Entity = cur
        var next: Entity
        getEntityPosition(0, lineData.values[0], start, end, top, bottom)
        if (cur.isDraw) {
            tempPath.moveTo(cur.x, cur.y)
            bgPath.moveTo(cur.x, bottom)
            bgPath.lineTo(cur.x, cur.y)
        }

        canvas.save()
        canvas.clipRect(start, top, end, bottom)

        for (index in 1 until lineData.values.size) {
            getEntityPosition(index, lineData.values[index], start, end, top, bottom)

            prevPrev = prev
            prev = cur
            cur = lineData.values[index]
            next = lineData.values[if (index + 1 < lineData.values.size) index + 1 else index]
            if (!next.isDraw) {
                // 下个节点不绘制，那当前节点即为结束点
                next = cur
            }

            if (cur.isDraw) {
                if (tempPath.isEmpty) {
                    tempPath.moveTo(cur.x, cur.y)
                    bgPath.moveTo(cur.x, bottom)
                    bgPath.lineTo(cur.x, cur.y)
                    // 重新已此点作为起始点
                    prev = cur
                } else {
                    prevDx =
                        (getDrawX(cur, start) - getDrawX(prevPrev, start)) * lineData.cubicIntensity
                    prevDy =
                        (getDrawY(cur, bottom) - getDrawY(prevPrev, bottom)) * lineData.cubicIntensity
                    curDx =
                        (getDrawX(next, start) - getDrawX(prev, start)) * lineData.cubicIntensity
                    curDy =
                        (getDrawY(next, bottom) - getDrawY(prev, bottom)) * lineData.cubicIntensity

                    tempPath.cubicTo(getDrawX(prev, start) + prevDx, getDrawY(prev, start) + prevDy,
                                     cur.x - curDx, cur.y - curDy, cur.x, cur.y)

                    bgPath.cubicTo(getDrawX(prev, start) + prevDx, getDrawY(prev, start) + prevDy,
                                   cur.x - curDx, cur.y - curDy, cur.x, cur.y)
                }


                if (lineStyle != null && lineStyle != cur.lineStyle) {
                    if (!bgPath.isEmpty) {
                        bgPath.lineTo(cur.x, bottom)
                        drawFillBg(canvas, bgPath, lineData, start, end, top, bottom)
                        bgPath.reset()
                        bgPath.moveTo(cur.x, bottom)
                        bgPath.lineTo(cur.x, cur.y)
                    }

                    if (!tempPath.isEmpty) {
                        setPathEffect(lineStyle, paint)
                        canvas.drawPath(tempPath, paint)
                        tempPath.reset()
                        tempPath.moveTo(cur.x, cur.y)
                    }
                }

                lineStyle = cur.lineStyle
            } else {
                if (!bgPath.isEmpty) {
                    if (index > 0) {
                        val preEntity = lineData.values[index - 1]
                        bgPath.lineTo(preEntity.x, bottom)
                        drawFillBg(canvas, bgPath, lineData, start, end, top, bottom)
                        bgPath.reset()
                    }
                }

                if (!tempPath.isEmpty) {
                    setPathEffect(lineStyle, paint)
                    canvas.drawPath(tempPath, paint)
                    tempPath.reset()
                }
            }

            if (index == lineData.values.size - 1 && !bgPath.isEmpty) {
                bgPath.lineTo(cur.x, bottom)
            }
        }

        if (!bgPath.isEmpty) {
            drawFillBg(canvas, bgPath, lineData, start, end, top, bottom)
        }

        if (!tempPath.isEmpty) {
            setPathEffect(lineStyle, paint)
            canvas.drawPath(tempPath, paint)
        }

        canvas.restore()
    }

    private fun setPathEffect(lineStyle: Int?, paint: Paint) {
        if (lineStyle == Entity.DOTTED_LINE) {
            if (!this::dashPathEffect.isInitialized) {
                dashPathEffect = DashPathEffect(floatArrayOf(10f.px, 10f.px), 0f)
            }
            paint.pathEffect = dashPathEffect
        } else {
            paint.pathEffect = null
        }
    }

    private fun getDrawX(entity: Entity, def: Float): Float {
        return entity.x
    }

    private fun getDrawY(entity: Entity, def: Float): Float {
        if (entity.isDraw) {
            return entity.y
        }
        return def
    }

    /**
     * 绘制文本值和数值指示器
     */
    private fun drawValueTextAndIndicator(canvas: Canvas, lineData: LineData) {
        textPaint.textAlign = Paint.Align.CENTER
        for (index in lineData.values.indices) {
            val entity = lineData.values[index]
            if (entity.isDraw) {
                var indicatorOffset = 0f
                if (entity.drawValueIndicator && entity.valueIndicatorDrawable != null
                    && entity.valueIndicatorSize > 0) {
                    val half = entity.valueIndicatorSize / 2f
                    entity.valueIndicatorDrawable?.setBounds((entity.x - half).toInt(), (entity.y - half).toInt(),
                                                             (entity.x + half).toInt(), (entity.y + half).toInt())
                    entity.valueIndicatorDrawable?.draw(canvas)
                    indicatorOffset = half
                }

                if (entity.drawValue && entity.valueTextSize > 0) {
                    val text = lineData.valueFormat?.invoke(entity) ?: entity.value.toString()
                    textPaint.textSize = entity.valueTextSize
                    textPaint.color = entity.valueTextColor
                    canvas.drawText(text, entity.x, entity.y - indicatorOffset, textPaint)
                }
            }
        }
    }

    /**
     * 绘制折线背景填充色
     */
    private fun drawFillBg(canvas: Canvas, path: Path, lineData: LineData, start: Float, end: Float, top: Float, bottom: Float) {
        if (lineData.fillBgDrawable != null) {
            lineData.fillBgDrawable?.let {
                it.setBounds(start.toInt(), top.toInt(), end.toInt(), bottom.toInt())

                canvas.save()
                canvas.clipPath(path)
                it.draw(canvas)
                canvas.restore()
            }
        } else if (lineData.fillBgColor != 0) {
            canvas.save()
            canvas.clipPath(path)
            canvas.drawColor(lineData.fillBgColor)
            canvas.restore()
        }
    }

    /**
     * [end]： 折线图X轴绘制区域结束点，不包括[Axis.topOffset]
     *
     * [top]： 折线图Y轴绘制区域起始点，不包括[Axis.topOffset]
     */
    private fun drawAddedLine(
        canvas: Canvas, lineData: LineData,
        start: Float, end: Float, top: Float, bottom: Float
    ) {
        canvas.save()
        tempRectF.set(start, top + yAxis.topOffset, end + xAxis.topOffset, bottom)
        canvas.clipRect(tempRectF)

        lineData.addedLines?.forEach {
            val y = bottom - ((it.value - yAxis.min) / (yAxis.max - yAxis.min) * (bottom - top))
            val stopX = if (it.useTopOffset) end else end + yAxis.topOffset
            setPaint(it.lineColor, it.lineWidth)
            canvas.drawLine(start, y, stopX, y, paint)

            if (it.label.isNullOrEmpty() || it.labelTextSize <= 0) {
                return
            }

            val isHorizontalCenter = it.labelGravity and Gravity.LEFT != Gravity.LEFT
                && it.labelGravity and Gravity.RIGHT != Gravity.RIGHT
                && it.labelGravity and Gravity.CENTER_HORIZONTAL == Gravity.CENTER_HORIZONTAL
            val isEnd = !isHorizontalCenter && (it.labelGravity and Gravity.RIGHT) == Gravity.RIGHT
            var isBottom = (it.labelGravity and Gravity.BOTTOM) == Gravity.BOTTOM

            textPaint.color = it.labelTextColor
            textPaint.textSize = it.labelTextSize
            var textStartX: Float = it.labelXOffset
            textPaint.textAlign = when {
                isHorizontalCenter -> {
                    textStartX += start + (stopX - start) / 2f
                    Paint.Align.CENTER
                }

                isEnd -> {
                    textStartX += stopX
                    Paint.Align.RIGHT
                }

                else -> {
                    textStartX += start
                    Paint.Align.LEFT
                }
            }

            val fm = textPaint.fontMetrics
            val drawTop: Float
            val drawBottom: Float
            if (isBottom) {
                drawTop = y + it.lineWidth / 2f
                drawBottom = drawTop + fm.descent - fm.ascent
            } else {
                drawBottom = y
                drawTop = drawBottom - (fm.descent - fm.ascent)
            }

            it.drawRectF.set(0f, drawTop, 0f, drawBottom)
            if (it.autoVerticalGravity) {
                checkAddLineDrawSpace(true, lineData, it, isBottom, y, drawTop, drawBottom, fm,
                                      top, bottom)
            }

            isBottom = if (it.drawRectF.top != drawTop || it.drawRectF.bottom != drawBottom) {
                !isBottom
            } else {
                isBottom
            }

            val textStartY = if (isBottom) {
                it.drawRectF.top - fm.ascent + it.labelYOffset
            } else {
                it.drawRectF.bottom - fm.descent - it.labelYOffset
            }

            textPaint.textSize = it.labelTextSize
            canvas.drawText(it.label!!, textStartX, textStartY, textPaint)
        }

        canvas.restore()
    }

    /**
     * 检查AddLine绘制空间，执行垂直方向自动排版逻辑。
     *
     * [isBottom]表明原始用户配置附加线文本绘制方向
     *
     * [drawY]为附加线的绘制坐标
     *
     * [drawTop]为附加线文本绘制的顶点，[drawBottom]为附加线文本绘制的底点
     *
     * [lineYTop]为折线图绘制预期Y轴顶点，[lineYBottom]为折线图绘制预期Y轴底点
     */
    private fun checkAddLineDrawSpace(isFirst: Boolean,
                                      lineData: LineData,
                                      addedLine: AddedLine,
                                      isBottom: Boolean,
                                      drawY: Float,
                                      drawTop: Float,
                                      drawBottom: Float,
                                      fm: Paint.FontMetrics,
                                      lineYTop: Float,
                                      lineYBottom: Float) {

        if (lineData.addedLines.isNullOrEmpty()) {
            return
        }

        for (line in lineData.addedLines!!) {
            var posOverlap = false
            if (drawTop < lineYTop || drawBottom > lineYBottom) {
                if (!isFirst) {
                    return
                }
                posOverlap = true
            } else {
                if (line == addedLine) {
                    break
                }

                if (!(drawTop >= line.drawRectF.bottom || drawBottom <= line.drawRectF.top)) {
                    if (!isFirst) {
                        return
                    }
                    posOverlap = true
                }
            }

            if (posOverlap) {
                // 调转方向
                val top: Float
                val bottom: Float
                if (!isBottom) {
                    top = drawY + addedLine.lineWidth / 2f
                    bottom = drawTop + fm.descent - fm.ascent
                } else {
                    bottom = drawY
                    top = drawBottom - (fm.descent - fm.ascent)
                }
                checkAddLineDrawSpace(false, lineData, addedLine, isBottom, drawY, top, bottom, fm,
                                      lineYTop, lineYBottom)

                return
            }
        }

        addedLine.drawRectF.set(0f, drawTop, 0f, drawBottom)
    }
}

/**
 * 配置X、Y轴值内容
 */
open class Axis internal constructor() {
    var lineColor: Int = Color.GRAY
        private set
    var lineWidth: Float = 1f
        private set
    var topOffset: Float = 0f
        private set

    var labelCount: Int = 5
        private set
    var labelSize: Float = 24f
        private set
    var labelColor: Int = Color.BLACK
        private set
    var labelOffset: Float? = null
        private set
    var mAxisValueFormat: AxisValueFormat? = DEFAULT_VALUE_FORMAT
        private set
    var labelAlign: Paint.Align? = null
        private set

    var drawGridLine: Boolean = false
        private set
    var gridLineColor: Int = Color.GRAY
        private set
    var gridLineWidth: Float = 1f
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
    fun lineWidth(@Px width: Float): Axis {
        lineWidth = width
        return this
    }

    /**
     * X/Y轴顶部绘制偏移值，比如控件高度100，设置offset为10，那么Y轴高度为100，折线图实际可用绘制高度90
     */
    fun topOffset(@Px offset: Float): Axis {
        topOffset = offset
        return this
    }

    /**
     * 轴上标签数量，不包含0值坐标，比如设置X轴最小最大值为0、100，labelCount设置为5，
     * 则X轴标签值为0，20，40，60，80，100
     */
    fun labelCount(@androidx.annotation.IntRange(from = 1) count: Int): Axis {
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
     * 标签绘制距离轴线偏移值
     */
    fun labelOffset(@Px @FloatRange(from = 0.0) offset: Float): Axis {
        if (offset < 0) {
            return this
        }

        this.labelOffset = offset
        return this
    }

    /**
     * 标签值格式化
     */
    fun valueFormat(axisValueFormat: AxisValueFormat?): Axis {
        if (this.mAxisValueFormat == axisValueFormat) {
            return this
        }

        this.mAxisValueFormat = axisValueFormat
        labelList = null
        return this
    }

    /**
     * 标签对齐方式
     */
    fun labelAlign(labelAlign: Paint.Align): Axis {
        if (this.labelAlign == labelAlign) {
            return this
        }

        this.labelAlign = labelAlign
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
    fun gridLineWidth(@Px @FloatRange(from = 0.0) width: Float): Axis {
        if (width < 1f) {
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
    open fun apply(axis: Axis) {
        this.lineColor = axis.lineColor
        this.lineWidth = axis.lineWidth
        this.topOffset = axis.topOffset

        this.labelCount = axis.labelCount
        this.labelSize = axis.labelSize
        this.labelColor = axis.labelColor
        this.labelOffset = axis.labelOffset
        this.mAxisValueFormat = axis.mAxisValueFormat
        this.labelAlign = axis.labelAlign

        this.drawGridLine = axis.drawGridLine
        this.gridLineColor = axis.gridLineColor
        this.gridLineWidth = axis.gridLineWidth
        this.gridUseTopOffset = axis.gridUseTopOffset
    }

    companion object {
        fun copy(): Axis = Axis()

        val DEFAULT_VALUE_FORMAT = object : AxisValueFormat {
            override fun invoke(position: Int): String {
                return position.toString()
            }
        }
    }
}

/**
 * 配置X、Y轴值内容
 */
open class YAxis internal constructor() : Axis() {
    var min: Float = 0f
        private set
    var max: Float = 100f
        private set

    var labelWidth: Float? = null
        private set
    var labelMinWidth: Float = 0f
        private set
    var labelMaxWidth: Float? = null
        private set

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
     * 标签绘制宽度，超出此宽度则裁剪标签.设置为null则根据文本宽度及[labelMaxWidth]自适应
     */
    fun labelWidth(@Px @FloatRange(from = 0.0) width: Float?): Axis {
        if (width != null && width < 0) {
            return this
        }

        this.labelWidth = width
        return this
    }

    /**
     * 标签绘制最小宽度
     */
    fun labelMinWidth(@Px @FloatRange(from = 0.0) min: Float): Axis {
        if (min < 0) {
            return this
        }

        this.labelMinWidth = min
        return this
    }

    /**
     * 标签绘制最大宽度，超出此值则裁剪标签，设置为null则无限制
     */
    fun labelMaxWidth(@Px @FloatRange(from = 0.0) max: Float?): Axis {
        if (max != null && max < 0) {
            return this
        }

        this.labelMaxWidth = max
        return this
    }

    override fun apply(axis: Axis) {
        super.apply(axis)
        if (axis is YAxis) {
            this.min = axis.min
            this.max = axis.max
            this.labelWidth = axis.labelWidth
            this.labelMinWidth = axis.labelMinWidth
            this.labelMaxWidth = axis.labelMaxWidth
        }
    }

    companion object {
        fun copy(): YAxis = YAxis()
    }
}

open class LineData(
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
     * 如果[lineStyle]是[CUBIC_BEZIER],则此值指定贝塞尔曲线的平滑度
     */
    var cubicIntensity by DefValueDelegate {
        when (lineStyle) {
            CUBIC_BEZIER -> 0.1f
            else -> 0f
        }
    }

    /**
     * 绘制折线宽度
     */
    @setparam:Px
    @setparam:FloatRange(from = 1.0)
    var lineWidth: Float = 1f

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
     * 填充的背景颜色，如果设置了此值也设置了[fillBgDrawable],则优先使用[fillBgDrawable]。
     */
    var fillBgColor: Int = 0

    /**
     * 填充的背景图像
     */
    var fillBgDrawable: Drawable? = null

    /**
     * [Entity] value值格式化文本
     */
    var valueFormat: ValueFormat? = null

    var addedLines: List<AddedLine>? = null

    @IntDef(LINER, CUBIC_BEZIER)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    private annotation class LineStyle

    companion object {
        const val LINER = 1
        const val CUBIC_BEZIER = 2
    }
}

open class Entity(var value: Float = 0f) {
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
    @Px
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
    @Px
    var valueIndicatorSize: Float = 10f

    /**
     * 绘制线条类型
     */
    @setparam:LineStyle
    var lineStyle: Int = SOLID_LINE

    /**
     * 附加信息
     */
    var data: Any? = null

    /**
     * 绘制时的x坐标
     */
    @Px
    var x: Float = 0f
        internal set

    /**
     * 绘制时的y坐标
     */
    @Px
    var y: Float = 0f
        internal set

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
 * 在折线图中显示在y轴上标记特定值的附加线
 */
open class AddedLine(var value: Float) {
    /**
     * 附加线颜色
     */
    @ColorInt
    var lineColor: Int = Color.GREEN

    /**
     * 附加线宽度
     */
    @Px
    var lineWidth: Float = 1f

    /**
     * 附加线文本
     */
    var label: String? = null

    /**
     * 附加线文本字体大小
     */
    @Px
    var labelTextSize = 24f

    /**
     * 附加线文本字体颜色
     */
    @ColorInt
    var labelTextColor: Int = Color.BLACK

    /**
     * 附加线文本绘制位置，以附加线作为基准.垂直方向只能基于附加线之上和之下，
     * 如果设置为[Gravity.CENTER_VERTICAL],则当做[Gravity.TOP]处理
     */
    var labelGravity: Int = Gravity.TOP or Gravity.RIGHT

    /**
     * 垂直方向是否自动布局，如：设置绘制位置为Gravity.TOP，但是发现上方没有足够空间，
     * 此时下方有足够可用控件，则以Gravity.bottom绘制
     */
    var autoVerticalGravity: Boolean = true

    /**
     * 标签X轴偏移值,>0:向右偏移，<0:向左偏移
     */
    var labelXOffset: Float = 0f

    /**
     * 标签Y轴偏移值，以附加线作为基准点，>0:远离基准线，<0:靠近基准线
     */
    var labelYOffset: Float = 0f

    /**
     * [Axis.topOffset]属性是否作用于附加线，如果此值为true,那么附加线绘制长度<=x轴轴线长度
     */
    var useTopOffset: Boolean = true

    /**
     * 增值线绘制的矩形区域
     */
    internal var drawRectF: RectF = RectF()
}

/**
 * 对一个不为空的数值进行代理，指定一个默认值
 */
private class DefValueDelegate<T>(val def: () -> T) : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value == null) {
            return def.invoke()
        }
        return value!!
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

/**
 * 文本格式化
 */
typealias AxisValueFormat = (position: Int) -> String

/**
 * 文本格式化
 */
typealias ValueFormat = (entity: Entity) -> String
