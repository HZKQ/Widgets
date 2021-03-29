package com.github.mikephil.charting.custom.piechart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.PieRadarChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import java.util.List;

import androidx.annotation.ColorInt;

/**
 * 饼状图
 *
 * @author Created by 汪高皖 on 2018/4/16 0016 14:21
 */
public class SmartPieChart extends PieRadarChartBase<SmartPieData> {
    /**
     * rect object that represents the bounds of the piechart, needed for
     * drawing the circle
     */
    private RectF mCircleBox = new RectF();
    
    /**
     * flag indicating if entry labels should be drawn or not
     */
    private boolean mDrawEntryLabels = true;
    
    /**
     * array that holds the width of each pie-slice in degrees
     */
    private float[] mDrawAngles = new float[1];
    
    /**
     * array that holds the absolute angle in degrees of each slice
     */
    private float[] mAbsoluteAngles = new float[1];
    
    /**
     * if true, the white hole inside the chart will be drawn
     */
    private boolean mDrawHole = true;
    
    /**
     * if true, the hole will see-through to the inner tips of the slices
     */
    private boolean mDrawSlicesUnderHole = false;
    
    /**
     * if true, the values inside the piechart are drawn as percent values
     */
    private boolean mUsePercentValues = false;
    
    /**
     * if true, the slices of the piechart are rounded
     */
    private boolean mDrawRoundedSlices = false;
    
    /**
     * variable for the text that is drawn in the center of the pie-chart
     */
    private CharSequence mCenterText = "";
    
    private MPPointF mCenterTextOffset = MPPointF.getInstance(0, 0);
    
    /**
     * indicates the size of the hole in the center of the piechart, default:
     * radius / 2
     */
    private float mHoleRadiusPercent = 50f;
    
    /**
     * the radius of the transparent circle next to the chart-hole in the center
     */
    protected float mTransparentCircleRadiusPercent = 55f;
    
    /**
     * if enabled, centertext is drawn
     */
    private boolean mDrawCenterText = true;
    
    private float mCenterTextRadiusPercent = 100.f;
    
    protected float mMaxAngle = 360f;
    
    public SmartPieChart(Context context) {
        super(context);
    }
    
    public SmartPieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public SmartPieChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void init() {
        super.init();
        
        mRenderer = new SmartPieChartRenderer(this, mAnimator, mViewPortHandler);
        mXAxis = null;
        
        mHighlighter = new SmartPieHighlighter(this);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mData == null)
            return;
        
        mRenderer.drawData(canvas);
        
        if (valuesToHighlight())
            mRenderer.drawHighlighted(canvas, mIndicesToHighlight);
        
        mRenderer.drawExtras(canvas);
        
        mRenderer.drawValues(canvas);
        
        mLegendRenderer.renderLegend(canvas);
        
        drawDescription(canvas);
        
