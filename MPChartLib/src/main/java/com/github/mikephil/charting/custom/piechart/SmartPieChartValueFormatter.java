package com.github.mikephil.charting.custom.piechart;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * @author Created by 汪高皖 on 2018/4/16 0016 16:05
 */
public abstract class SmartPieChartValueFormatter extends ValueFormatter {
    public String getFormattedSecondValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return getFormattedValue(value);
    }
}
