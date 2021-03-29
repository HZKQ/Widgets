
package com.github.mikephil.charting.components;

import android.graphics.Color;
import android.graphics.Typeface;

import com.github.mikephil.charting.utils.Utils;

/**
 * This class encapsulates everything both Axis, Legend and LimitLines have in common.
 *
 * @author Philipp Jahoda
 */
public abstract class ComponentBase {
    
    /**
     * flag that indicates if this axis / legend is enabled or not
     */
    protected boolean mEnabled = true;
    
    /**
     * the offset in pixels this component has on the x-axis
     */
    protected float mXOffset = 5f;
    
    /**
     * the offset in pixels this component has on the Y-axis
     */
    protected float mYOffset = 5f;
    
    /**
     * the typeface used for the labels
     */
    protected Typeface mTypeface = null;
    
    /**
     * the text size of the labels
     */
    protected float mTextSize = Utils.convertDpToPixel(10f);
    
    /**
     * the text color to use for the labels
     */
    protected int mTextColor = Color.BLACK;
    
    
    public ComponentBase() {
    
    }
    
    /**
     * Returns the used offset on the x-axis for drawing the axis or legend
     * labels. This offset is applied before and after the label.
     */
    public float getXOffset() {
        return mXOffset;
    }
    
    /**
     * Sets the used x-axis offset for the labels on this axis.
     */
    public void setXOffset(float xOffset) {
        mXOffset = Utils.convertDpToPixel(xOffset);
    }
    
    /**
     * Sets the used x-axis offset for the labels on this axis in px.
     */
    public void setXOffsetInPx(float xOffset) {
        mXOffset = xOffset;
    }
    
    /**
     * Returns the used offset on the x-axis for drawing the axis labels. This
     * offset is applied before and after the label.
     */
    public float getYOffset() {
        return mYOffset;
    }
    
    /**
     * Sets the used y-axis offset for the labels on this axis. For the legend,
     * higher offset means the legend as a whole will be placed further away
     * from the top.
     */
    @Deprecated
    public void setYOffset(float yOffset) {
        mYOffset = Utils.convertDpToPixel(yOffset);
    }

    public void setYOffsetInPx(float yOffset) {
        mYOffset = yOffset;
    }
    
    /**
     * returns the Typeface used for the labels, returns null if none is set
     */
    public Typeface getTypeface() {
        return mTypeface;
    }
    
    /**
     * sets a specific Typeface for the labels
     */
    public void setTypeface(Typeface tf) {
        mTypeface = tf;
    }
    
    /**
     * sets the size of the label text in density pixels min = 6f, max = 24f, default
     * 10f
     * 推荐使用{@link #setTextSizeInPx(float)}
     * @param size the text size, in DP
     */
    @Deprecated
    public void setTextSize(float size) {
        
        if (size > 24f)
            size = 24f;
        if (size < 6f)
            size = 6f;
        
        mTextSize = Utils.convertDpToPixel(size);
    }
    
    /**
     * 使用px单位设置文字大小
     *
     * @param size px
     */
    public void setTextSizeInPx(float size) {
        mTextSize = size;
    }
    
    /**
     * returns the text size that is currently set for the labels, in pixels
     */
    public float getTextSize() {
        return mTextSize;
    }
    
    
    /**
     * Sets the text color to use for the labels. Make sure to use
     * getResources().getColor(...) when using a color from the resources.
     */
    public void setTextColor(int color) {
        mTextColor = color;
    }
    
    /**
     * Returns the text color that is set for the labels.
     */
    public int getTextColor() {
        return mTextColor;
    }
    
    /**
     * Set this to true if this component should be enabled (should be drawn),
     * false if not. If disabled, nothing of this component will be drawn.
     * Default: true
     */
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }
    
    /**
     * Returns true if this comonent is enabled (should be drawn), false if not.
     */
    public boolean isEnabled() {
        return mEnabled;
    }
}
