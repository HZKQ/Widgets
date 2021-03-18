
package com.github.mikephil.charting.custom.radarchart;

import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

/**
 * 配置雷达图数据
 *
 * @author Created by wanggaowan on 3/18/21 1:51 PM
 */
public class SmartRadarData extends ChartData<ISmartRadarDataSet> {
    
    private List<String> mLabels;
    private int mDrawBgMode = DrawBgMode.POLYGON;
    private boolean mDrawBgRadiusLine = true;
    private boolean mDrawBg = false;
    private int mBgColor = -1;
    private Drawable mBgDrawable;
    
    public SmartRadarData() {
        super();
    }
    
    public SmartRadarData(List<ISmartRadarDataSet> dataSets) {
        super(dataSets);
    }
    
    public SmartRadarData(ISmartRadarDataSet... dataSets) {
        super(dataSets);
    }
    
    /**
     * Sets the labels that should be drawn around the RadarChart at the end of each web line.
     */
    public void setLabels(List<String> labels) {
        this.mLabels = labels;
    }
    
    /**
     * Sets the labels that should be drawn around the RadarChart at the end of each web line.
     */
    public void setLabels(String... labels) {
        this.mLabels = Arrays.asList(labels);
    }
    
    public List<String> getLabels() {
        return mLabels;
    }
    
    @Override
    public Entry getEntryForHighlight(Highlight highlight) {
        return getDataSetByIndex(highlight.getDataSetIndex()).getEntryForIndex((int) highlight.getX());
    }
    
    /**
     * 设置雷达图背景绘制模式
     */
    public void setDrawBgMode(@DrawBgMode int drawBgMode) {
        mDrawBgMode = drawBgMode;
    }
    
    /**
     * 获取雷达图绘制模式，参考{@link DrawBgMode}
     */
    public int getDrawBgMode() {
        return mDrawBgMode;
    }
    
    /**
     * 是否绘制雷达图背景半径线条
     */
    public void setDrawBgRadiusLine(boolean drawBgRadiusLine) {
        mDrawBgRadiusLine = drawBgRadiusLine;
    }
    
    /**
     * 是否绘制雷达图背景半径线条
     */
    public boolean isDrawBgRadiusLine() {
        return mDrawBgRadiusLine;
    }
    
    /**
     * 设置是否绘制雷达图背景，该背景仅为雷达图的背景，非整个控件背景
     */
    public void setDrawBg(boolean drawBg) {
        mDrawBg = drawBg;
    }
    
    /**
     * 是否绘制雷达图背景，该背景仅为雷达图的背景，非整个控件背景
     */
    public boolean isDrawBg() {
        return mDrawBg;
    }
    
    /**
     * 设置雷达图背景颜色，如果设置{@link #mBgDrawable},则优先使用{@link #mBgDrawable}
     */
    public void setBgColor(@ColorInt int bgColor) {
        mBgColor = bgColor;
    }
    
    /**
     * 获取雷达图背景颜色
     */
    public int getBgColor() {
        return mBgColor;
    }
    
    /**
     * 设置雷达图背景图
     */
    public void setBgDrawable(Drawable bgDrawable) {
        mBgDrawable = bgDrawable;
    }
    
    /**
     * 获取雷达图背景图
     */
    @Nullable
    public Drawable getBgDrawable() {
        return mBgDrawable;
    }
}
