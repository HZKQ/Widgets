package com.github.mikephil.charting.custom.linechart;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * 自定义Chart Y轴线
 * @author Created by 汪高皖 on 2018/8/13 0013 10:58
 */
public class SmartYAxis extends YAxis {
    private boolean customCalculateYOffset;
    
    public SmartYAxis(){
        super();
    }
    
    public SmartYAxis(AxisDependency position){
        super(position);
    }
    
    @Override
    public String getFormattedLabel(int index){
        //FIXME by 汪高皖 时间：2018/8/7 0007 13:29 修改内容：Y轴坐标只以Y轴数据量多少从0递增，不再计算每一个Y轴位置对应的计算值
        if (index < 0 || index >= mEntries.length) {
            return "";
        } else if (customCalculateYOffset){
            return getValueFormatter().getFormattedValue(index, this);
        } else {
            return getValueFormatter().getFormattedValue(mEntries[index], this);
        }
    }
    
    /**
     * @return {@code true} 此时{@link IAxisValueFormatter#getFormattedValue(float, AxisBase)}中第一个参数为每一个标签的index值，从0往上递增<br>
     * {@code false} 此时{@link IAxisValueFormatter#getFormattedValue(float, AxisBase)}中第一个参数将是系统根据{@link #setLabelCount(int)}
     * 和{@link #setAxisMaximum(float)} (int)} 以及 {@link #setAxisMinimum(float)} (float)}三者设置的值共同计算出来的间隔值
     */
    public boolean isCustomCalculateYOffset(){
        return customCalculateYOffset;
    }
    
    /**
     * @param customCalculateYOffset 是否自己计算Y轴每个标签的间隔值<br>
     *                               {@code true} 此时{@link IAxisValueFormatter#getFormattedValue(float, AxisBase)}中第一个参数为每一个标签的index值，从0往上递增<br>
     *                               {@code false} 此时{@link IAxisValueFormatter#getFormattedValue(float, AxisBase)}中第一个参数将是系统根据{@link #setLabelCount(int)}
     *                               和{@link #setAxisMaximum(float)} (int)} 以及 {@link #setAxisMinimum(float)} (float)}三者设置的值共同计算出来的间隔值
     */
    public void setCustomCalculateYOffset(boolean customCalculateYOffset){
        this.customCalculateYOffset = customCalculateYOffset;
    }
}
