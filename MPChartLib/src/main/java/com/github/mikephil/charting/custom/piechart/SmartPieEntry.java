package com.github.mikephil.charting.custom.piechart;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

/**
 * @author Created by 汪高皖 on 2018/4/16 0016 14:49
 */
public class SmartPieEntry extends Entry {
    private String label;
    
    /**
     * 饼状图第二个数据
     */
    private float mYSecondValue;
    
    /**
     * 弧线的宽度
     */
    private float mArcWidth;
    
    @DrawBaseline
    private int mDrawBaseline = DrawBaseline.BOTTOM;
    
    public SmartPieEntry(float value) {
        super(0f, value);
    }
    
    public SmartPieEntry(float value, Object data) {
        super(0f, value, data);
    }
    
    public SmartPieEntry(float value, Drawable icon) {
        super(0f, value, icon);
    }
    
    public SmartPieEntry(float value, Drawable icon, Object data) {
        super(0f, value, icon, data);
    }
    
    public SmartPieEntry(float value, String label) {
        super(0f, value);
        this.label = label;
    }
    
    public SmartPieEntry(float value, String label, Object data) {
        super(0f, value, data);
        this.label = label;
    }
    
    public SmartPieEntry(float value, String label, Drawable icon) {
        super(0f, value, icon);
        this.label = label;
    }
    
    public SmartPieEntry(float value, String label, Drawable icon, Object data) {
        super(0f, value, icon, data);
        this.label = label;
    }
    
    /**
     * This is the same as getY(). Returns the value of the PieEntry.
     */
    public float getValue() {
        return getY();
    }
    
    public float getYSecondValue() {
        return mYSecondValue;
    }
    
    public void setYSecondValue(float YSecondValue) {
        mYSecondValue = YSecondValue;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public float getArcWidth() {
        return mArcWidth;
    }
    
    /**
     * 设置弧线的宽度，只在{@link SmartPieChart#setDrawHoleEnabled(boolean)}为true且{@link SmartPieChart#setHoleRadius(float)}的值>0时此值有效。
     *
     * @param arcWidth <=0，则使用默认的弧线宽度（等于饼状图宽高最小值/2 - holeRadius）
     */
    public void setArcWidth(float arcWidth) {
        this.mArcWidth = arcWidth;
    }
    
    /**
     * @return 参考 {@link DrawBaseline}
     */
    public int getDrawBaseline() {
        return mDrawBaseline;
    }
    
    /**
     * @param drawBaseline 参考 {@link DrawBaseline}
     */
    public void setDrawBaseline(@DrawBaseline int drawBaseline) {
        mDrawBaseline = drawBaseline;
    }
    
    @Deprecated
    @Override
    public void setX(float x) {
        super.setX(x);
        Log.i("DEPRECATED", "Pie entries do not have x values");
    }
    
    @Deprecated
    @Override
    public float getX() {
        Log.i("DEPRECATED", "Pie entries do not have x values");
        return super.getX();
    }
    
    @Override
    public SmartPieEntry copy() {
        SmartPieEntry e = new SmartPieEntry(getY(), label, getData());
        e.setYSecondValue(mYSecondValue);
        return e;
    }
}
