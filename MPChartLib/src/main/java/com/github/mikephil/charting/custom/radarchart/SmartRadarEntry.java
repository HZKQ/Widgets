package com.github.mikephil.charting.custom.radarchart;

import android.annotation.SuppressLint;

import com.github.mikephil.charting.data.Entry;

/**
 * @author Created by wanggaowan on 3/18/21 1:40 PM
 */
@SuppressLint("ParcelCreator")
public class SmartRadarEntry extends Entry {
    
    public SmartRadarEntry(float value) {
        super(0f, value);
    }
    
    public SmartRadarEntry(float value, Object data) {
        super(0f, value, data);
    }
    
    /**
     * This is the same as getY(). Returns the value of the RadarEntry.
     */
    public float getValue() {
        return getY();
    }
    
    public SmartRadarEntry copy() {
        return new SmartRadarEntry(getY(), getData());
    }
    
    /**
     * Radar entries do not have x values
     */
    @Deprecated
    @Override
    public void setX(float x) {
    
    }
    
    /**
     * is always 0, Radar entries do not have x values
     */
    @Deprecated
    @Override
    public float getX() {
        return 0;
    }
}
