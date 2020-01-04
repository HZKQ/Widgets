package com.github.mikephil.charting.custom.piechart;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当{@link SmartPieDataSet#setYValuePosition(SmartPieDataSet.ValuePosition)} ==
 * {@linkplain com.github.mikephil.charting.data.PieDataSet.ValuePosition#OUTSIDE_SLICE ValuePosition.OUTSIDE_SLICE}时，
 * 用于标识文本所绘制的位置
 *
 * @author Created by 汪高皖 on 2018/12/4 0004 16:35
 */
@IntDef({LabelGravity.END, LabelGravity.TOP, LabelGravity.BOTTOM})
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface LabelGravity {
    /**
     * 绘制到线条尾部，垂直方向上字体中间和线条持平,此时{@link SmartPieDataSet#isDrawYSecondValue()}不管什么值都不生效
     */
    int END = 0;
    
    /**
     * 绘制到线条上面，字体水平方向从线条尾部向圆心方向绘制，如果{@link SmartPieDataSet#isDrawYSecondValue()}为true，
     * 此时{@link SmartPieEntry#getY()}绘制在顶部，{@link SmartPieEntry#getYSecondValue()}的值绘制在底部
     */
    int TOP = 1;
    
    /**
     * 绘制到线条下面，字体水平方向从线条尾部向圆心方向绘制，如果{@link SmartPieDataSet#isDrawYSecondValue()}为true，
     * 此时{@link SmartPieEntry#getY()}绘制在底部，{@link SmartPieEntry#getYSecondValue()}的值绘制在顶部
     */
    int BOTTOM = 2;
}
