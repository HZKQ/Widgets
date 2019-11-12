/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.journeyapps.barcodescanner;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.R;

import java.util.ArrayList;
import java.util.List;

import me.zhouzhuo810.magpiex.utils.DisplayUtil;
import me.zhouzhuo810.magpiex.utils.ScreenAdapterUtil;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public class ViewfinderView extends View {
    protected static final String TAG = ViewfinderView.class.getSimpleName();
    
    protected static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    protected static final long ANIMATION_DELAY = 80L;
    protected static final int CURRENT_POINT_OPACITY = 0xA0;
    protected static final int MAX_RESULT_POINTS = 20;
    protected static final int POINT_SIZE = 6;
    
    protected final Paint paint;
    protected final Paint textPaint;
    protected final Paint rectPaint;
    protected Bitmap resultBitmap;
    protected final int maskColor;
    protected final int resultColor;
    protected final int laserColor;
    protected final int resultPointColor;
    protected int scannerAlpha;
    protected List<ResultPoint> possibleResultPoints;
    protected List<ResultPoint> lastPossibleResultPoints;
    protected CameraPreview cameraPreview;
    
    protected String hintText = getResources().getString(R.string.zxing_msg_default_status);
    
    // Cache the framingRect and previewFramingRect, so that we can still draw it after the preview
    // stopped.
    protected Rect framingRect;
    protected Rect previewFramingRect;
    
    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        textPaint.setTextSize(SimpleUtil.getScaledValue(46));
        textPaint.setColor(Color.WHITE);
        rectPaint.setColor(Color.WHITE);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(SimpleUtil.getScaledValue(6));
        
        Resources resources = getResources();
        
        // Get setted attributes on view
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.zxing_finder);
        
        this.maskColor = attributes.getColor(R.styleable.zxing_finder_zxing_viewfinder_mask,
            resources.getColor(R.color.zxing_viewfinder_mask));
        this.resultColor = attributes.getColor(R.styleable.zxing_finder_zxing_result_view,
            resources.getColor(R.color.zxing_result_view));
        this.laserColor = attributes.getColor(R.styleable.zxing_finder_zxing_viewfinder_laser,
            resources.getColor(R.color.zxing_viewfinder_laser));
        this.resultPointColor = attributes.getColor(R.styleable.zxing_finder_zxing_possible_result_points,
            resources.getColor(R.color.zxing_possible_result_points));
        
        attributes.recycle();
        
        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<>(MAX_RESULT_POINTS);
        lastPossibleResultPoints = new ArrayList<>(MAX_RESULT_POINTS);
    }
    
    public void setCameraPreview(CameraPreview view) {
        this.cameraPreview = view;
        view.addStateListener(new CameraPreview.StateListener() {
            @Override
            public void previewSized() {
                refreshSizes();
                invalidate();
            }
            
            @Override
            public void previewStarted() {
            
            }
            
            @Override
            public void previewStopped() {
            
            }
            
            @Override
            public void cameraError(Exception error) {
            
            }
            
            @Override
            public void cameraClosed() {
            
            }
        });
    }
    
    protected void refreshSizes() {
        if (cameraPreview == null) {
            return;
        }
        Rect framingRect = cameraPreview.getFramingRect();
        Rect previewFramingRect = cameraPreview.getPreviewFramingRect();
        if (framingRect != null && previewFramingRect != null) {
            this.framingRect = framingRect;
            this.previewFramingRect = previewFramingRect;
        }
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        refreshSizes();
        if (framingRect == null || previewFramingRect == null) {
            return;
        }
        
        //扫描框大小
        final Rect frame = framingRect;
        //预览窗体大小
        final Rect previewFrame = previewFramingRect;
        
        final int width = canvas.getWidth();
        final int height = canvas.getHeight();
        
        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);
        
        //画四角边框
        drawCorner(canvas, frame);
        //画提示文字
        drawText(canvas, frame);
        
        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            
            // Draw a red "laser scanner" line through the middle to show decoding is active
            paint.setColor(laserColor);
            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            final int middle = frame.height() / 2 + frame.top;
            canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint);
            
            final float scaleX = frame.width() / (float) previewFrame.width();
            final float scaleY = frame.height() / (float) previewFrame.height();
            
            final int frameLeft = frame.left;
            final int frameTop = frame.top;
            
            // draw the last possible result points
            if (!lastPossibleResultPoints.isEmpty()) {
                paint.setAlpha(CURRENT_POINT_OPACITY / 2);
                paint.setColor(resultPointColor);
                float radius = POINT_SIZE / 2.0f;
                for (final ResultPoint point : lastPossibleResultPoints) {
                    if (point == null) {
                        continue;
                    }
                    canvas.drawCircle(
                        frameLeft + (int) (point.getX() * scaleX),
                        frameTop + (int) (point.getY() * scaleY),
                        radius, paint
                    );
                }
                lastPossibleResultPoints.clear();
            }
            
            // draw current possible result points
            if (!possibleResultPoints.isEmpty()) {
                paint.setAlpha(CURRENT_POINT_OPACITY);
                paint.setColor(resultPointColor);
                for (final ResultPoint point : possibleResultPoints) {
                    if (point == null) {
                        continue;
                    }
                    canvas.drawCircle(
                        frameLeft + (int) (point.getX() * scaleX),
                        frameTop + (int) (point.getY() * scaleY),
                        POINT_SIZE, paint
                    );
                }
                
                // swap and clear buffers
                final List<ResultPoint> temp = possibleResultPoints;
                possibleResultPoints = lastPossibleResultPoints;
                lastPossibleResultPoints = temp;
                possibleResultPoints.clear();
            }
            
            // Request another update at the animation interval, but only repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(ANIMATION_DELAY,
                frame.left - POINT_SIZE,
                frame.top - POINT_SIZE,
                frame.right + POINT_SIZE,
                frame.bottom + POINT_SIZE);
        }
    }
    
    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }
    
    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param result An image of the result.
     */
    public void drawResultBitmap(Bitmap result) {
        resultBitmap = result;
        invalidate();
    }
    
    /**
     * Only call from the UI thread.
     *
     * @param point a point to draw, relative to the preview frame
     */
    public void addPossibleResultPoint(ResultPoint point) {
        if (possibleResultPoints.size() < MAX_RESULT_POINTS)
            possibleResultPoints.add(point);
    }
    
    
    /**
     * 画四个角
     */
    private void drawCorner(Canvas canvas, Rect rect) {
        Path leftTopPath = new Path();
        final int lineLength = ScreenAdapterUtil.getInstance().getScaledValue(40);
        leftTopPath.moveTo(rect.left + lineLength, rect.top);
        leftTopPath.lineTo(rect.left, rect.top);
        leftTopPath.lineTo(rect.left, rect.top + lineLength);
        canvas.drawPath(leftTopPath, rectPaint);
        
        Path rightTop = new Path();
        rightTop.moveTo(rect.right - lineLength, rect.top);
        rightTop.lineTo(rect.right, rect.top);
        rightTop.lineTo(rect.right, rect.top + lineLength);
        canvas.drawPath(rightTop, rectPaint);
        
        Path leftBottom = new Path();
        leftBottom.moveTo(rect.left + lineLength, rect.bottom);
        leftBottom.lineTo(rect.left, rect.bottom);
        leftBottom.lineTo(rect.left, rect.bottom - lineLength);
        canvas.drawPath(leftBottom, rectPaint);
        
        Path rightBottom = new Path();
        rightBottom.moveTo(rect.right - lineLength, rect.bottom);
        rightBottom.lineTo(rect.right, rect.bottom);
        rightBottom.lineTo(rect.right, rect.bottom - lineLength);
        canvas.drawPath(rightBottom, rectPaint);
    }
    
    /**
     * 设置提示文字
     */
    public void setHintText(String hintText) {
        this.hintText = hintText;
    }
    
    /**
     * 画提示文字
     */
    private void drawText(Canvas canvas, Rect rect) {
        final String text = hintText;
        if (text != null) {
            final int lineLength = ScreenAdapterUtil.getInstance().getScaledValue(40);
            float textWidth = textPaint.measureText(text);
            int screenWidth = DisplayUtil.getScreenWidth();
            if (textWidth > screenWidth) {
                int length = text.length();
                int halfLength = length / 2;
                String left = text.substring(0, halfLength);
                String right = text.substring(halfLength);
                float textWidth1 = textPaint.measureText(left);
                float textWidth2 = textPaint.measureText(right);
                float textHeight = textPaint.descent() - textPaint.ascent();
                canvas.drawText(left, rect.left + rect.width() * 1.0f / 2 - textWidth1 / 2, rect.bottom + lineLength + textHeight, textPaint);
                canvas.drawText(right, rect.left + rect.width() * 1.0f / 2 - textWidth2 / 2, rect.bottom + lineLength + textHeight + textHeight, textPaint);
            } else {
                float textHeight = textPaint.descent() - textPaint.ascent();
                canvas.drawText(text, rect.left + rect.width() * 1.0f / 2 - textWidth / 2, rect.bottom + lineLength + textHeight, textPaint);
            }
        }
    }
    
}
