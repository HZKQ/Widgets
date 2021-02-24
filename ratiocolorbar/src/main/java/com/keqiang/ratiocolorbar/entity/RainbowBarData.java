package com.keqiang.ratiocolorbar.entity;

/**
 * 彩虹条数据
 *
 * @author Created by 汪高皖 on 2020/1/8 15:05
 */
public class RainbowBarData {
    /**
     * 彩虹条值,如果是连续的数据，此值表示某一段的值。如果是非连续的数据，此值表示彩虹条上距离开始某一点的值
     */
    private float value;
    /**
     * 彩虹条颜色
     */
    private int color;
    
    /**
     * 当前值附加数据
     */
    private Object data;
    
    public float getValue() {
        return value;
    }
    
    public void setValue(float value) {
        this.value = value;
    }
    
    public int getColor() {
        return color;
    }
    
    public void setColor(int color) {
        this.color = color;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
}