        drawMarkers(canvas);
    }
    
    @Override
    public void calculateOffsets() {
        super.calculateOffsets();
        
        // prevent nullpointer when no data set
        if (mData == null)
            return;
        
        float diameter = getDiameter();
        float radius = diameter / 2f;
        
        MPPointF c = getCenterOffsets();
        
        float shift = mData.getDataSet().getSelectionShift();
        
        // create the circle box that will contain the pie-chart (the bounds of
        // the pie-chart)
        mCircleBox.set(c.x - radius + shift,
            c.y - radius + shift,
            c.x + radius - shift,
            c.y + radius - shift);
        
        MPPointF.recycleInstance(c);
    }
    
    @Override
    protected void calcMinMax() {
        calcAngles();
    }
    
    @Override
    protected float[] getMarkerPosition(Highlight highlight) {
        
        MPPointF center = getCenterCircleBox();
        float r = getRadius();
        
        float off = r / 10f * 3.6f;
        
        if (isDrawHoleEnabled()) {
            off = (r - (r / 100f * getHoleRadius())) / 2f;
        }
        
        r -= off; // offset to keep things inside the chart
        
        float rotationAngle = getRotationAngle();
        
        int entryIndex = (int) highlight.getX();
        
        // offset needed to center the drawn text in the slice
        float offset = mDrawAngles[entryIndex] / 2;
        
        // calculate the text position
        float x = (float) (r
            * Math.cos(Math.toRadians((rotationAngle + mAbsoluteAngles[entryIndex] - offset)
            * mAnimator.getPhaseY())) + center.x);
        float y = (float) (r
            * Math.sin(Math.toRadians((rotationAngle + mAbsoluteAngles[entryIndex] - offset)
            * mAnimator.getPhaseY())) + center.y);
        
        MPPointF.recycleInstance(center);
        return new float[]{x, y};
    }
    
    /**
     * calculates the needed angles for the chart slices
     */
    private void calcAngles() {
        
        int entryCount = mData.getEntryCount();
        
        if (mDrawAngles.length != entryCount) {
            mDrawAngles = new float[entryCount];
        } else {
            for (int i = 0; i < entryCount; i++) {
                mDrawAngles[i] = 0;
            }
        }
        if (mAbsoluteAngles.length != entryCount) {
            mAbsoluteAngles = new float[entryCount];
        } else {
            for (int i = 0; i < entryCount; i++) {
                mAbsoluteAngles[i] = 0;
            }
        }
        
        float yValueSum = mData.getYValueSum();
        
        List<ISmartPieDataSet> dataSets = mData.getDataSets();
        
        int cnt = 0;
        
        for (int i = 0; i < mData.getDataSetCount(); i++) {
            
            ISmartPieDataSet set = dataSets.get(i);
            
            for (int j = 0; j < set.getEntryCount(); j++) {
                
                mDrawAngles[cnt] = calcAngle(Math.abs(set.getEntryForIndex(j).getY()), yValueSum);
                
                if (cnt == 0) {
                    mAbsoluteAngles[cnt] = mDrawAngles[cnt];
                } else {
                    mAbsoluteAngles[cnt] = mAbsoluteAngles[cnt - 1] + mDrawAngles[cnt];
                }
                
                cnt++;
            }
        }
        
    }
    
    /**
     * Checks if the given index is set to be highlighted.
     */
    public boolean needsHighlight(int index) {
        // no highlight
        if (!valuesToHighlight())
            return false;
        
        for (int i = 0; i < mIndicesToHighlight.length; i++)
            // check if the xvalue for the given dataset needs highlight
            if ((int) mIndicesToHighlight[i].getX() == index)
                return true;
        
        return false;
    }
    
    /**
     * calculates the needed angle for a given value
     */
    private float calcAngle(float value) {
        return calcAngle(value, mData.getYValueSum());
    }
    
    /**
     * calculates the needed angle for a given value
     */
    private float calcAngle(float value, float yValueSum) {
        return value / yValueSum * mMaxAngle;
    }
    
    /**
     * This will throw an exception, PieChart has no XAxis object.
     */
    @Deprecated
    @Override
    public XAxis getXAxis() {
        throw new RuntimeException("PieChart has no XAxis");
    }
    
    @Override
    public int getIndexForAngle(float angle) {
        
        // take the current angle of the chart into consideration
        float a = Utils.getNormalizedAngle(angle - getRotationAngle());
        
        for (int i = 0; i < mAbsoluteAngles.length; i++) {
            if (mAbsoluteAngles[i] > a)
                return i;
        }
        
        return -1; // return -1 if no index found
    }
    
    /**
     * Returns the index of the DataSet this x-index belongs to.
     */
    public int getDataSetIndexForIndex(int xIndex) {
        
        List<ISmartPieDataSet> dataSets = mData.getDataSets();
        
        for (int i = 0; i < dataSets.size(); i++) {
            if (dataSets.get(i).getEntryForXValue(xIndex, Float.NaN) != null)
                return i;
        }
        
        return -1;
    }
    
    /**
     * returns an integer array of all the different angles the chart slices
     * have the angles in the returned array determine how much space (of 360°)
     * each slice takes
     */
    public float[] getDrawAngles() {
        return mDrawAngles;
    }
    
    /**
     * returns the absolute angles of the different chart slices (where the
     * slices end)
     */
    public float[] getAbsoluteAngles() {
        return mAbsoluteAngles;
    }
    
    /**
     * Sets the color for the hole that is drawn in the center of the PieChart
     * (if enabled).
     */
    public void setHoleColor(int color) {
        ((SmartPieChartRenderer) mRenderer).getPaintHole().setColor(color);
    }
    
    /**
     * Enable or disable the visibility of the inner tips of the slices behind the hole
     */
    public void setDrawSlicesUnderHole(boolean enable) {
        mDrawSlicesUnderHole = enable;
    }
    
    /**
     * Returns true if the inner tips of the slices are visible behind the hole,
     * false if not.
     *
     * @return true if slices are visible behind the hole.
     */
    public boolean isDrawSlicesUnderHoleEnabled() {
        return mDrawSlicesUnderHole;
    }
    
    /**
     * set this to true to draw the pie center empty
     */
    public void setDrawHoleEnabled(boolean enabled) {
        this.mDrawHole = enabled;
    }
    
    /**
     * returns true if the hole in the center of the pie-chart is set to be
     * visible, false if not
     */
    public boolean isDrawHoleEnabled() {
        return mDrawHole;
    }
    
    /**
     * Sets the text String that is displayed in the center of the PieChart.
     */
    public void setCenterText(CharSequence text) {
        if (text == null)
            mCenterText = "";
        else
            mCenterText = text;
    }
    
    /**
     * returns the text that is drawn in the center of the pie-chart
     */
    public CharSequence getCenterText() {
        return mCenterText;
    }
    
    /**
     * set this to true to draw the text that is displayed in the center of the
     * pie chart
     */
    public void setDrawCenterText(boolean enabled) {
        this.mDrawCenterText = enabled;
    }
    
    /**
     * returns true if drawing the center text is enabled
     */
    public boolean isDrawCenterTextEnabled() {
        return mDrawCenterText;
    }
    
    @Override
    protected float getRequiredLegendOffset() {
        return mLegendRenderer.getLabelPaint().getTextSize() * 2.f;
    }
    
    @Override
    protected float getRequiredBaseOffset() {
        return 0;
    }
    
    @Override
    public float getRadius() {
        if (mCircleBox == null)
            return 0;
        else
            return Math.min(mCircleBox.width() / 2f, mCircleBox.height() / 2f);
    }
    
    /**
     * returns the circlebox, the boundingbox of the pie-chart slices
     */
    public RectF getCircleBox() {
        return mCircleBox;
    }
    
    /**
     * returns the center of the circlebox
     */
    public MPPointF getCenterCircleBox() {
        return MPPointF.getInstance(mCircleBox.centerX(), mCircleBox.centerY());
    }
    
    /**
     * sets the typeface for the center-text paint
     */
    public void setCenterTextTypeface(Typeface t) {
        ((SmartPieChartRenderer) mRenderer).getPaintCenterText().setTypeface(t);
    }
    
    /**
     * Sets the size of the center text of the PieChart in dp.
     */
    public void setCenterTextSize(float sizeDp) {
        ((SmartPieChartRenderer) mRenderer).getPaintCenterText().setTextSize(
            Utils.convertDpToPixel(sizeDp));
    }
    
    /**
     * Sets the size of the center text of the PieChart in px.
     */
    public void setCenterTextSizeInPx(float sizePixels) {
        ((SmartPieChartRenderer) mRenderer).getPaintCenterText().setTextSize(sizePixels);
    }
    
    /**
     * Sets the offset the center text should have from it's original position in dp. Default x = 0, y = 0
     */
    public void setCenterTextOffset(float x, float y) {
        mCenterTextOffset.x = Utils.convertDpToPixel(x);
        mCenterTextOffset.y = Utils.convertDpToPixel(y);
    }
    
    /**
     * Sets the offset the center text should have from it's original position in px. Default x = 0, y = 0
     */
    public void setCenterTextOffsetInPx(float x, float y) {
        mCenterTextOffset.x = x;
        mCenterTextOffset.y = y;
    }
    
    /**
     * Returns the offset on the x- and y-axis the center text has in dp.
     */
    public MPPointF getCenterTextOffset() {
        return MPPointF.getInstance(mCenterTextOffset.x, mCenterTextOffset.y);
    }
    
    /**
     * Sets the color of the center text of the PieChart.
     */
    public void setCenterTextColor(int color) {
        ((SmartPieChartRenderer) mRenderer).getPaintCenterText().setColor(color);
    }
    
    /**
     * sets the radius of the hole in the center of the piechart in percent of
     * the maximum radius (max = the radius of the whole chart), default 50%
     */
    public void setHoleRadius(final float percent) {
        mHoleRadiusPercent = percent;
    }
    
    /**
     * Returns the size of the hole radius in percent of the total radius.
     */
    public float getHoleRadius() {
        return mHoleRadiusPercent;
    }
    
    /**
     * Sets the color the transparent-circle should have.
     */
    public void setTransparentCircleColor(int color) {
        
        Paint p = ((SmartPieChartRenderer) mRenderer).getPaintTransparentCircle();
        int alpha = p.getAlpha();
        p.setColor(color);
        p.setAlpha(alpha);
    }
    
    /**
     * sets the radius of the transparent circle that is drawn next to the hole
     * in the piechart in percent of the maximum radius (max = the radius of the
     * whole chart), default 55% -> means 5% larger than the center-hole by
     * default
     */
    public void setTransparentCircleRadius(final float percent) {
        mTransparentCircleRadiusPercent = percent;
    }
    
    public float getTransparentCircleRadius() {
        return mTransparentCircleRadiusPercent;
    }
    
    /**
     * Sets the amount of transparency the transparent circle should have 0 = fully transparent,
     * 255 = fully opaque.
     * Default value is 100.
     *
     * @param alpha 0-255
     */
    public void setTransparentCircleAlpha(int alpha) {
        ((SmartPieChartRenderer) mRenderer).getPaintTransparentCircle().setAlpha(alpha);
    }
    
    /**
     * Set this to true to draw the entry labels into the pie slices (Provided by the getLabel() method of the PieEntry class).
     * Deprecated -> use setDrawEntryLabels(...) instead.
     */
    @Deprecated
    public void setDrawSliceText(boolean enabled) {
        mDrawEntryLabels = enabled;
    }
    
    /**
     * Set this to true to draw the entry labels into the pie slices (Provided by the getLabel() method of the PieEntry class).
     */
    public void setDrawEntryLabels(boolean enabled) {
        mDrawEntryLabels = enabled;
    }
    
    /**
     * Returns true if drawing the entry labels is enabled, false if not.
     */
    public boolean isDrawEntryLabelsEnabled() {
        return mDrawEntryLabels;
    }
    
    /**
     * Sets the color the entry labels are drawn with.
     */
    public void setEntryLabelColor(int color) {
        ((SmartPieChartRenderer) mRenderer).getPaintEntryLabels().setColor(color);
    }
    
    /**
     * Sets a custom Typeface for the drawing of the entry labels.
     */
    public void setEntryLabelTypeface(Typeface tf) {
        ((SmartPieChartRenderer) mRenderer).getPaintEntryLabels().setTypeface(tf);
    }
    
    /**
     * Sets the size of the entry labels in dp. Default: 13dp
     */
    public void setEntryLabelTextSize(float size) {
        ((SmartPieChartRenderer) mRenderer).getPaintEntryLabels().setTextSize(Utils.convertDpToPixel(size));
    }
    
    /**
     * Sets the size of the entry labels in px
     */
    public void setEntryLabelTextSizeInPx(float size) {
        ((SmartPieChartRenderer) mRenderer).getPaintEntryLabels().setTextSize(size);
    }
    
    /**
     * Returns true if the chart is set to draw each end of a pie-slice
     * "rounded".
     */
    public boolean isDrawRoundedSlicesEnabled() {
        return mDrawRoundedSlices;
    }
    
    /**
     * If this is enabled, values inside the PieChart are drawn in percent and
     * not with their original value. Values provided for the IValueFormatter to
     * format are then provided in percent.
     */
    public void setUsePercentValues(boolean enabled) {
        mUsePercentValues = enabled;
    }
    
    /**
     * Returns true if using percentage values is enabled for the chart.
     */
    public boolean isUsePercentValuesEnabled() {
        return mUsePercentValues;
    }
    
    /**
     * the rectangular radius of the bounding box for the center text, as a percentage of the pie
     * hole
     * default 1.f (100%)
     */
    public void setCenterTextRadiusPercent(float percent) {
        mCenterTextRadiusPercent = percent;
    }
    
    /**
     * the rectangular radius of the bounding box for the center text, as a percentage of the pie
     * hole
     * default 1.f (100%)
     */
    public float getCenterTextRadiusPercent() {
        return mCenterTextRadiusPercent;
    }
    
    public float getMaxAngle() {
        return mMaxAngle;
    }
    
    /**
     * Sets the max angle that is used for calculating the pie-circle. 360f means
     * it's a full PieChart, 180f results in a half-pie-chart. Default: 360f
     *
     * @param maxangle min 90, max 360
     */
    public void setMaxAngle(float maxangle) {
        
        if (maxangle > 360)
            maxangle = 360f;
        
        if (maxangle < 90)
            maxangle = 90f;
        
        this.mMaxAngle = maxangle;
    }
    
    /**
     * 设置是否绘制外边框
     */
    public void setDrawBorder(boolean isDrawBorder) {
        ((SmartPieChartRenderer) mRenderer).setDrawBorder(isDrawBorder);
    }
    
    /**
     * 设置外边框的宽度,单位px
     */
    public void setBorderWidth(float borderWidth) {
        ((SmartPieChartRenderer) mRenderer).setBorderWidth(borderWidth);
    }
    
    /**
     * 设置外边框的颜色
     */
    public void setBorderColor(@ColorInt int color) {
        ((SmartPieChartRenderer) mRenderer).setBorderColor(color);
    }
    
    /**
     * 设置外边框距离饼状图最外围的偏移值
     */
    public void setBorderOffset(float offset) {
        ((SmartPieChartRenderer) mRenderer).setBorderOffset(offset);
    }
    
    /**
     * 设置饼状图外边框的路径效果，例如虚线实线，弯折等
     */
    public void setBorderPathEffect(PathEffect borderPathEffect) {
        ((SmartPieChartRenderer) mRenderer).setBorderPathEffect(borderPathEffect);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        // releases the bitmap in the renderer to avoid oom error
        if (mRenderer != null && mRenderer instanceof SmartPieChartRenderer) {
            ((SmartPieChartRenderer) mRenderer).releaseBitmap();
        }
        super.onDetachedFromWindow();
    }
}
