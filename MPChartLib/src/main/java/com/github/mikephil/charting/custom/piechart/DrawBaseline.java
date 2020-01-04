package com.github.mikephil.charting.custom.piechart;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当自定义圆弧宽度时，用于指导宽度增大/缩小策略。
 * 只在{@link SmartPieChart#setDrawHoleEnabled(boolean)}为true且{@link SmartPieChart#setHoleRadius(float)}的值>0时此值有效。
 *
 * @author Created by 汪高皖 on 2018/12/4 0004 16:35
 */
@IntDef({DrawBaseline.CENTER, DrawBaseline.TOP, DrawBaseline.BOTTOM})
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface DrawBaseline {
    /**
     * 绘制内容以{@link SmartPieChart#getRadius()}作为顶点，向内扩散绘制，但不会超出中心圆
     */
    int TOP = 1;
    
    /**
     * 绘制内容以({@link SmartPieChart#getRadius()} - 中心圆半径) / 2 + 中心圆半径向内外扩散绘制，向内不会超出中心圆，向外不会超出{@link SmartPieChart#getRadius()}
     */
    int CENTER = 0;
    
    /**
     * 绘制内容以{@link SmartPieChart}中心圆作为起始点，向外扩散绘制，但不会超出{@link SmartPieChart#getRadius()}
     */
    int BOTTOM = 2;
}
