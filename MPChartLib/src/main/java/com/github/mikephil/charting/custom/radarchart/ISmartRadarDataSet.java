package com.github.mikephil.charting.custom.radarchart;

import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineRadarDataSet;

import androidx.annotation.Nullable;

/**
 * @author Created by wanggaowan on 3/18/21 1:49 PM
 */
public interface ISmartRadarDataSet extends ILineRadarDataSet<SmartRadarEntry> {
    
    /**
     * flag indicating whether highlight circle should be drawn or not
     */
    boolean isDrawHighlightCircleEnabled();
    
    /**
     * Sets whether highlight circle should be drawn or not
     */
    void setDrawHighlightCircleEnabled(boolean enabled);
    
    int getHighlightCircleFillColor();
    
    /**
     * The stroke color for highlight circle.
     * If Utils.COLOR_NONE, the color of the dataset is taken.
     */
    int getHighlightCircleStrokeColor();
    
    int getHighlightCircleStrokeAlpha();
    
    float getHighlightCircleInnerRadius();
    
    float getHighlightCircleOuterRadius();
    
    float getHighlightCircleStrokeWidth();
    
    /**
     * @deprecated 已过时，始终返回null，请使用 {@link #getSmartValueFormatter()}
     */
    @Deprecated
    @Override
    @Nullable
    ValueFormatter getValueFormatter();
    
    ISmartValueFormatter<SmartRadarEntry> getSmartValueFormatter();
}
