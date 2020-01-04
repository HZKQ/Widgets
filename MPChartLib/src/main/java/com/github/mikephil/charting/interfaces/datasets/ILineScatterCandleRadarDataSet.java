package com.github.mikephil.charting.interfaces.datasets;

import android.graphics.DashPathEffect;

import com.github.mikephil.charting.data.Entry;

/**
 * Created by Philipp Jahoda on 21/10/15.
 */
public interface ILineScatterCandleRadarDataSet<T extends Entry> extends IBarLineScatterCandleBubbleDataSet<T> {
    
    /**
     * Returns true if vertical highlight indicator lines are enabled (drawn)
     */
    boolean isVerticalHighlightIndicatorEnabled();
    
    /**
     * 垂直线绘制时是否超出刻度点垂直结束位置，目前只针对x轴位置在底部
     */
    boolean isOverVerticalHighlightIndicatorEnd();
    
    /**
     * Returns true if vertical highlight indicator lines are enabled (drawn)
     */
    boolean isHorizontalHighlightIndicatorEnabled();
    
    /**
     * 水平线绘制时是否超出刻度点水平结束位置,目前只针对y轴位置在左边
     */
    boolean isOverHorizontalHighlightIndicatorEnd();
    
    /**
     * Returns the line-width in which highlight lines are to be drawn.
     */
    float getHighlightLineWidth();
    
    /**
     * Returns the DashPathEffect that is used for highlighting.
     */
    DashPathEffect getDashPathEffectHighlight();
}
