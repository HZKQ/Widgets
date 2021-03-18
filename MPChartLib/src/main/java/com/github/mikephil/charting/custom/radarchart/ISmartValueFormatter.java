package com.github.mikephil.charting.custom.radarchart;

/**
 * 格式化数据
 *
 * @author Created by 汪高皖 on 2018/4/16 0016 16:05
 */
public interface ISmartValueFormatter<T> {
    String getFormattedValue(T entry);
}
