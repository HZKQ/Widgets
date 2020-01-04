package com.github.mikephil.charting.custom.piechart;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

import androidx.annotation.ColorInt;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.renderer.DataRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 处理饼状图绘制逻辑
 *
 * @author Created by 汪高皖 on 2018/4/16 0016 14:25
 */
@SuppressWarnings("ALL")
public class SmartPieChartRenderer extends DataRenderer {
    protected SmartPieChart mChart;
    
    /**
     * paint for the hole in the center of the pie chart and the transparent
     * circle
     */
    protected Paint mHolePaint;
    protected Paint mTransparentCirclePaint;
    protected Paint mValueLinePaint;
    protected Paint mBoundsPaint;
    
    /**
     * paint object for the text that can be displayed in the center of the
     * chart
     */
    private TextPaint mCenterTextPaint;
    
    /**
     * paint object used for drwing the slice-text
     */
    protected Paint mEntryLabelsPaint;
    
    private StaticLayout mCenterTextLayout;
    private CharSequence mCenterTextLastValue;
    private RectF mCenterTextLastBounds = new RectF();
    private RectF[] mRectFBuffer = {new RectF(), new RectF()};
    
    /**
     * Bitmap for drawing the center hole
     */
    protected WeakReference<Bitmap> mDrawBitmap;
    
    protected Canvas mBitmapCanvas;
    
    protected boolean mIsDrawBorder = false;
    protected float mBorderWidth = 10f;
    protected float mBorderOffset = 10f;
    protected int mBorderColor = Color.WHITE;
    protected PathEffect mBorderPathEffect;
    
    private Path mHoleCirclePath = new Path();
    protected Path mDrawCenterTextPathBuffer = new Path();
    protected RectF mOutRectFBuffer = new RectF();
    private Path mPathBuffer = new Path();
    private RectF mInnerRectBuffer = new RectF();
    
    public SmartPieChartRenderer(SmartPieChart chart, ChartAnimator animator,
                                 ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        mChart = chart;
        
        mHolePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHolePaint.setColor(Color.WHITE);
        mHolePaint.setStyle(Paint.Style.FILL);
        
        mTransparentCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTransparentCirclePaint.setColor(Color.WHITE);
        mTransparentCirclePaint.setStyle(Paint.Style.FILL);
        mTransparentCirclePaint.setAlpha(105);
        
        mCenterTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mCenterTextPaint.setColor(Color.BLACK);
        mCenterTextPaint.setTextSize(Utils.convertDpToPixel(12f));
        
        mValuePaint.setTextSize(Utils.convertDpToPixel(13f));
        mValuePaint.setColor(Color.WHITE);
        mValuePaint.setTextAlign(Paint.Align.CENTER);
        
        mEntryLabelsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEntryLabelsPaint.setColor(Color.WHITE);
        mEntryLabelsPaint.setTextAlign(Paint.Align.CENTER);
        mEntryLabelsPaint.setTextSize(Utils.convertDpToPixel(13f));
        
        mValueLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mValueLinePaint.setStyle(Paint.Style.STROKE);
        
        mBoundsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBoundsPaint.setStyle(Paint.Style.STROKE);
    }
    
    public Paint getPaintHole() {
        return mHolePaint;
    }
    
    public Paint getPaintTransparentCircle() {
        return mTransparentCirclePaint;
    }
    
    public TextPaint getPaintCenterText() {
        return mCenterTextPaint;
    }
    
    public Paint getPaintEntryLabels() {
        return mEntryLabelsPaint;
    }
    
    @Override
    public void initBuffers() {
        // TODO Auto-generated method stub
    }
    
    @Override
    public void drawData(Canvas c) {
        
        int width = (int) mViewPortHandler.getChartWidth();
        int height = (int) mViewPortHandler.getChartHeight();
        
        if (mDrawBitmap == null
            || (mDrawBitmap.get().getWidth() != width)
            || (mDrawBitmap.get().getHeight() != height)) {
            
            if (width > 0 && height > 0) {
                
                mDrawBitmap = new WeakReference<Bitmap>(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444));
                mBitmapCanvas = new Canvas(mDrawBitmap.get());
            } else {
                return;
            }
        }
        
        mDrawBitmap.get().eraseColor(Color.TRANSPARENT);
        
        SmartPieData pieData = mChart.getData();
        
