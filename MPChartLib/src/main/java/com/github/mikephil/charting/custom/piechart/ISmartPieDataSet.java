package com.github.mikephil.charting.custom.piechart;

import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;

/**
 * @author Created by 汪高皖 on 2018/4/16 0016 14:29
 */
public interface ISmartPieDataSet extends IDataSet<SmartPieEntry> {
    
    /**
     * Returns the space that is set to be between the piechart-slices of this
     * DataSet, in pixels.
     */
    float getSliceSpace();
    
    /**
     * When enabled, slice spacing will be 0.0 when the smallest value is going to be
     * smaller than the slice spacing itself.
     */
    boolean isAutomaticallyDisableSliceSpacingEnabled();
    
    /**
     * Returns the distance a highlighted piechart slice is "shifted" away from
     * the chart-center in dp.
     */
    float getSelectionShift();
    
    /**
     * x轴值绘制位置
     */
    SmartPieDataSet.ValuePosition getXValuePosition();
    
    /**
     * y轴值绘制位置
     */
    SmartPieDataSet.ValuePosition getYValuePosition();
    
    /**
     * When valuePosition is OutsideSlice, indicates line color
     */
    int getValueLineColor();
    
    /**
     * When valuePosition is OutsideSlice, indicates line width
     */
    float getValueLineWidth();
    
    /**
     * When valuePosition is OutsideSlice, indicates offset as percentage out of the slice size
     */
    float getValueLinePart1OffsetPercentage();
    
    /**
     * When valuePosition is OutsideSlice, indicates length of first half of the line
     */
    float getValueLinePart1Length();
    
    /**
     * When valuePosition is OutsideSlice, indicates length of second half of the line
     */
    float getValueLinePart2Length();
    
    /**
     * When valuePosition is OutsideSlice, this allows variable line length
     */
    boolean isValueLineVariableLength();
    
    /**
     * 获取线条颜色值
     *
     * @param index 当前线条所指内容在饼状图的位置
     */
    int getValueLineColor(int index);
    
    /**
     * 是否绘制线条起始位置圆点，圆点颜色跟随线条颜色
     */
    boolean isDrawDot();
    
    /**
     * 圆点的半径
     */
    float getDotRadius();
    
    /**
     * 是否绘制Y Value下面的值
     */
    boolean isDrawYSecondValue();
    
    /**
     * 第二段线条的长度是否跟随文本的宽度，如果同时绘制顶部和底部文本，则取两个文本中宽度最大值
     */
    boolean isValueLinePart2LengthFollowTextWidth();
    
    @Override
    ValueFormatter getValueFormatter();
    
    SmartPieChartValueFormatter getSmartValueFormatter();
    
    /**
     * 获取文本绘制的位置。具体值参考{@link LabelGravity}
     */
    int getYGravity();
    
    /**
     * 获取第二个值的字体大小
     */
    float getYSecondValueTextSize();
    
    /**
     * 获取第二个值的颜色
     */
    int getSecondValueTextColor();
    
    /**
     * 获取第二个值的颜色
     *
     * @param index 当前绘制值的下标
     */
    int getSecondValueTextColor(int index);
    
    /**
     * 当高亮某一块内容时，是否显示其Value值
     */
    boolean isDrawHighlightYValue();
    
    /**
     * 当高亮某一块内容时，是否显示其Label值
     */
    boolean isDrawHighlightXValue();
    
    /**
     * 如果设置绘制值的位置为{@link SmartPieDataSet.ValuePosition#INSIDE_SLICE}，
     * 此值用于设置离圆心的偏移位置
     *
     * @return 离圆心偏移值0~1(半径的百分比)
     */
    float getValueInSideOffset();
}
