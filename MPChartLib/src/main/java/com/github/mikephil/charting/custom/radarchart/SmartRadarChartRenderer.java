package com.github.mikephil.charting.custom.radarchart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.renderer.LineRadarRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class SmartRadarChartRenderer extends LineRadarRenderer {
    
    protected SmartRadarChart mChart;
    
    /**
     * paint for drawing the web
     */
    protected Paint mWebPaint;
    protected Paint mHighlightCirclePaint;
    
    public SmartRadarChartRenderer(SmartRadarChart chart, ChartAnimator animator,
                                   ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        mChart = chart;
        
        mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightPaint.setStyle(Paint.Style.STROKE);
        mHighlightPaint.setStrokeWidth(2f);
        mHighlightPaint.setColor(Color.rgb(255, 187, 115));
        
        mWebPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWebPaint.setStyle(Paint.Style.STROKE);
        
        mHighlightCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }
    
    public Paint getWebPaint() {
        return mWebPaint;
    }
    
    @Override
    public void initBuffers() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void drawData(Canvas c) {
        
        SmartRadarData radarData = mChart.getData();
        
        int mostEntries = radarData.getMaxEntryCountSet().getEntryCount();
        
        for (ISmartRadarDataSet set : radarData.getDataSets()) {
            
            if (set.isVisible()) {
                drawDataSet(c, set, mostEntries);
            }
        }
    }
    
    protected Path mDrawDataSetSurfacePathBuffer = new Path();
    
    /**
     * Draws the RadarDataSet
     *
     * @param mostEntries the entry count of the dataset with the most entries
     */
    protected void drawDataSet(Canvas c, ISmartRadarDataSet dataSet, int mostEntries) {
        
        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();
        
        float sliceangle = mChart.getSliceAngle();
        
        // calculate the factor that is needed for transforming the value to
        // pixels
        float factor = mChart.getFactor();
        
        MPPointF center = mChart.getCenterOffsets();
        MPPointF pOut = MPPointF.getInstance(0, 0);
        Path surface = mDrawDataSetSurfacePathBuffer;
        surface.reset();
        
        boolean hasMovedToPoint = false;
        
        for (int j = 0; j < dataSet.getEntryCount(); j++) {
            
            mRenderPaint.setColor(dataSet.getColor(j));
            
            SmartRadarEntry e = dataSet.getEntryForIndex(j);
            
            Utils.getPosition(
                center,
                (e.getY() - mChart.getYChartMin()) * factor * phaseY,
                sliceangle * j * phaseX + mChart.getRotationAngle(), pOut);
            
            if (Float.isNaN(pOut.x))
                continue;
            
            if (!hasMovedToPoint) {
                surface.moveTo(pOut.x, pOut.y);
                hasMovedToPoint = true;
            } else
                surface.lineTo(pOut.x, pOut.y);
        }
        
        if (dataSet.getEntryCount() > mostEntries) {
            // if this is not the largest set, draw a line to the center before closing
            surface.lineTo(center.x, center.y);
        }
        
        surface.close();
        
        if (dataSet.isDrawFilledEnabled()) {
            
            final Drawable drawable = dataSet.getFillDrawable();
            if (drawable != null) {
                
                drawFilledPath(c, surface, drawable);
            } else {
                
                drawFilledPath(c, surface, dataSet.getFillColor(), dataSet.getFillAlpha());
            }
        }
        
        mRenderPaint.setStrokeWidth(dataSet.getLineWidth());
        mRenderPaint.setStyle(Paint.Style.STROKE);
        
        // draw the line (only if filled is disabled or alpha is below 255)
        if (!dataSet.isDrawFilledEnabled() || dataSet.getFillAlpha() < 255)
            c.drawPath(surface, mRenderPaint);
        
        MPPointF.recycleInstance(center);
        MPPointF.recycleInstance(pOut);
    }
    
    @Override
    public void drawValues(Canvas c) {
        
        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();
        
        float sliceangle = mChart.getSliceAngle();
        
        // calculate the factor that is needed for transforming the value to
        // pixels
        float factor = mChart.getFactor();
        
        MPPointF center = mChart.getCenterOffsets();
        MPPointF pOut = MPPointF.getInstance(0, 0);
        MPPointF pIcon = MPPointF.getInstance(0, 0);
        
        float yoffset = Utils.convertDpToPixel(5f);
        
        for (int i = 0; i < mChart.getData().getDataSetCount(); i++) {
            
            ISmartRadarDataSet dataSet = mChart.getData().getDataSetByIndex(i);
            
            if (!shouldDrawValues(dataSet))
                continue;
            
            // apply the text-styling defined by the DataSet
            applyValueTextStyle(dataSet);
            
            ISmartValueFormatter<SmartRadarEntry> formatter = dataSet.getSmartValueFormatter();
            
            MPPointF iconsOffset = MPPointF.getInstance(dataSet.getIconsOffset());
            iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x);
            iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y);
            
            for (int j = 0; j < dataSet.getEntryCount(); j++) {
                
                SmartRadarEntry entry = dataSet.getEntryForIndex(j);
                
                Utils.getPosition(
                    center,
                    (entry.getY() - mChart.getYChartMin()) * factor * phaseY,
                    sliceangle * j * phaseX + mChart.getRotationAngle(),
                    pOut);
                
                if (dataSet.isDrawValuesEnabled()) {
                    drawValue(c, formatter.getFormattedValue(entry), pOut.x, pOut.y - yoffset, dataSet.getValueTextColor(j));
                }
                
                if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {
                    
                    Drawable icon = entry.getIcon();
                    
                    Utils.getPosition(
                        center,
                        (entry.getY()) * factor * phaseY + iconsOffset.y,
                        sliceangle * j * phaseX + mChart.getRotationAngle(),
                        pIcon);
                    
                    //noinspection SuspiciousNameCombination
                    pIcon.y += iconsOffset.x;
                    
                    Utils.drawImage(
                        c,
                        icon,
                        (int) pIcon.x,
                        (int) pIcon.y,
                        icon.getIntrinsicWidth(),
                        icon.getIntrinsicHeight());
                }
            }
            
            MPPointF.recycleInstance(iconsOffset);
        }
        
        MPPointF.recycleInstance(center);
        MPPointF.recycleInstance(pOut);
        MPPointF.recycleInstance(pIcon);
    }
    
    @Override
    public void drawValue(Canvas c, String valueText, float x, float y, int color) {
        mValuePaint.setColor(color);
        c.drawText(valueText, x, y, mValuePaint);
    }
    
    @Override
    public void drawExtras(Canvas c) {
        drawWeb(c);
    }
    
    /**
     * 绘制雷达图背景
     */
    protected void drawWeb(Canvas c) {
        
        float sliceangle = mChart.getSliceAngle();
        
        // calculate the factor that is needed for transforming the value to
        // pixels
        float factor = mChart.getFactor();
        float rotationangle = mChart.getRotationAngle();
        
        MPPointF center = mChart.getCenterOffsets();
        
        SmartRadarData data = mChart.getData();
        int labelCount = mChart.getYAxis().mEntryCount;
        drawWebBg(c, data, labelCount, factor, center, sliceangle, rotationangle);
        
        // draw the web lines that come from the center
        mWebPaint.setStrokeWidth(mChart.getWebLineWidth());
        mWebPaint.setColor(mChart.getWebColor());
        mWebPaint.setAlpha(mChart.getWebAlpha());
        
        if (data.isDrawBgRadiusLine()) {
            final int xIncrements = 1 + mChart.getSkipWebLineCount();
            int maxEntryCount = mChart.getData().getMaxEntryCountSet().getEntryCount();
            MPPointF p = MPPointF.getInstance(0, 0);
            for (int i = 0; i < maxEntryCount; i += xIncrements) {
                
                Utils.getPosition(
                    center,
                    mChart.getYRange() * factor,
                    sliceangle * i + rotationangle,
                    p);
                
                c.drawLine(center.x, center.y, p.x, p.y, mWebPaint);
            }
            MPPointF.recycleInstance(p);
        }
        
        // draw the inner-web
        mWebPaint.setStrokeWidth(mChart.getWebLineWidthInner());
        mWebPaint.setColor(mChart.getWebColorInner());
        mWebPaint.setAlpha(mChart.getWebAlpha());
        
        if (data.getDrawBgMode() == DrawBgMode.POLYGON) {
            MPPointF p1out = MPPointF.getInstance(0, 0);
            MPPointF p2out = MPPointF.getInstance(0, 0);
            
            for (int j = 0; j < labelCount; j++) {
                
                for (int i = 0; i < mChart.getData().getEntryCount(); i++) {
                    
                    float r = (mChart.getYAxis().mEntries[j] - mChart.getYChartMin()) * factor;
                    
                    Utils.getPosition(center, r, sliceangle * i + rotationangle, p1out);
                    Utils.getPosition(center, r, sliceangle * (i + 1) + rotationangle, p2out);
                    
                    c.drawLine(p1out.x, p1out.y, p2out.x, p2out.y, mWebPaint);
                }
            }
            MPPointF.recycleInstance(p1out);
            MPPointF.recycleInstance(p2out);
        } else {
            for (int j = 0; j < labelCount; j++) {
                float r = (mChart.getYAxis().mEntries[j] - mChart.getYChartMin()) * factor;
                c.drawCircle(center.x, center.y, r, mWebPaint);
            }
        }
    }
    
    private void drawWebBg(Canvas c, SmartRadarData data, int labelCount, float factor,
                           MPPointF center, float sliceangle, float rotationangle) {
        if (!data.isDrawBg() || (data.getBgDrawable() == null && data.getBgColor() == -1)) {
            return;
        }
        
        Path surface = mDrawDataSetSurfacePathBuffer;
        surface.reset();
        
        if (data.getDrawBgMode() == DrawBgMode.POLYGON) {
            MPPointF p1out = MPPointF.getInstance(0, 0);
            MPPointF p2out = MPPointF.getInstance(0, 0);
            
            boolean hasMovedToPoint = false;
            
            for (int i = 0; i < mChart.getData().getEntryCount(); i++) {
                float r = (mChart.getYAxis().mEntries[labelCount - 1] - mChart.getYChartMin()) * factor;
                
                Utils.getPosition(center, r, sliceangle * i + rotationangle, p1out);
                Utils.getPosition(center, r, sliceangle * (i + 1) + rotationangle, p2out);
                if (!hasMovedToPoint) {
                    surface.moveTo(p1out.x, p1out.y);
                    hasMovedToPoint = true;
                }
                
                surface.lineTo(p2out.x, p2out.y);
            }
            
            surface.close();
            
            if (data.getBgDrawable() != null) {
                drawFilledPath(c, surface, data.getBgDrawable());
            } else {
                drawFilledPath(c, surface, data.getBgColor(), 1);
            }
            
            MPPointF.recycleInstance(p1out);
            MPPointF.recycleInstance(p2out);
            return;
        }
        
        float r = (mChart.getYAxis().mEntries[labelCount - 1] - mChart.getYChartMin()) * factor;
        surface.addCircle(center.x, center.y, r, Direction.CCW);
        
        if (data.getBgDrawable() != null) {
            drawFilledPath(c, surface, data.getBgDrawable());
        } else {
            drawFilledPath(c, surface, data.getBgColor(), 255);
        }
    }
    
    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {
        
        float sliceangle = mChart.getSliceAngle();
        
        // calculate the factor that is needed for transforming the value to
        // pixels
        float factor = mChart.getFactor();
        
        MPPointF center = mChart.getCenterOffsets();
        MPPointF pOut = MPPointF.getInstance(0, 0);
        
        SmartRadarData radarData = mChart.getData();
        
        for (Highlight high : indices) {
            
            ISmartRadarDataSet set = radarData.getDataSetByIndex(high.getDataSetIndex());
            
            if (set == null || !set.isHighlightEnabled())
                continue;
            
            SmartRadarEntry e = set.getEntryForIndex((int) high.getX());
            
            if (!isInBoundsX(e, set))
                continue;
            
            float y = (e.getY() - mChart.getYChartMin());
            
            Utils.getPosition(center,
                y * factor * mAnimator.getPhaseY(),
                sliceangle * high.getX() * mAnimator.getPhaseX() + mChart.getRotationAngle(),
                pOut);
            
            high.setDraw(pOut.x, pOut.y);
            
            // draw the lines
            drawHighlightLines(c, pOut.x, pOut.y, set);
            
            if (set.isDrawHighlightCircleEnabled()) {
                
                if (!Float.isNaN(pOut.x) && !Float.isNaN(pOut.y)) {
                    
                    int strokeColor = set.getHighlightCircleStrokeColor();
                    if (strokeColor == ColorTemplate.COLOR_NONE) {
                        strokeColor = set.getColor(0);
                    }
                    
                    if (set.getHighlightCircleStrokeAlpha() < 255) {
                        strokeColor = ColorTemplate.colorWithAlpha(strokeColor, set.getHighlightCircleStrokeAlpha());
                    }
                    
                    drawHighlightCircle(c,
                        pOut,
                        set.getHighlightCircleInnerRadius(),
                        set.getHighlightCircleOuterRadius(),
                        set.getHighlightCircleFillColor(),
                        strokeColor,
                        set.getHighlightCircleStrokeWidth());
                }
            }
        }
        
        MPPointF.recycleInstance(center);
        MPPointF.recycleInstance(pOut);
    }
    
    protected Path mDrawHighlightCirclePathBuffer = new Path();
    
    public void drawHighlightCircle(Canvas c,
                                    MPPointF point,
                                    float innerRadius,
                                    float outerRadius,
                                    int fillColor,
                                    int strokeColor,
                                    float strokeWidth) {
        c.save();
        
        outerRadius = Utils.convertDpToPixel(outerRadius);
        innerRadius = Utils.convertDpToPixel(innerRadius);
        
        if (fillColor != ColorTemplate.COLOR_NONE) {
            Path p = mDrawHighlightCirclePathBuffer;
            p.reset();
            p.addCircle(point.x, point.y, outerRadius, Path.Direction.CW);
            if (innerRadius > 0.f) {
                p.addCircle(point.x, point.y, innerRadius, Path.Direction.CCW);
            }
            mHighlightCirclePaint.setColor(fillColor);
            mHighlightCirclePaint.setStyle(Paint.Style.FILL);
            c.drawPath(p, mHighlightCirclePaint);
        }
        
        if (strokeColor != ColorTemplate.COLOR_NONE) {
            mHighlightCirclePaint.setColor(strokeColor);
            mHighlightCirclePaint.setStyle(Paint.Style.STROKE);
            mHighlightCirclePaint.setStrokeWidth(Utils.convertDpToPixel(strokeWidth));
            c.drawCircle(point.x, point.y, outerRadius, mHighlightCirclePaint);
        }
        
        c.restore();
    }
}
