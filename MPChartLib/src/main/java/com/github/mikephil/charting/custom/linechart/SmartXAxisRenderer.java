package com.github.mikephil.charting.custom.linechart;

import android.graphics.Canvas;

import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * 自定义chart X轴绘制类
 *
 * @author Created by 汪高皖 on 2018/8/13 0013 11:00
 */
public class SmartXAxisRenderer extends XAxisRenderer {
    private SmartXAxis mSmartXAxis;
    
    public SmartXAxisRenderer(ViewPortHandler viewPortHandler, SmartXAxis xAxis, Transformer trans) {
        super(viewPortHandler, xAxis, trans);
        mSmartXAxis = xAxis;
    }
    
    @Override
    protected void drawLabels(Canvas c, float pos, MPPointF anchor) {
        final float labelRotationAngleDegrees = mXAxis.getLabelRotationAngle();
        boolean centeringEnabled = mXAxis.isCenterAxisLabelsEnabled();
        
        float[] positions = new float[mXAxis.mEntryCount * 2];
        
        for (int i = 0; i < positions.length; i += 2) {
            
            // only fill x values
            if (centeringEnabled) {
                positions[i] = mXAxis.mCenteredEntries[i / 2];
            } else {
                positions[i] = mXAxis.mEntries[i / 2];
            }
        }
        
        mTrans.pointValuesToPixel(positions);
        
        for (int i = 0; i < positions.length; i += 2) {
            
            float x = positions[i];
            
            if (mViewPortHandler.isInBoundsX(x)) {
                
                String label = mXAxis.getValueFormatter().getFormattedValue(mXAxis.mEntries[i / 2], mXAxis);
                
                if (mXAxis.isAvoidFirstLastClippingEnabled()) {
                    
                    // avoid clipping of the last mXAxis.mEntryCount-1为x轴标签数
                    if (i == mXAxis.mEntryCount - 1 && mXAxis.mEntryCount > 1) {
                        float width = Utils.calcTextWidth(mAxisLabelPaint, label);
                        
                        if (width > mViewPortHandler.offsetRight() * 2
                            && x + width > mViewPortHandler.getChartWidth()) {
                            x -= width / 2;
                        }
                        // avoid clipping of the first
                    } else if (i == 0) {
                        if (mSmartXAxis.getXLabelRetractType() == XLabelRetractType.LEFT
                            || mSmartXAxis.getXLabelRetractType() == XLabelRetractType.BOTH) {
                            float width = Utils.calcTextWidth(mAxisLabelPaint, label);
                            x += width / 2;
                        }
                    } else if (i == (mXAxis.mEntryCount - 1) * 2) {
                        if (mSmartXAxis.getXLabelRetractType() == XLabelRetractType.RIGHT
                            || mSmartXAxis.getXLabelRetractType() == XLabelRetractType.BOTH) {
                            //重写该方法 添加的代码，x轴最后一个标签缩进
                            float width = Utils.calcTextWidth(mAxisLabelPaint, label);
                            x -= width / 2;
                        }
                    }
                }
                drawLabel(c, label, x, pos, anchor, labelRotationAngleDegrees);
            }
        }
    }
}
