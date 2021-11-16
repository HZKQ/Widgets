package com.github.mikephil.charting.custom.linechart;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;

/**
 * 自定义Chart X轴线
 *
 * @author Created by 汪高皖 on 2018/8/13 0013 10:58
 */
public class SmartXAxis extends XAxis {
    private boolean customCalculateXOffset;
    
    /**
     * X轴Label缩进格式
     */
    protected int mXLabelRetractType = XLabelRetractType.LEFT;
    
    @Override
    public String getFormattedLabel(int index) {
        //FIXME by 汪高皖 时间：2018/8/7 0007 13:29 修改内容：X轴坐标只以x轴数据量多少从0递增，不再计算每一个X轴位置对应的计算值
        if (index < 0 || index >= mEntries.length) {
            return "";
        } else if (customCalculateXOffset) {
            return getValueFormatter().getFormattedValue(index, this);
        } else {
            return getValueFormatter().getFormattedValue(mEntries[index], this);
        }
    }
    
    /**
     * @return {@code true} 此时{@link ValueFormatter#getFormattedValue(float, AxisBase)}中第一个参数为每一个标签的index值(可控)，从0往上递增<br>
     * {@code false} 此时{@link ValueFormatter#getFormattedValue(float, AxisBase)}中第一个参数将是系统根据{@link #setLabelCount(int)}
     * 和{@link #setAxisMaximum(float)} (int)} 以及 {@link #setAxisMinimum(float)} (float)}三者设置的值共同计算出来的间隔值(不可控)
     */
    public boolean isCustomCalculateXOffset() {
        return customCalculateXOffset;
    }
    
    /**
     * @param customCalculateXOffset 是否自己计算X轴每个标签的间隔值<br>
     *                               {@code true} 此时{@link ValueFormatter#getFormattedValue(float, AxisBase)}中第一个参数为每一个标签的index值(可控)，从0往上递增<br>
     *                               {@code false} 此时{@link ValueFormatter#getFormattedValue(float, AxisBase)}中第一个参数将是系统根据{@link #setLabelCount(int)}
     *                               和{@link #setAxisMaximum(float)} (int)} 以及 {@link #setAxisMinimum(float)} (float)}三者设置的值共同计算出来的间隔值(不可控)
     */
    public void setCustomCalculateXOffset(boolean customCalculateXOffset) {
        this.customCalculateXOffset = customCalculateXOffset;
    }
    
    /**
     * 设置x轴边界Label内容缩进类型
     */
    public void setXLabelRetractType(@XLabelRetractType int xLabelRetractType) {
        mXLabelRetractType = xLabelRetractType;
    }
    
    /**
     * @return x轴边界Label内容缩进类型，默认值{@link XLabelRetractType#LEFT}
     */
    public int getXLabelRetractType() {
        return mXLabelRetractType;
    }
    
}