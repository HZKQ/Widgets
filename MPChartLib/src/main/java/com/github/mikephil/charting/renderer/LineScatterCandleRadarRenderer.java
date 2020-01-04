package com.github.mikephil.charting.renderer;

import android.graphics.Canvas;
import android.graphics.Path;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.interfaces.datasets.ILineScatterCandleRadarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Created by Philipp Jahoda on 11/07/15.
 */
public abstract class LineScatterCandleRadarRenderer extends BarLineScatterCandleBubbleRenderer {

    /**
     * path that is used for drawing highlight-lines (drawLines(...) cannot be used because of dashes)
     */
    private Path mHighlightLinePath = new Path();

    public LineScatterCandleRadarRenderer(ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
    }

    /**
     * Draws vertical & horizontal highlight-lines if enabled.
     *
     * @param c
     * @param x x-position of the highlight line intersection
     * @param y y-position of the highlight line intersection
     * @param set the currently drawn dataset
     */
    protected void drawHighlightLines(Canvas c, float x, float y, ILineScatterCandleRadarDataSet set) {

        // set color and stroke-width
        mHighlightPaint.setColor(set.getHighLightColor());
        mHighlightPaint.setStrokeWidth(set.getHighlightLineWidth());

        // draw highlighted lines (if enabled)
        mHighlightPaint.setPathEffect(set.getDashPathEffectHighlight());

        // draw vertical highlight lines
        if (set.isVerticalHighlightIndicatorEnabled()) {
        
            // create vertical path
            mHighlightLinePath.reset();
            //TODO by 汪高皖 2018/9/7 0007 17:46 需要做的内容：后续加入支持判断x轴位置以及是否上下x轴都启用时的绘制方案
            mHighlightLinePath.moveTo(x, set.isOverVerticalHighlightIndicatorEnd() ? mViewPortHandler.contentTop() : y);
            mHighlightLinePath.lineTo(x, mViewPortHandler.contentBottom());
        
            c.drawPath(mHighlightLinePath, mHighlightPaint);
        }
    
        // draw horizontal highlight lines
        if (set.isHorizontalHighlightIndicatorEnabled()) {
        
            // create horizontal path
            mHighlightLinePath.reset();
            mHighlightLinePath.moveTo(mViewPortHandler.contentLeft(), y);
            //TODO by 汪高皖 2018/9/7 0007 17:46 需要做的内容：后续加入支持判断y轴位置以及是否左右y轴都启用时的绘制方案
            mHighlightLinePath.lineTo(set.isOverHorizontalHighlightIndicatorEnd() ? mViewPortHandler.contentRight() : x, y);
        
            c.drawPath(mHighlightLinePath, mHighlightPaint);
        }
    }
}