        for (ISmartPieDataSet set : pieData.getDataSets()) {
            
            if (set.isVisible() && set.getEntryCount() > 0) {
                drawDataSet(c, set);
            }
        }
    }
    
    protected float calculateMinimumRadiusForSpacedSlice(
        MPPointF center,
        float radius,
        float angle,
        float arcStartPointX,
        float arcStartPointY,
        float startAngle,
        float sweepAngle) {
        final float angleMiddle = startAngle + sweepAngle / 2.f;
        
        // Other point of the arc
        float arcEndPointX = center.x + radius * (float) Math.cos((startAngle + sweepAngle) * Utils.FDEG2RAD);
        float arcEndPointY = center.y + radius * (float) Math.sin((startAngle + sweepAngle) * Utils.FDEG2RAD);
        
        // Middle point on the arc
        float arcMidPointX = center.x + radius * (float) Math.cos(angleMiddle * Utils.FDEG2RAD);
        float arcMidPointY = center.y + radius * (float) Math.sin(angleMiddle * Utils.FDEG2RAD);
        
        // This is the base of the contained triangle
        double basePointsDistance = Math.sqrt(
            Math.pow(arcEndPointX - arcStartPointX, 2) +
                Math.pow(arcEndPointY - arcStartPointY, 2));
        
        // After reducing space from both sides of the "slice",
        //   the angle of the contained triangle should stay the same.
        // So let's find out the height of that triangle.
        float containedTriangleHeight = (float) (basePointsDistance / 2.0 *
            Math.tan((180.0 - angle) / 2.0 * Utils.DEG2RAD));
        
        // Now we subtract that from the radius
        float spacedRadius = radius - containedTriangleHeight;
        
        // And now subtract the height of the arc that's between the triangle and the outer circle
        spacedRadius -= Math.sqrt(
            Math.pow(arcMidPointX - (arcEndPointX + arcStartPointX) / 2.f, 2) +
                Math.pow(arcMidPointY - (arcEndPointY + arcStartPointY) / 2.f, 2));
        
        return spacedRadius;
    }
    
    /**
     * Calculates the sliceSpace to use based on visible values and their size compared to the set sliceSpace.
     */
    protected float getSliceSpace(ISmartPieDataSet dataSet) {
        
        if (!dataSet.isAutomaticallyDisableSliceSpacingEnabled()) {
            return dataSet.getSliceSpace();
        }
        
        float spaceSizeRatio = dataSet.getSliceSpace() / mViewPortHandler.getSmallestContentExtension();
        float minValueRatio = dataSet.getYMin() / mChart.getData().getYValueSum() * 2;
        
        float sliceSpace = spaceSizeRatio > minValueRatio ? 0f : dataSet.getSliceSpace();
        
        return sliceSpace;
    }
    
    protected void drawDataSet(Canvas c, ISmartPieDataSet dataSet) {
        
        float angle = 0;
        float rotationAngle = mChart.getRotationAngle();
        
        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();
        
        final int entryCount = dataSet.getEntryCount();
        final float[] drawAngles = mChart.getDrawAngles();
        final MPPointF center = mChart.getCenterCircleBox();
        final float radius = mChart.getRadius();
        final boolean drawInnerArc = mChart.isDrawHoleEnabled() && !mChart.isDrawSlicesUnderHoleEnabled();
        final float userInnerRadius = drawInnerArc
            ? radius * (mChart.getHoleRadius() / 100.f)
            : 0.f;
        
        int visibleAngleCount = 0;
        for (int j = 0; j < entryCount; j++) {
            // draw only if the value is greater than zero
            if ((Math.abs(dataSet.getEntryForIndex(j).getY()) > Utils.FLOAT_EPSILON)) {
                visibleAngleCount++;
            }
        }
        
        final float sliceSpace = visibleAngleCount <= 1 ? 0.f : getSliceSpace(dataSet);
        for (int j = 0; j < entryCount; j++) {
            
            float sliceAngle = drawAngles[j];
            float innerRadius = userInnerRadius;
            float outRadius = radius;
            Entry e = dataSet.getEntryForIndex(j);
            if (e instanceof SmartPieEntry && drawInnerArc && userInnerRadius > 0) {
                float arcWidth = ((SmartPieEntry) e).getArcWidth();
                if (arcWidth > 0) {
                    int drawBaseline = ((SmartPieEntry) e).getDrawBaseline();
                    if (drawBaseline == DrawBaseline.CENTER) {
                        float arcOffset = (radius - userInnerRadius - arcWidth) / 2;
                        if (arcOffset > 0) {
                            innerRadius += arcOffset;
                            outRadius -= arcOffset;
                        }
                    } else if (drawBaseline == DrawBaseline.TOP) {
                        float temp = outRadius - arcWidth;
                        innerRadius = temp < innerRadius ? innerRadius : temp;
                    } else {
                        float temp = innerRadius + arcWidth;
                        outRadius = temp > outRadius ? outRadius : temp;
                    }
                }
            }
            
            // draw only if the value is greater than zero
            if ((Math.abs(e.getY()) > Utils.FLOAT_EPSILON)) {
                if (!dataSet.isHighlightEnabled() || !mChart.needsHighlight(j)) {
                    
                    final boolean accountForSliceSpacing = sliceSpace > 0.f && sliceAngle <= 180.f;
                    
                    mRenderPaint.setColor(dataSet.getColor(j));
                    
                    final float sliceSpaceAngleOuter = visibleAngleCount == 1 ?
                        0.f :
                        sliceSpace / (Utils.FDEG2RAD * radius);
                    final float startAngleOuter = rotationAngle + (angle + sliceSpaceAngleOuter / 2.f) * phaseY;
                    float sweepAngleOuter = (sliceAngle - sliceSpaceAngleOuter) * phaseY;
                    if (sweepAngleOuter < 0.f) {
                        sweepAngleOuter = 0.f;
                    }
                    
                    mPathBuffer.reset();
                    
                    float arcStartPointX = center.x + outRadius * (float) Math.cos(startAngleOuter * Utils.FDEG2RAD);
                    float arcStartPointY = center.y + outRadius * (float) Math.sin(startAngleOuter * Utils.FDEG2RAD);
                    
                    if (sweepAngleOuter >= 360.f && sweepAngleOuter % 360f <= Utils.FLOAT_EPSILON) {
                        // Android is doing "mod 360"
                        mPathBuffer.addCircle(center.x, center.y, outRadius, Path.Direction.CW);
                    } else {
                        mPathBuffer.moveTo(arcStartPointX, arcStartPointY);
                        
                        // API < 21 does not receive floats in addArc, but a RectF
                        mOutRectFBuffer.set(
                            center.x - outRadius,
                            center.y - outRadius,
                            center.x + outRadius,
                            center.y + outRadius
                        );
                        mPathBuffer.arcTo(
                            mOutRectFBuffer,
                            startAngleOuter,
                            sweepAngleOuter
                        );
                    }
                    
                    if (drawInnerArc &&
                        (innerRadius > 0.f || accountForSliceSpacing)) {
                        
                        if (accountForSliceSpacing) {
                            float minSpacedRadius =
                                calculateMinimumRadiusForSpacedSlice(
                                    center, radius,
                                    sliceAngle * phaseY,
                                    arcStartPointX, arcStartPointY,
                                    startAngleOuter,
                                    sweepAngleOuter);
                            
                            if (minSpacedRadius < 0.f) {
                                minSpacedRadius = -minSpacedRadius;
                            }
                            
                            innerRadius = Math.max(innerRadius, minSpacedRadius);
                        }
                        
                        final float sliceSpaceAngleInner = visibleAngleCount == 1 || innerRadius == 0.f ?
                            0.f :
                            sliceSpace / (Utils.FDEG2RAD * innerRadius);
                        final float startAngleInner = rotationAngle + (angle + sliceSpaceAngleInner / 2.f) * phaseY;
                        float sweepAngleInner = (sliceAngle - sliceSpaceAngleInner) * phaseY;
                        if (sweepAngleInner < 0.f) {
                            sweepAngleInner = 0.f;
                        }
                        final float endAngleInner = startAngleInner + sweepAngleInner;
                        
                        if (sweepAngleOuter >= 360.f && sweepAngleOuter % 360f <= Utils.FLOAT_EPSILON) {
                            // Android is doing "mod 360"
                            mPathBuffer.addCircle(center.x, center.y, innerRadius, Path.Direction.CCW);
                        } else {
                            
                            mPathBuffer.lineTo(
                                center.x + innerRadius * (float) Math.cos(endAngleInner * Utils.FDEG2RAD),
                                center.y + innerRadius * (float) Math.sin(endAngleInner * Utils.FDEG2RAD));
                            
                            // API < 21 does not receive floats in addArc, but a RectF
                            mInnerRectBuffer.set(
                                center.x - innerRadius,
                                center.y - innerRadius,
                                center.x + innerRadius,
                                center.y + innerRadius
                            );
                            mPathBuffer.arcTo(
                                mInnerRectBuffer,
                                endAngleInner,
                                -sweepAngleInner
                            );
                        }
                    } else {
                        
                        if (sweepAngleOuter % 360f > Utils.FLOAT_EPSILON) {
                            if (accountForSliceSpacing) {
                                
                                float angleMiddle = startAngleOuter + sweepAngleOuter / 2.f;
                                
                                float sliceSpaceOffset =
                                    calculateMinimumRadiusForSpacedSlice(
                                        center,
                                        radius,
                                        sliceAngle * phaseY,
                                        arcStartPointX,
                                        arcStartPointY,
                                        startAngleOuter,
                                        sweepAngleOuter);
                                
                                float arcEndPointX = center.x +
                                    sliceSpaceOffset * (float) Math.cos(angleMiddle * Utils.FDEG2RAD);
                                float arcEndPointY = center.y +
                                    sliceSpaceOffset * (float) Math.sin(angleMiddle * Utils.FDEG2RAD);
                                
                                mPathBuffer.lineTo(
                                    arcEndPointX,
                                    arcEndPointY);
                                
                            } else {
                                mPathBuffer.lineTo(
                                    center.x,
                                    center.y);
                            }
                        }
                        
                    }
                    
                    mPathBuffer.close();
                    
                    mBitmapCanvas.drawPath(mPathBuffer, mRenderPaint);
                }
            }
            
            angle += sliceAngle * phaseX;
        }
        
        MPPointF.recycleInstance(center);
    }
    
    @Override
    public void drawValues(Canvas c) {
        MPPointF center = mChart.getCenterCircleBox();
        
        // get whole the radius
        float radius = mChart.getRadius();
        float rotationAngle = mChart.getRotationAngle();
        float[] drawAngles = mChart.getDrawAngles();
        float[] absoluteAngles = mChart.getAbsoluteAngles();
        
        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();
        
        final float holeRadiusPercent = mChart.getHoleRadius() / 100.f;
        
        SmartPieData data = mChart.getData();
        List<ISmartPieDataSet> dataSets = data.getDataSets();
        
        float yValueSum = data.getYValueSum();
        
        boolean drawEntryLabels = mChart.isDrawEntryLabelsEnabled();
        
        float angle;
        int xIndex = 0;
        
        c.save();
        
        float offset = Utils.convertDpToPixel(-0.f);
        
        for (int i = 0; i < dataSets.size(); i++) {
            
            ISmartPieDataSet dataSet = dataSets.get(i);
            
            final boolean drawValues = dataSet.isDrawValuesEnabled();
            final boolean isDrawHighlightYValue = dataSet.isDrawHighlightYValue();
            final boolean isDrawHighlightXValue = dataSet.isDrawHighlightXValue();
            if (!drawValues && !drawEntryLabels
                && (!dataSet.isHighlightEnabled()
                || (!isDrawHighlightXValue && !isDrawHighlightYValue))) {
                continue;
            }
            
            final SmartPieDataSet.ValuePosition xValuePosition = dataSet.getXValuePosition();
            final SmartPieDataSet.ValuePosition yValuePosition = dataSet.getYValuePosition();
            
            // apply the text-styling defined by the DataSet
            applyValueTextStyle(dataSet);
            
            mValuePaint.setTextSize(dataSet.getValueTextSize());
            final float labelTextHeight = Utils.calcTextHeight(mEntryLabelsPaint, "Q");
            final float textHeight = Utils.calcTextHeight(mValuePaint, "Q");
            
            mValuePaint.setTextSize(dataSet.getYSecondValueTextSize());
            final float secondTextHeight = Utils.calcTextHeight(mValuePaint, "Q");
            final float textHeightOffset = Utils.convertDpToPixel(4f);
            
            ValueFormatter formatter = dataSet.getValueFormatter();
            SmartPieChartValueFormatter smartValueFormatter = dataSet.getSmartValueFormatter();
            
            int entryCount = dataSet.getEntryCount();
            
            mValueLinePaint.setColor(dataSet.getValueLineColor());
            mValueLinePaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getValueLineWidth()));
            mValueLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            
            final float sliceSpace = getSliceSpace(dataSet);
            
            MPPointF iconsOffset = MPPointF.getInstance(dataSet.getIconsOffset());
            iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x);
            iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y);
            
            for (int j = 0; j < entryCount; j++) {
                boolean needsHighlight = mChart.needsHighlight(j);
                if (!drawValues && !drawEntryLabels && !needsHighlight) {
                    // 此时说明进入绘制是因为需要绘制高亮选中的值
                    // 但是当前Item的needsHighlight为false说明未选中，因此不绘制
                    xIndex++;
                    continue;
                }
                
                mValueLinePaint.setColor(dataSet.getValueLineColor(j));
                float radius2 = radius;
                float innerRadius = radius * holeRadiusPercent;
                SmartPieEntry entry = dataSet.getEntryForIndex(j);
                if (entry instanceof SmartPieEntry && mChart.isDrawHoleEnabled() && holeRadiusPercent > 0) {
                    float arcWidth = ((SmartPieEntry) entry).getArcWidth();
                    if (arcWidth > 0) {
                        int drawBaseline = ((SmartPieEntry) entry).getDrawBaseline();
                        if (drawBaseline == DrawBaseline.CENTER) {
                            radius2 = radius - ((radius - innerRadius) - arcWidth) / 2;
                        } else if (drawBaseline == DrawBaseline.TOP) {
                        
                        } else {
                            radius2 = innerRadius + arcWidth;
                        }
                    }
                }
                
                if (needsHighlight) {
                    radius2 += dataSet.getSelectionShift();
                }
                float labelRadiusOffset = (radius2 - (radius * holeRadiusPercent)) / 2f;
                float labelRadius = radius2 - labelRadiusOffset;
                
                if (xIndex == 0) {
                    angle = 0.f;
                } else {
                    angle = absoluteAngles[xIndex - 1] * phaseX;
                }
                
                final float sliceAngle = drawAngles[xIndex];
                final float sliceSpaceMiddleAngle = sliceSpace / (Utils.FDEG2RAD * labelRadius);
                
                // offset needed to center the drawn text in the slice
                final float angleOffset = (sliceAngle - sliceSpaceMiddleAngle / 2.f) / 2.f;
                
                angle = angle + angleOffset;
                
                final float transformedAngle = rotationAngle + angle * phaseY;
                
                float value = mChart.isUsePercentValuesEnabled() ? entry.getY()
                    / yValueSum * 100f : entry.getY();
                
                float secondValue = mChart.isUsePercentValuesEnabled() ? entry.getYSecondValue()
                    / yValueSum * 100f : entry.getYSecondValue();
                
                final float sliceXBase = (float) Math.cos(transformedAngle * Utils.FDEG2RAD);
                final float sliceYBase = (float) Math.sin(transformedAngle * Utils.FDEG2RAD);
                
                final boolean drawXOutside = (drawEntryLabels || (isDrawHighlightXValue && needsHighlight)) &&
                    xValuePosition == SmartPieDataSet.ValuePosition.OUTSIDE_SLICE;
                final boolean drawYOutside = (drawValues || (isDrawHighlightYValue && needsHighlight)) &&
                    yValuePosition == SmartPieDataSet.ValuePosition.OUTSIDE_SLICE;
                final boolean drawXInside = (drawEntryLabels || (isDrawHighlightXValue && needsHighlight)) &&
                    xValuePosition == SmartPieDataSet.ValuePosition.INSIDE_SLICE;
                final boolean drawYInside = (drawValues || (isDrawHighlightXValue && needsHighlight)) &&
                    yValuePosition == SmartPieDataSet.ValuePosition.INSIDE_SLICE;
                
                String text = smartValueFormatter == null ? formatter.getFormattedValue(value, entry, 0, mViewPortHandler)
                    : smartValueFormatter.getFormattedValue(value, entry, 0, mViewPortHandler);
                String secondText = smartValueFormatter == null ? formatter.getFormattedValue(secondValue) : smartValueFormatter.getFormattedSecondValue(secondValue, entry, 0, mViewPortHandler);
                
                if (drawXOutside || drawYOutside) {
                    
                    final float valueLineLength1 = dataSet.getValueLinePart1Length();
                    final float valueLineLength2 = dataSet.getValueLinePart2Length();
                    final float valueLinePart1OffsetPercentage = dataSet.getValueLinePart1OffsetPercentage() / 100.f;
                    
                    float pt2x, pt2y;
                    float labelPtx, labelPty, secondLabelPty;
                    
                    float line1Radius;
                    
                    if (mChart.isDrawHoleEnabled()) {
                        line1Radius = (radius2 - (radius2 * holeRadiusPercent))
                            * valueLinePart1OffsetPercentage
                            + (radius2 * holeRadiusPercent);
                    } else {
                        line1Radius = radius2 * valueLinePart1OffsetPercentage;
                    }
                    
                    float polyline2Width = labelRadius * valueLineLength2;
                    if (dataSet.isValueLinePart2LengthFollowTextWidth()) {
                        if (!TextUtils.isEmpty(text)) {
                            mValuePaint.setTextSize(dataSet.getValueTextSize());
                            polyline2Width = mValuePaint.measureText(text) * valueLineLength2;
                        }
                        
                        if (dataSet.isDrawYSecondValue() && !TextUtils.isEmpty(secondText)) {
                            mValuePaint.setTextSize(dataSet.getYSecondValueTextSize());
                            float v = mValuePaint.measureText(secondText) * valueLineLength2;
                            if (polyline2Width < v) {
                                polyline2Width = v;
                            }
                        }
                    }
                    
                    
                    final float pt3x = (line1Radius + (dataSet.isDrawDot() ? dataSet.getDotRadius() : 0)) * sliceXBase + center.x;
                    final float pt3y = (line1Radius + (dataSet.isDrawDot() ? dataSet.getDotRadius() : 0)) * sliceYBase + center.y;
                    
                    final float pt0x = pt3x + (dataSet.isDrawDot() ? dataSet.getDotRadius() * sliceXBase : 0);
                    final float pt0y = pt3y + (dataSet.isDrawDot() ? dataSet.getDotRadius() * sliceYBase : 0);
                    
                    final float pt1x = labelRadius * (1 + valueLineLength1) * sliceXBase + center.x + (dataSet.isDrawDot() ? dataSet.getDotRadius() * sliceXBase : 0);
                    final float pt1y = labelRadius * (1 + valueLineLength1) * sliceYBase + center.y + (dataSet.isDrawDot() ? dataSet.getDotRadius() * sliceYBase : 0);
                    
                    float labelPty2;
                    
                    if (transformedAngle % 360.0 >= 90.0 && transformedAngle % 360.0 <= 270.0) {
                        pt2x = pt1x - polyline2Width;
                        pt2y = pt1y;
                        
                        if (dataSet.getYGravity() == LabelGravity.END) {
                            mValuePaint.setTextAlign(Paint.Align.RIGHT);
                        } else {
                            mValuePaint.setTextAlign(Paint.Align.LEFT);
                        }
                        
                        if (drawXOutside) {
                            mEntryLabelsPaint.setTextAlign(Paint.Align.RIGHT);
                        }
                        
                        labelPtx = pt2x - offset;
                    } else {
                        pt2x = pt1x + polyline2Width;
                        pt2y = pt1y;
                        
                        if (dataSet.getYGravity() == LabelGravity.END) {
                            mValuePaint.setTextAlign(Paint.Align.LEFT);
                        } else {
                            mValuePaint.setTextAlign(Paint.Align.RIGHT);
                        }
                        
                        if (drawXOutside) {
                            mEntryLabelsPaint.setTextAlign(Paint.Align.LEFT);
                        }
                        
                        labelPtx = pt2x + offset;
                    }
                    
                    if (dataSet.getYGravity() == LabelGravity.END) {
                        labelPty = pt2y + textHeight / 2;
                        secondLabelPty = pt2y + secondTextHeight / 2;
                        labelPty2 = labelPty + textHeight;
                    } else if (dataSet.getYGravity() == LabelGravity.BOTTOM) {
                        labelPty = pt2y + textHeight + textHeightOffset;
                        secondLabelPty = pt2y - textHeightOffset / 2;
                        labelPty2 = pt2y + labelTextHeight / 2;
                    } else {
                        labelPty = pt2y - textHeightOffset;
                        secondLabelPty = pt2y + secondTextHeight + textHeightOffset / 2;
                        labelPty2 = pt2y + labelTextHeight / 2;
                    }
                    
                    if (dataSet.getValueLineColor() != ColorTemplate.COLOR_NONE) {
                        if (dataSet.isDrawDot()) {
                            c.drawCircle(pt3x, pt3y, dataSet.getDotRadius(), mValueLinePaint);
                        }
                        
                        c.drawLine(pt0x, pt0y, pt1x, pt1y, mValueLinePaint);
                        c.drawLine(pt1x, pt1y, pt2x, pt2y, mValueLinePaint);
                    }
                    
                    // draw everything, depending on settings
                    if (drawXOutside && drawYOutside) {
                        drawValue(c, text, labelPtx, labelPty, dataSet.getValueTextColor(j), dataSet.getValueTextSize());
                        if (dataSet.getYGravity() != LabelGravity.END && dataSet.isDrawYSecondValue()) {
                            drawValue(c, secondText, labelPtx, secondLabelPty, dataSet.getSecondValueTextColor(j), dataSet.getYSecondValueTextSize());
                        }
                        
                        if (j < data.getEntryCount() && entry.getLabel() != null) {
                            drawEntryLabel(c, entry.getLabel(), labelPtx, labelPty2);
                        }
                        
                    } else if (drawXOutside) {
                        if (j < data.getEntryCount() && entry.getLabel() != null) {
                            drawEntryLabel(c, entry.getLabel(), labelPtx, labelPty + labelTextHeight / 2.f);
                        }
                    } else if (drawYOutside) {
                        drawValue(c, text, labelPtx, labelPty, dataSet.getValueTextColor(j), dataSet.getValueTextSize());
                        if (dataSet.getYGravity() != LabelGravity.END && dataSet.isDrawYSecondValue()) {
                            drawValue(c, secondText, labelPtx, secondLabelPty, dataSet.getSecondValueTextColor(j), dataSet.getYSecondValueTextSize());
                        }
                    }
                }
                
                if (drawXInside || drawYInside) {
                    // calculate the text position
                    labelRadius = radius2 / dataSet.getValueInSideOffset() * 3.6f;
                    float x = labelRadius * sliceXBase + center.x;
                    float y = labelRadius * sliceYBase + center.y;
                    
                    mValuePaint.setTextAlign(Paint.Align.CENTER);
                    
                    // draw everything, depending on settings
                    if (drawXInside && drawYInside) {
                        drawValue(c, text, x, y, dataSet.getValueTextColor(j), dataSet.getValueTextSize());
                        
                        if (j < data.getEntryCount() && entry.getLabel() != null) {
                            drawEntryLabel(c, entry.getLabel(), x, y + textHeight);
                        }
                        
                    } else if (drawXInside) {
                        if (j < data.getEntryCount() && entry.getLabel() != null) {
                            drawEntryLabel(c, entry.getLabel(), x, y + textHeight / 2f);
                        }
                    } else if (drawYInside) {
                        drawValue(c, text, x, y + textHeight / 2f, dataSet.getValueTextColor(j), dataSet.getValueTextSize());
                    }
                }
                
                if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {
                    
                    Drawable icon = entry.getIcon();
                    
                    float x = (labelRadius + iconsOffset.y) * sliceXBase + center.x;
                    float y = (labelRadius + iconsOffset.y) * sliceYBase + center.y;
                    y += iconsOffset.x;
                    
                    Utils.drawImage(
                        c,
                        icon,
                        (int) x,
                        (int) y,
                        icon.getIntrinsicWidth(),
                        icon.getIntrinsicHeight());
                }
                
                xIndex++;
            }
            
            MPPointF.recycleInstance(iconsOffset);
        }
        MPPointF.recycleInstance(center);
        c.restore();
    }
    
    @Override
    public void drawValue(Canvas c, String valueText, float x, float y, int color) {
        mValuePaint.setColor(color);
        c.drawText(valueText, x, y, mValuePaint);
    }
    
    private void drawValue(Canvas c, String text, float x, float y, int color, float textSize) {
        mValuePaint.setTextSize(textSize);
        mValuePaint.setColor(color);
        c.drawText(text, x, y, mValuePaint);
    }
    
    /**
     * Draws an entry label at the specified position.
     */
    protected void drawEntryLabel(Canvas c, String label, float x, float y) {
        c.drawText(label, x, y, mEntryLabelsPaint);
    }
    
    @Override
    public void drawExtras(Canvas c) {
        if (isDrawBorder()) {
            drawBorder(c);
        }
        
        drawHole(c);
        c.drawBitmap(mDrawBitmap.get(), 0, 0, null);
        drawCenterText(c);
    }
    
    private void drawBorder(Canvas c) {
        float radius = mChart.getRadius();
        MPPointF center = mChart.getCenterCircleBox();
        mBoundsPaint.setColor(getBorderColor());
        mBoundsPaint.setStrokeWidth(getBorderWidth());
        mBoundsPaint.setPathEffect(mBorderPathEffect);
        c.drawCircle(center.x, center.y, radius + getBorderOffset(), mBoundsPaint);
        
        MPPointF.recycleInstance(center);
    }
    
    /**
     * draws the hole in the center of the chart and the transparent circle /
     * hole
     */
    protected void drawHole(Canvas c) {
        
        if (mChart.isDrawHoleEnabled() && mBitmapCanvas != null) {
            
            float radius = mChart.getRadius();
            float holeRadius = radius * (mChart.getHoleRadius() / 100);
            MPPointF center = mChart.getCenterCircleBox();
            
            if (Color.alpha(mHolePaint.getColor()) > 0) {
                // draw the hole-circle
                mBitmapCanvas.drawCircle(
                    center.x, center.y,
                    holeRadius, mHolePaint);
            }
            
            // only draw the circle if it can be seen (not covered by the hole)
            if (Color.alpha(mTransparentCirclePaint.getColor()) > 0 &&
                mChart.getTransparentCircleRadius() > mChart.getHoleRadius()) {
                
                int alpha = mTransparentCirclePaint.getAlpha();
                float secondHoleRadius = radius * (mChart.getTransparentCircleRadius() / 100);
                
                mTransparentCirclePaint.setAlpha((int) ((float) alpha * mAnimator.getPhaseX() * mAnimator.getPhaseY()));
                
                // draw the transparent-circle
                mHoleCirclePath.reset();
                mHoleCirclePath.addCircle(center.x, center.y, secondHoleRadius, Path.Direction.CW);
                mHoleCirclePath.addCircle(center.x, center.y, holeRadius, Path.Direction.CCW);
                mBitmapCanvas.drawPath(mHoleCirclePath, mTransparentCirclePaint);
                
                // reset alpha
                mTransparentCirclePaint.setAlpha(alpha);
            }
            MPPointF.recycleInstance(center);
        }
    }
    
    /**
     * draws the description text in the center of the pie chart makes most
     * sense when center-hole is enabled
     */
    protected void drawCenterText(Canvas c) {
        
        CharSequence centerText = mChart.getCenterText();
        
        if (mChart.isDrawCenterTextEnabled() && centerText != null) {
            
            MPPointF center = mChart.getCenterCircleBox();
            MPPointF offset = mChart.getCenterTextOffset();
            
            float x = center.x + offset.x;
            float y = center.y + offset.y;
            
            float innerRadius = mChart.isDrawHoleEnabled() && !mChart.isDrawSlicesUnderHoleEnabled()
                ? mChart.getRadius() * (mChart.getHoleRadius() / 100f)
                : mChart.getRadius();
            
            RectF holeRect = mRectFBuffer[0];
            holeRect.left = x - innerRadius;
            holeRect.top = y - innerRadius;
            holeRect.right = x + innerRadius;
            holeRect.bottom = y + innerRadius;
            RectF boundingRect = mRectFBuffer[1];
            boundingRect.set(holeRect);
            
            float radiusPercent = mChart.getCenterTextRadiusPercent() / 100f;
            if (radiusPercent > 0.0) {
                boundingRect.inset(
                    (boundingRect.width() - boundingRect.width() * radiusPercent) / 2.f,
                    (boundingRect.height() - boundingRect.height() * radiusPercent) / 2.f
                );
            }
            
            if (!centerText.equals(mCenterTextLastValue) || !boundingRect.equals(mCenterTextLastBounds)) {
                
                // Next time we won't recalculate StaticLayout...
                mCenterTextLastBounds.set(boundingRect);
                mCenterTextLastValue = centerText;
                
                float width = mCenterTextLastBounds.width();
                
                // If width is 0, it will crash. Always have a minimum of 1
                mCenterTextLayout = new StaticLayout(centerText, 0, centerText.length(),
                    mCenterTextPaint,
                    (int) Math.max(Math.ceil(width), 1.f),
                    Layout.Alignment.ALIGN_CENTER, 1.f, 0.f, false);
            }
            
            //float layoutWidth = Utils.getStaticLayoutMaxWidth(mCenterTextLayout);
            float layoutHeight = mCenterTextLayout.getHeight();
            
            c.save();
            if (Build.VERSION.SDK_INT >= 18) {
                Path path = mDrawCenterTextPathBuffer;
                path.reset();
                path.addOval(holeRect, Path.Direction.CW);
                c.clipPath(path);
            }
            
            c.translate(boundingRect.left, boundingRect.top + (boundingRect.height() - layoutHeight) / 2.f);
            mCenterTextLayout.draw(c);
            
            c.restore();
            
            MPPointF.recycleInstance(center);
            MPPointF.recycleInstance(offset);
        }
    }
    
    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {
        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();
        
        float angle;
        float rotationAngle = mChart.getRotationAngle();
        
        float[] drawAngles = mChart.getDrawAngles();
        float[] absoluteAngles = mChart.getAbsoluteAngles();
        final MPPointF center = mChart.getCenterCircleBox();
        final float radius = mChart.getRadius();
        final boolean drawInnerArc = mChart.isDrawHoleEnabled() && !mChart.isDrawSlicesUnderHoleEnabled();
        final float userInnerRadius = drawInnerArc
            ? radius * (mChart.getHoleRadius() / 100.f)
            : 0.f;
        
        for (int i = 0; i < indices.length; i++) {
            
            // get the index to highlight
            int index = (int) indices[i].getX();
            
            if (index >= drawAngles.length)
                continue;
            
            ISmartPieDataSet set = mChart.getData()
                .getDataSetByIndex(indices[i]
                    .getDataSetIndex());
            
            if (set == null || !set.isHighlightEnabled())
                continue;
            
            final int entryCount = set.getEntryCount();
            int visibleAngleCount = 0;
            for (int j = 0; j < entryCount; j++) {
                // draw only if the value is greater than zero
                if ((Math.abs(set.getEntryForIndex(j).getY()) > Utils.FLOAT_EPSILON)) {
                    visibleAngleCount++;
                }
            }
            
            if (index == 0)
                angle = 0.f;
            else
                angle = absoluteAngles[index - 1] * phaseX;
            
            final float sliceSpace = visibleAngleCount <= 1 ? 0.f : set.getSliceSpace();
            
            float sliceAngle = drawAngles[index];
            float innerRadius = userInnerRadius;
            float shift = set.getSelectionShift();
            float highlightedRadius = radius + shift;
            Entry e = set.getEntryForIndex(index);
            if (e instanceof SmartPieEntry && drawInnerArc && userInnerRadius > 0) {
                float arcWidth = ((SmartPieEntry) e).getArcWidth();
                if (arcWidth > 0) {
                    int drawBaseline = ((SmartPieEntry) e).getDrawBaseline();
                    if (drawBaseline == DrawBaseline.CENTER) {
                        float arcOffset = (radius - userInnerRadius - arcWidth) / 2;
                        if (arcOffset > 0) {
                            innerRadius += arcOffset;
                            highlightedRadius -= arcOffset;
                        }
                    } else if (drawBaseline == DrawBaseline.TOP) {
                        float temp = highlightedRadius - arcWidth;
                        innerRadius = temp < innerRadius ? innerRadius : temp;
                    } else {
                        float temp = innerRadius + arcWidth;
                        highlightedRadius = temp > highlightedRadius ? highlightedRadius : temp;
                    }
                }
            }
            
            final boolean accountForSliceSpacing = sliceSpace > 0.f && sliceAngle <= 180.f;
            
            mRenderPaint.setColor(set.getColor(index));
            
            final float sliceSpaceAngleOuter = visibleAngleCount == 1 ?
                0.f :
                sliceSpace / (Utils.FDEG2RAD * radius);
            
            final float sliceSpaceAngleShifted = visibleAngleCount == 1 ?
                0.f :
                sliceSpace / (Utils.FDEG2RAD * highlightedRadius);
            
            final float startAngleOuter = rotationAngle + (angle + sliceSpaceAngleOuter / 2.f) * phaseY;
            float sweepAngleOuter = (sliceAngle - sliceSpaceAngleOuter) * phaseY;
            if (sweepAngleOuter < 0.f) {
                sweepAngleOuter = 0.f;
            }
            
            final float startAngleShifted = rotationAngle + (angle + sliceSpaceAngleShifted / 2.f) * phaseY;
            float sweepAngleShifted = (sliceAngle - sliceSpaceAngleShifted) * phaseY;
            if (sweepAngleShifted < 0.f) {
                sweepAngleShifted = 0.f;
            }
            
            mPathBuffer.reset();
            
            if (sweepAngleOuter >= 360.f && sweepAngleOuter % 360f <= Utils.FLOAT_EPSILON) {
                // Android is doing "mod 360"
                mPathBuffer.addCircle(center.x, center.y, highlightedRadius, Path.Direction.CW);
            } else {
                
                mPathBuffer.moveTo(
                    center.x + highlightedRadius * (float) Math.cos(startAngleShifted * Utils.FDEG2RAD),
                    center.y + highlightedRadius * (float) Math.sin(startAngleShifted * Utils.FDEG2RAD));
                
                // API < 21 does not receive floats in addArc, but a RectF
                mOutRectFBuffer.set(
                    center.x - highlightedRadius,
                    center.y - highlightedRadius,
                    center.x + highlightedRadius,
                    center.y + highlightedRadius
                );
                mPathBuffer.arcTo(
                    mOutRectFBuffer,
                    startAngleShifted,
                    sweepAngleShifted
                );
            }
            
            float sliceSpaceRadius = 0.f;
            if (accountForSliceSpacing) {
                sliceSpaceRadius =
                    calculateMinimumRadiusForSpacedSlice(
                        center, radius,
                        sliceAngle * phaseY,
                        center.x + radius * (float) Math.cos(startAngleOuter * Utils.FDEG2RAD),
                        center.y + radius * (float) Math.sin(startAngleOuter * Utils.FDEG2RAD),
                        startAngleOuter,
                        sweepAngleOuter);
            }
            
            if (drawInnerArc &&
                (innerRadius > 0.f || accountForSliceSpacing)) {
                
                if (accountForSliceSpacing) {
                    float minSpacedRadius = sliceSpaceRadius;
                    
                    if (minSpacedRadius < 0.f)
                        minSpacedRadius = -minSpacedRadius;
                    
                    innerRadius = Math.max(innerRadius, minSpacedRadius);
                }
                
                final float sliceSpaceAngleInner = visibleAngleCount == 1 || innerRadius == 0.f ?
                    0.f :
                    sliceSpace / (Utils.FDEG2RAD * innerRadius);
                final float startAngleInner = rotationAngle + (angle + sliceSpaceAngleInner / 2.f) * phaseY;
                float sweepAngleInner = (sliceAngle - sliceSpaceAngleInner) * phaseY;
                if (sweepAngleInner < 0.f) {
                    sweepAngleInner = 0.f;
                }
                final float endAngleInner = startAngleInner + sweepAngleInner;
                
                if (sweepAngleOuter >= 360.f && sweepAngleOuter % 360f <= Utils.FLOAT_EPSILON) {
                    // Android is doing "mod 360"
                    mPathBuffer.addCircle(center.x, center.y, innerRadius, Path.Direction.CCW);
                } else {
                    
                    mPathBuffer.lineTo(
                        center.x + innerRadius * (float) Math.cos(endAngleInner * Utils.FDEG2RAD),
                        center.y + innerRadius * (float) Math.sin(endAngleInner * Utils.FDEG2RAD));
                    
                    // API < 21 does not receive floats in addArc, but a RectF
                    mInnerRectBuffer.set(
                        center.x - innerRadius,
                        center.y - innerRadius,
                        center.x + innerRadius,
                        center.y + innerRadius
                    );
                    mPathBuffer.arcTo(
                        mInnerRectBuffer,
                        endAngleInner,
                        -sweepAngleInner
                    );
                }
            } else {
                
                if (sweepAngleOuter % 360f > Utils.FLOAT_EPSILON) {
                    
                    if (accountForSliceSpacing) {
                        final float angleMiddle = startAngleOuter + sweepAngleOuter / 2.f;
                        
                        final float arcEndPointX = center.x +
                            sliceSpaceRadius * (float) Math.cos(angleMiddle * Utils.FDEG2RAD);
                        final float arcEndPointY = center.y +
                            sliceSpaceRadius * (float) Math.sin(angleMiddle * Utils.FDEG2RAD);
                        
                        mPathBuffer.lineTo(
                            arcEndPointX,
                            arcEndPointY);
                        
                    } else {
                        
                        mPathBuffer.lineTo(
                            center.x,
                            center.y);
                    }
                    
                }
                
            }
            
            mPathBuffer.close();
            
            mBitmapCanvas.drawPath(mPathBuffer, mRenderPaint);
        }
        
        MPPointF.recycleInstance(center);
    }
    
    /**
     * This gives all pie-slices a rounded edge.
     */
    protected void drawRoundedSlices(Canvas c) {
        
        if (!mChart.isDrawRoundedSlicesEnabled()) {
            return;
        }
        
        ISmartPieDataSet dataSet = mChart.getData().getDataSet();
        
        if (!dataSet.isVisible()) {
            return;
        }
        
        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();
        
        MPPointF center = mChart.getCenterCircleBox();
        float r = mChart.getRadius();
        
        // calculate the radius of the "slice-circle"
        float circleRadius = (r - (r * mChart.getHoleRadius() / 100f)) / 2f;
        
        float[] drawAngles = mChart.getDrawAngles();
        float angle = mChart.getRotationAngle();
        
        for (int j = 0; j < dataSet.getEntryCount(); j++) {
            
            float sliceAngle = drawAngles[j];
            
            Entry e = dataSet.getEntryForIndex(j);
            
            // draw only if the value is greater than zero
            if ((Math.abs(e.getY()) > Utils.FLOAT_EPSILON)) {
                
                float x = (float) ((r - circleRadius)
                    * Math.cos(Math.toRadians((angle + sliceAngle)
                    * phaseY)) + center.x);
                float y = (float) ((r - circleRadius)
                    * Math.sin(Math.toRadians((angle + sliceAngle)
                    * phaseY)) + center.y);
                
                mRenderPaint.setColor(dataSet.getColor(j));
                mBitmapCanvas.drawCircle(x, y, circleRadius, mRenderPaint);
            }
            
            angle += sliceAngle * phaseX;
        }
        MPPointF.recycleInstance(center);
    }
    
    /**
     * Releases the drawing bitmap. This should be called when {@link LineChart#onDetachedFromWindow()}.
     */
    public void releaseBitmap() {
        if (mBitmapCanvas != null) {
            mBitmapCanvas.setBitmap(null);
            mBitmapCanvas = null;
        }
        if (mDrawBitmap != null) {
            mDrawBitmap.get().recycle();
            mDrawBitmap.clear();
            mDrawBitmap = null;
        }
    }
    
    /**
     * 设置是否绘制外边框
     */
    public void setDrawBorder(boolean isDrawBorder) {
        mIsDrawBorder = isDrawBorder;
    }
    
    /**
     * 是否绘制外边框
     */
    public boolean isDrawBorder() {
        return mIsDrawBorder;
    }
    
    /**
     * 设置外边框的宽度
     */
    public void setBorderWidth(float borderWidth) {
        mBorderWidth = borderWidth;
    }
    
    /**
     * 获取外边框的宽度
     */
    public float getBorderWidth() {
        return mBorderWidth;
    }
    
    /**
     * 设置外边框的颜色
     */
    public void setBorderColor(@ColorInt int color) {
        mBorderColor = color;
    }
    
    /**
     * 获取外边框颜色
     */
    public int getBorderColor() {
        return mBorderColor;
    }
    
    /**
     * 设置外边框距离饼状图最外围的偏移值
     */
    public void setBorderOffset(float offset) {
        mBorderOffset = offset;
    }
    
    /**
     * 获取外边框距离饼状图最外围的偏移值
     */
    public float getBorderOffset() {
        return mBorderOffset;
    }
    
    /**
     * 设置饼状图外边框的路径效果，例如虚线实线，弯折等
     */
    public void setBorderPathEffect(PathEffect borderPathEffect) {
        this.mBorderPathEffect = borderPathEffect;
    }
}
