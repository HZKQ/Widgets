package com.github.mikephil.charting.custom.linechart;

import android.graphics.DashPathEffect;

import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineRadarDataSet;

/**
 * @author Created by 汪高皖 on 2018/4/13 0013 14:28
 */
public interface ISmartLineDataSet extends ILineRadarDataSet<SmartLineEntry> {
    
    /**
     * Returns the drawing mode for this line dataset
     *
     * @return
     */
    LineDataSet.Mode getMode();
    
    /**
     * Returns the intensity of the cubic lines (the effect intensity).
     * Max = 1f = very cubic, Min = 0.05f = low cubic effect, Default: 0.2f
     *
     * @return
     */
    float getCubicIntensity();
    
    @Deprecated
    boolean isDrawCubicEnabled();
    
    @Deprecated
    boolean isDrawSteppedEnabled();
    
    /**
     * Returns the size of the drawn circles.
     */
    float getCircleRadius();
    
    /**
     * Returns the hole radius of the drawn circles.
     */
    float getCircleHoleRadius();
    
    /**
     * Returns the color at the given index of the DataSet's circle-color array.
     * Performs a IndexOutOfBounds check by modulus.
     *
     * @param index
     * @return
     */
    int getCircleColor(int index);
    
    /**
     * Returns the number of colors in this DataSet's circle-color array.
     *
     * @return
     */
    int getCircleColorCount();
    
    /**
     * Returns true if drawing circles for this DataSet is enabled, false if not
     *
     * @return
     */
    boolean isDrawCirclesEnabled();
    
    /**
     * Returns the color of the inner circle (the circle-hole).
     *
     * @return
     */
    int getCircleHoleColor();
    
    /**
     * Returns true if drawing the circle-holes is enabled, false if not.
     *
     * @return
     */
    boolean isDrawCircleHoleEnabled();
    
    /**
     * Returns the DashPathEffect that is used for drawing the lines.
     *
     * @return
     */
    DashPathEffect getDashPathEffect();
    
    /**
     * Returns true if the dashed-line effect is enabled, false if not.
     * If the DashPathEffect object is null, also return false here.
     *
     * @return
     */
    boolean isDashedLineEnabled();
    
    /**
     * Returns the IFillFormatter that is set for this DataSet.
     *
     * @return
     */
    ISmartFillFormatter getFillFormatter();
    
    /**
     * @return 返回对应于 {@link #getColors()}中每个颜色在渐变时所占的比重，
     *         只有在 {@link #getColors()}的Size > 1时才生效
     */
    float[] getColorsPercent();
}
