package com.github.mikephil.charting.custom.piechart;

import android.graphics.Color;
import android.view.Gravity;

import androidx.annotation.ColorInt;

import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Created by 汪高皖 on 2018/4/16 0016 14:31
 */
public class SmartPieDataSet extends DataSet<SmartPieEntry> implements ISmartPieDataSet {
    /**
     * the space in pixels between the chart-slices, default 0f
     */
    private float mSliceSpace = 0f;
    private boolean mAutomaticallyDisableSliceSpacing;
    
    /**
     * indicates the selection distance of a pie slice
     */
    private float mShift = 18f;
    
    private ValuePosition mXValuePosition = ValuePosition.INSIDE_SLICE;
    private ValuePosition mYValuePosition = ValuePosition.INSIDE_SLICE;
    private int mValueLineColor = 0xff000000;
    private float mValueLineWidth = 1.0f;
    private float mValueLinePart1OffsetPercentage = 75.f;
    private float mValueLinePart1Length = 0.3f;
    private float mValueLinePart2Length = 0.4f;
    private boolean mValueLineVariableLength = true;
    
    private boolean mDrawDot;
    private float mDotRadius;
    private boolean mValueLinePart2LengthFollowTextWidth;
    private int mYGravity = Gravity.END;
    private boolean mDrawYSecondValue = false;
    private float mYSecondValueSize;
    private int mYSecondValueColor = Color.BLACK;
    private List<Integer> mYSecondValueColors;
    private List<Integer> mValueLineColors;
    private boolean mDrawHighlightYValue;
    private boolean mDrawHighlightXValue;
    private float mValueInSideOffset = 0.6f;
    
    private SmartPieChartValueFormatter mSmartValueFormatter;
    
    public SmartPieDataSet(List<SmartPieEntry> yVals, String label) {
        super(yVals, label);
    }
    
    @Override
    public DataSet<SmartPieEntry> copy() {
        
        List<SmartPieEntry> yVals = new ArrayList<>();
        
        for (int i = 0; i < mValues.size(); i++) {
            yVals.add(mValues.get(i).copy());
        }
        
        SmartPieDataSet copied = new SmartPieDataSet(yVals, getLabel());
        copied.mColors = mColors;
        copied.mSliceSpace = mSliceSpace;
        copied.mShift = mShift;
        return copied;
    }
    
    @Override
    protected void calcMinMax(SmartPieEntry e) {
        
        if (e == null) {
            return;
        }
        
        calcMinMaxY(e);
    }
    
    /**
     * Sets the space that is left out between the piechart-slices in dp.
     * Default: 0 --> no space, maximum 20f
     */
    public void setSliceSpace(float spaceDp) {
        
        if (spaceDp > 20) {
            spaceDp = 20f;
        }
        if (spaceDp < 0) {
            spaceDp = 0f;
        }
        
        mSliceSpace = Utils.convertDpToPixel(spaceDp);
    }
    
    @Override
    public float getSliceSpace() {
        return mSliceSpace;
    }
    
    /**
     * When enabled, slice spacing will be 0.0 when the smallest value is going to be
     * smaller than the slice spacing itself.
     */
    public void setAutomaticallyDisableSliceSpacing(boolean autoDisable) {
        mAutomaticallyDisableSliceSpacing = autoDisable;
    }
    
    /**
     * When enabled, slice spacing will be 0.0 when the smallest value is going to be
     * smaller than the slice spacing itself.
     */
    @Override
    public boolean isAutomaticallyDisableSliceSpacingEnabled() {
        return mAutomaticallyDisableSliceSpacing;
    }
    
    /**
     * sets the distance the highlighted piechart-slice of this DataSet is
     * "shifted" away from the center of the chart, default 12f
     */
    public void setSelectionShift(float shift) {
        mShift = Utils.convertDpToPixel(shift);
    }
    
    /**
     * sets the distance the highlighted piechart-slice of this DataSet is
     * "shifted" away from the center of the chart, default 12f
     */
    public void setSelectionShiftWithPixcel(float shift) {
        mShift = shift;
    }
    
    @Override
    public float getSelectionShift() {
        return mShift;
    }
    
    @Override
    public ValuePosition getXValuePosition() {
        return mXValuePosition;
    }
    
    public void setXValuePosition(ValuePosition xValuePosition) {
        this.mXValuePosition = xValuePosition;
    }
    
    @Override
    public ValuePosition getYValuePosition() {
        return mYValuePosition;
    }
    
    public void setYValuePosition(ValuePosition yValuePosition) {
        this.mYValuePosition = yValuePosition;
    }
    
    /**
     * When valuePosition is OutsideSlice, indicates line color
     */
    @Override
    public int getValueLineColor() {
        return mValueLineColor;
    }
    
    public void setValueLineColor(int valueLineColor) {
        this.mValueLineColor = valueLineColor;
    }
    
    /**
     * When valuePosition is OutsideSlice, indicates line width
     */
    @Override
    public float getValueLineWidth() {
        return mValueLineWidth;
    }
    
    public void setValueLineWidth(float valueLineWidth) {
        this.mValueLineWidth = valueLineWidth;
    }
    
    /**
     * When valuePosition is OutsideSlice, indicates offset as percentage out of the slice size
     */
    @Override
    public float getValueLinePart1OffsetPercentage() {
        return mValueLinePart1OffsetPercentage;
    }
    
    public void setValueLinePart1OffsetPercentage(float valueLinePart1OffsetPercentage) {
        this.mValueLinePart1OffsetPercentage = valueLinePart1OffsetPercentage;
    }
    
    /**
     * When valuePosition is OutsideSlice, indicates length of first half of the line
     */
    @Override
    public float getValueLinePart1Length() {
        return mValueLinePart1Length;
    }
    
