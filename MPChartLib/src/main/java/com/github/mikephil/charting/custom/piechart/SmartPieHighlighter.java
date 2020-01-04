package com.github.mikephil.charting.custom.piechart;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.highlight.PieRadarHighlighter;

/**
 * @author Created by 汪高皖 on 2018/4/17 0017 09:09
 */
public class SmartPieHighlighter extends PieRadarHighlighter<SmartPieChart> {
    
    public SmartPieHighlighter(SmartPieChart chart) {
        super(chart);
    }
    
    @Override
    protected Highlight getClosestHighlight(int index, float x, float y) {
        
        ISmartPieDataSet set = mChart.getData().getDataSet();
        
        final Entry entry = set.getEntryForIndex(index);
        
        return new Highlight(index, entry.getY(), x, y, 0, set.getAxisDependency());
    }
}