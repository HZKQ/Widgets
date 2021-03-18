
package com.github.mikephil.charting.custom.radarchart;

import android.graphics.Color;

import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.LineRadarDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class SmartRadarDataSet extends LineRadarDataSet<SmartRadarEntry> implements ISmartRadarDataSet {
    
    private static final ISmartValueFormatter<SmartRadarEntry> DEFAULT_VALUE_FORMAT
        = new ISmartValueFormatter<SmartRadarEntry>() {
        @Override
        public String getFormattedValue(SmartRadarEntry entry) {
            return String.valueOf(entry.getValue());
        }
    };
    
    /// flag indicating whether highlight circle should be drawn or not
    protected boolean mDrawHighlightCircleEnabled = false;
    
    protected int mHighlightCircleFillColor = Color.WHITE;
    
    /// The stroke color for highlight circle.
    /// If Utils.COLOR_NONE, the color of the dataset is taken.
    protected int mHighlightCircleStrokeColor = ColorTemplate.COLOR_NONE;
    
    protected int mHighlightCircleStrokeAlpha = (int) (0.3 * 255);
    protected float mHighlightCircleInnerRadius = 3.0f;
    protected float mHighlightCircleOuterRadius = 4.0f;
    protected float mHighlightCircleStrokeWidth = 2.0f;
    
    private ISmartValueFormatter<SmartRadarEntry> mSmartValueFormatter;
    
    public SmartRadarDataSet(List<SmartRadarEntry> yVals, String label) {
        super(yVals, label);
    }
    
    /// Returns true if highlight circle should be drawn, false if not
    @Override
    public boolean isDrawHighlightCircleEnabled() {
        return mDrawHighlightCircleEnabled;
    }
    
    /// Sets whether highlight circle should be drawn or not
    @Override
    public void setDrawHighlightCircleEnabled(boolean enabled) {
        mDrawHighlightCircleEnabled = enabled;
    }
    
    @Override
    public int getHighlightCircleFillColor() {
        return mHighlightCircleFillColor;
    }
    
    public void setHighlightCircleFillColor(int color) {
        mHighlightCircleFillColor = color;
    }
    
    /// Returns the stroke color for highlight circle.
    /// If Utils.COLOR_NONE, the color of the dataset is taken.
    @Override
    public int getHighlightCircleStrokeColor() {
        return mHighlightCircleStrokeColor;
    }
    
    /// Sets the stroke color for highlight circle.
    /// Set to Utils.COLOR_NONE in order to use the color of the dataset;
    public void setHighlightCircleStrokeColor(int color) {
        mHighlightCircleStrokeColor = color;
    }
    
    @Override
    public int getHighlightCircleStrokeAlpha() {
        return mHighlightCircleStrokeAlpha;
    }
    
    public void setHighlightCircleStrokeAlpha(int alpha) {
        mHighlightCircleStrokeAlpha = alpha;
    }
    
    @Override
    public float getHighlightCircleInnerRadius() {
        return mHighlightCircleInnerRadius;
    }
    
    public void setHighlightCircleInnerRadius(float radius) {
        mHighlightCircleInnerRadius = radius;
    }
    
    @Override
    public float getHighlightCircleOuterRadius() {
        return mHighlightCircleOuterRadius;
    }
    
    public void setHighlightCircleOuterRadius(float radius) {
        mHighlightCircleOuterRadius = radius;
    }
    
    @Override
    public float getHighlightCircleStrokeWidth() {
        return mHighlightCircleStrokeWidth;
    }
    
    /**
     * @deprecated 已过时，设置已无效，请使用 {@link #setSmartValueFormatter(ISmartValueFormatter)}
     */
    @Deprecated
    @Override
    public void setValueFormatter(ValueFormatter f) {
    
    }
    
    @Override
    @Nullable
    public ValueFormatter getValueFormatter() {
        return null;
    }
    
    public void setSmartValueFormatter(ISmartValueFormatter<SmartRadarEntry> smartValueFormatter) {
        mSmartValueFormatter = smartValueFormatter;
    }
    
    @Override
    public ISmartValueFormatter<SmartRadarEntry> getSmartValueFormatter() {
        if (mSmartValueFormatter == null) {
            return DEFAULT_VALUE_FORMAT;
        }
        return mSmartValueFormatter;
    }
    
    public void setHighlightCircleStrokeWidth(float strokeWidth) {
        mHighlightCircleStrokeWidth = strokeWidth;
    }
    
    @Override
    public DataSet<SmartRadarEntry> copy() {
        List<SmartRadarEntry> entries = new ArrayList<SmartRadarEntry>();
        for (int i = 0; i < mValues.size(); i++) {
            entries.add(mValues.get(i).copy());
        }
        SmartRadarDataSet copied = new SmartRadarDataSet(entries, getLabel());
        copy(copied);
        return copied;
    }
    
    protected void copy(SmartRadarDataSet radarDataSet) {
        super.copy(radarDataSet);
        radarDataSet.mDrawHighlightCircleEnabled = mDrawHighlightCircleEnabled;
        radarDataSet.mHighlightCircleFillColor = mHighlightCircleFillColor;
        radarDataSet.mHighlightCircleInnerRadius = mHighlightCircleInnerRadius;
        radarDataSet.mHighlightCircleStrokeAlpha = mHighlightCircleStrokeAlpha;
        radarDataSet.mHighlightCircleStrokeColor = mHighlightCircleStrokeColor;
        radarDataSet.mHighlightCircleStrokeWidth = mHighlightCircleStrokeWidth;
        radarDataSet.mSmartValueFormatter = mSmartValueFormatter;
    }
}