    public void setValueLinePart1Length(float valueLinePart1Length) {
        this.mValueLinePart1Length = valueLinePart1Length;
    }
    
    /**
     * When valuePosition is OutsideSlice, indicates length of second half of the line
     */
    @Override
    public float getValueLinePart2Length() {
        return mValueLinePart2Length;
    }
    
    public void setValueLinePart2Length(float valueLinePart2Length) {
        this.mValueLinePart2Length = valueLinePart2Length;
    }
    
    /**
     * When valuePosition is OutsideSlice, this allows variable line length
     */
    @Override
    public boolean isValueLineVariableLength() {
        return mValueLineVariableLength;
    }
    
    public void setValueLineColors(@ColorInt int... colors) {
        mValueLineColors = new ArrayList<>();
        for (int i : colors) {
            mValueLineColors.add(i);
        }
    }
    
    public void setValueLineColors(List<Integer> colors) {
        mValueLineColors = colors;
    }
    
    @Override
    public int getValueLineColor(int index) {
        if (mValueLineColors == null) {
            return getValueLineColor();
        }
        
        return mValueLineColors.get(index % mValueLineColors.size());
    }
    
    /**
     * 是否绘制用于展示值的线条与饼状图之间的圆点
     */
    public void setDrawDot(boolean drawDot) {
        mDrawDot = drawDot;
    }
    
    @Override
    public boolean isDrawDot() {
        return mDrawDot;
    }
    
    /**
     * 设置展示值的线条与饼状图之间圆点的半径
     */
    public void setDotRadius(float dotRadius) {
        mDotRadius = dotRadius;
    }
    
    @Override
    public float getDotRadius() {
        return mDotRadius;
    }
    
    /**
     * 设置第二段线条长度是否跟随文本宽度，如果跟随，则{@link #mValueLinePart2Length}则是相对于文本宽度的相对百分比
     */
    public void setValueLinePart2LengthFollowTextWidth(boolean followTextWidth) {
        mValueLinePart2LengthFollowTextWidth = followTextWidth;
    }
    
    @Override
    public boolean isValueLinePart2LengthFollowTextWidth() {
        return mValueLinePart2LengthFollowTextWidth;
    }
    
    public void setSmartValueFormatter(SmartPieChartValueFormatter formatter) {
        mSmartValueFormatter = formatter;
    }
    
    @Override
    public SmartPieChartValueFormatter getSmartValueFormatter() {
        return mSmartValueFormatter;
    }
    
    /**
     * 设置Y轴方向值绘制位置
     *
     * @param yGravity 参考{@link Gravity}
     */
    public void setYGravity(@LabelGravity int yGravity) {
        mYGravity = yGravity;
    }
    
    @Override
    public int getYGravity() {
        return mYGravity;
    }
    
    public void setYSecondValueTextSize(int size) {
        mYSecondValueSize = size;
    }
    
    @Override
    public float getYSecondValueTextSize() {
        return mYSecondValueSize;
    }
    
    public void setYSecondValueTextColor(@ColorInt int color) {
        mYSecondValueColor = color;
    }
    
    public void setYSecondValueTextColors(@ColorInt int... colors) {
        mYSecondValueColors = new ArrayList<>();
        for (int i : colors) {
            mYSecondValueColors.add(i);
        }
    }
    
    public void setYSecondValueTextColors(List<Integer> colors) {
        mYSecondValueColors = colors;
    }
    
    @Override
    public int getSecondValueTextColor() {
        return mYSecondValueColor;
    }
    
    @Override
    public int getSecondValueTextColor(int index) {
        if (mYSecondValueColors == null) {
            return getSecondValueTextColor();
        }
        
        return mYSecondValueColors.get(index % mYSecondValueColors.size());
    }
    
    /**
     * 设置高亮选中时是否绘制value值
     */
    public void setDrawHighlightYValue(boolean drawHighlightYValue) {
        mDrawHighlightYValue = drawHighlightYValue;
    }
    
    @Override
    public boolean isDrawHighlightYValue() {
        return mDrawHighlightYValue;
    }
    
    /**
     * 设置高亮选中时是否绘制label值
     */
    public void setDrawHighlightXValue(boolean drawHighlightXValue) {
        mDrawHighlightXValue = drawHighlightXValue;
    }
    
    @Override
    public boolean isDrawHighlightXValue() {
        return mDrawHighlightXValue;
    }
    
    /**
     * 如果设置绘制值的位置为{@link SmartPieDataSet.ValuePosition#INSIDE_SLICE}，
     * 此值用于设置离圆心的偏移位置
     *
     * @param valueInSideOffset 0~1
     */
    public void setValueInSideOffset(float valueInSideOffset) {
        mValueInSideOffset = valueInSideOffset;
    }
    
    @Override
    public float getValueInSideOffset() {
        return mValueInSideOffset;
    }
    
    @Override
    public SmartPieEntry getEntryForIndex(int index) {
        return super.getEntryForIndex(index);
    }
    
    /**
     * 设置是否绘制Y轴第二个值，根据{@link #setYGravity(int)}决定绘制的位置
     */
    public void setDrawYSecondValue(boolean drawYSecondValue) {
        mDrawYSecondValue = drawYSecondValue;
    }
    
    @Override
    public boolean isDrawYSecondValue() {
        return mDrawYSecondValue;
    }
    
    public void setValueLineVariableLength(boolean valueLineVariableLength) {
        this.mValueLineVariableLength = valueLineVariableLength;
    }
    
    
    public enum ValuePosition {
        /**
         * 值绘制在饼状图内部
         */
        INSIDE_SLICE,
        
        /**
         * 值绘制在饼状图外围
         */
        OUTSIDE_SLICE
    }
}
