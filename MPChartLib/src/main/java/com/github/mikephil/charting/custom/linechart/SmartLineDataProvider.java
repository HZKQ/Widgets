package com.github.mikephil.charting.custom.linechart;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.interfaces.dataprovider.BarLineScatterCandleBubbleDataProvider;

/**
 * @author Created by 汪高皖 on 2018/4/13 0013 14:56
 */
public interface SmartLineDataProvider extends BarLineScatterCandleBubbleDataProvider {
    SmartLineData getLineData();
    
    YAxis getAxis(YAxis.AxisDependency dependency);
    
    @Override
    SmartTransformer getTransformer(YAxis.AxisDependency axis);
}
