package com.github.mikephil.charting.custom.linechart;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * @author Created by 汪高皖 on 2018/4/13 0013 15:12
 */
public class SmartTransformer extends Transformer {
    
    public SmartTransformer(ViewPortHandler viewPortHandler){
        super(viewPortHandler);
    }
    
    public float[] generateTransformedValuesLine(ISmartLineDataSet data, float phaseX, float phaseY, int min, int max){
        final int count = ((int) ((max - min) * phaseX) + 1) * 2;
    
        if (valuePointsForGenerateTransformedValuesLine.length != count) {
            valuePointsForGenerateTransformedValuesLine = new float[count];
        }
        float[] valuePoints = valuePointsForGenerateTransformedValuesLine;
    
        for (int j = 0; j < count; j += 2) {
        
            Entry e = data.getEntryForIndex(j / 2 + min);
        
            if (e != null) {
                valuePoints[j] = e.getX();
                valuePoints[j + 1] = e.getY() * phaseY;
            } else {
                valuePoints[j] = 0;
                valuePoints[j + 1] = 0;
            }
        }
    
        getValueToPixelMatrix().mapPoints(valuePoints);
    
        return valuePoints;
    }
}
