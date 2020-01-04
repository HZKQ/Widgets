package com.github.mikephil.charting.custom.linechart;

import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;

import java.util.List;

/**
 * @author Created by 汪高皖 on 2018/4/13 0013 14:39
 */
public class SmartLineData extends BarLineScatterCandleBubbleData<ISmartLineDataSet> {
    public SmartLineData() {
        super();
    }
    
    public SmartLineData(ISmartLineDataSet... dataSets) {
        super(dataSets);
    }
    
    public SmartLineData(List<ISmartLineDataSet> dataSets) {
        super(dataSets);
    }
}
