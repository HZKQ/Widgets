package com.github.mikephil.charting.custom.linechart;

/**
 * @author Created by 汪高皖 on 2018/4/13 0013 15:02
 */
public interface ISmartFillFormatter {
    /**
     * Returns the vertical (y-axis) position where the filled-line of the
     * LineDataSet should end.
     *
     * @param dataSet the ILineDataSet that is currently drawn
     * @param dataProvider
     * @return
     */
    float getFillLinePosition(ISmartLineDataSet dataSet, SmartLineDataProvider dataProvider);
}
