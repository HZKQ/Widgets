package com.keqiang.progressbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * 圆环形进度条
 *
 * @author Created by 汪高皖 on 2018/4/10 0010 10:11
 */
public class RingProgress extends View {
    private float mMax;
    private float mProgress;
    
    private int mWidth;
    private int mHeight;
    
    /**
     * 用了画圆的画笔
     */
    private Paint mHolePaint;
    
    /**
     * 用来画圆环的画笔
     */
    private Paint mArcBgPaint;
    
    /**
     * 用来画扇形的画笔
     */
    private Paint mArcPaint;
    
    private Paint mBorderPaint;
    
    /**
     * 中心圆的颜色
     */
    private int mHoleColor;
    
    /**
     * 圆环背景色
     */
    private int mRingBg;
    
    /**
     * 圆环颜色
     */
    private int mRingColor;
    
    /**
     * 圆环宽度
     */
    private float mRingWidth;
    
    /**
     * 旋转角度
     */
    private float mRotateAngle;
    
    /**
     * 是否绘制中心圆
     */
    private boolean mDrawHole;
    
    /**
     * 绘制圆环的最大角度
     */
    private float mMaxAngle;
    
    /**
     * 圆环的样式
     */
    private int style;
    
    private boolean mDrawBorder;
    private float mBorderWidth;
    private int mBorderColor;
    private float mBorderOffset;
    
    private RectF[] mRectBuffer;
    private Path mDrawCenterTextPathBuffer;
    private boolean mDrawCenterText;
    private RectF mCenterTextLastBounds;
    private StaticLayout mCenterTextLayout;
    private TextPaint mCenterTextPaint;
    private CharSequence mCenterTextLastValue;
    private CharSequence mCenterText;
    private int mCenterTextColor;
    private float mCenterTextSize;
    private float mCenterTextRadiusPercent;
    
    /**
     * 用来保存渐变色
     */
    private int[] mRingColors;
    private float[] mRingColorsWeight;
    private ValueAnimator mValueAnimator;
    
    public RingProgress(Context context) {
        this(context, null);
    }
    
    public RingProgress(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public RingProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RingProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        mRectBuffer = new RectF[]{new RectF(), new RectF(), new RectF(), new RectF()};
        mCenterTextLastBounds = new RectF();
        mDrawCenterTextPathBuffer = new Path();
        
        mArcBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcBgPaint.setStyle(Paint.Style.STROKE);
        
        mArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcPaint.setStyle(Paint.Style.STROKE);
        
        mHolePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHolePaint.setStyle(Paint.Style.FILL);
        
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        
        mCenterTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        
        if (attrs == null) {
            mMax = 100;
            mProgress = 0;
            mMaxAngle = 360f;
            mRotateAngle = 0;
            
            style = Style.RECT;
            
            mDrawHole = false;
            mHoleColor = Color.TRANSPARENT;
            
            mRingBg = Color.DKGRAY;
            mRingColor = Color.BLUE;
            mRingWidth = 10;
            
            mDrawCenterText = false;
            mCenterTextColor = Color.BLACK;
            mCenterTextSize = 32;
            mCenterTextRadiusPercent = 100f;
            
            mDrawBorder = false;
            mBorderWidth = 10;
            mBorderColor = Color.GRAY;
            mBorderOffset = 10;
        } else {
            TypedArray t = null;
            try {
                t = context.obtainStyledAttributes(attrs, R.styleable.RingProgress);
                mMax = t.getFloat(R.styleable.RingProgress_rp_max, 100);
                mProgress = t.getFloat(R.styleable.RingProgress_rp_progress, 0);
                mHoleColor = t.getColor(R.styleable.RingProgress_rp_hole_color, Color.TRANSPARENT);
                mDrawHole = t.getBoolean(R.styleable.RingProgress_rp_drawHole, false);
                mRingBg = t.getColor(R.styleable.RingProgress_rp_ring_background, Color.DKGRAY);
                mRingColor = t.getColor(R.styleable.RingProgress_rp_ring_color, Color.BLUE);
                mRingWidth = t.getDimensionPixelSize(R.styleable.RingProgress_rp_ring_width, 10);
                mMaxAngle = t.getFloat(R.styleable.RingProgress_rp_max_angle, 360);
                mRotateAngle = t.getFloat(R.styleable.RingProgress_rp_rotate_angle, 0);
                mDrawCenterText = t.getBoolean(R.styleable.RingProgress_rp_drawCenterText, false);
                mCenterText = t.getString(R.styleable.RingProgress_rp_centerText);
                mCenterTextColor = t.getColor(R.styleable.RingProgress_rp_centerTextColor, Color.BLACK);
                mCenterTextRadiusPercent = t.getFloat(R.styleable.RingProgress_rp_centerText_radius_percent, 80f);
                mCenterTextSize = t.getDimensionPixelSize(R.styleable.RingProgress_rp_centerTextSize, 32);
                style = t.getInt(R.styleable.RingProgress_rp_ring_style, Style.RECT);
                mDrawBorder = t.getBoolean(R.styleable.RingProgress_rp_draw_border, false);
                mBorderColor = t.getColor(R.styleable.RingProgress_rp_border_color, Color.GRAY);
                mBorderWidth = t.getDimensionPixelSize(R.styleable.RingProgress_rp_border_width, 10);
                mBorderOffset = t.getDimensionPixelSize(R.styleable.RingProgress_rp_border_offset, 10);
            } finally {
                if (t != null) {
                    t.recycle();
                }
            }
        }
        
        setRotateAngle(mRotateAngle);
        if (!isInEditMode()) {
            mRingWidth = SimpleUtil.getScaledValue(mRingWidth);
            mCenterTextSize = SimpleUtil.getScaledValue(mCenterTextSize, true);
            mBorderWidth = SimpleUtil.getScaledValue(mBorderWidth);
            mBorderOffset = SimpleUtil.getScaledValue(mBorderOffset);
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int radius = (int) (getRadius() - mRingWidth);
        if (mDrawBorder) {
            radius -= (mBorderOffset + mBorderWidth);
        }
        if (radius < 0) {
            radius = 0;
        }
        
        mHolePaint.setColor(mHoleColor);
        int centerX = mWidth / 2;
        int centerY = mHeight / 2;
        
        if (mDrawHole) {
            canvas.drawCircle(centerX, centerY, radius, mHolePaint);
        }
        
        if (mDrawCenterText) {
            drawCenterText(canvas);
        }
        
        drawRing(canvas, radius, centerX, centerY);
    }
    
    private void drawRing(Canvas canvas, int radius, int centerX, int centerY) {
        if (mDrawBorder) {
            mBorderPaint.setColor(mBorderColor);
            mBorderPaint.setStrokeWidth(mBorderWidth);
            
            RectF rectF = mRectBuffer[3];
            rectF.left = centerX - radius - mRingWidth - mBorderOffset;
            rectF.right = centerX + radius + mRingWidth + mBorderOffset;
            rectF.top = centerY - radius - mRingWidth - mBorderOffset;
            rectF.bottom = centerY + radius + mRingWidth + mBorderOffset;
            canvas.drawArc(rectF, -90 + mRotateAngle, mMaxAngle, false, mBorderPaint);
        }
        
        mArcBgPaint.setColor(mRingBg);
        mArcPaint.setColor(mRingColor);
        
        mArcBgPaint.setStrokeWidth(mRingWidth);
        mArcPaint.setStrokeWidth(mRingWidth);
        
        // 根据progress绘制进度条圆环
        mProgress = Math.min(mProgress, mMax);
        float angle = mProgress / mMax * mMaxAngle;
        RectF f = mRectBuffer[0];
        f.left = centerX - radius;
        f.right = centerX + radius;
        f.top = centerY - radius;
        f.bottom = centerY + radius;
        
        if (style == Style.RECT) {
            mArcBgPaint.setStrokeCap(Paint.Cap.BUTT);
            mArcPaint.setStrokeCap(Paint.Cap.BUTT);
        } else {
            mArcBgPaint.setStrokeCap(Paint.Cap.ROUND);
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
        }
        
        if (mRingColors != null) {
            float[] positions = null;
            if (mRingColorsWeight != null) {
                float sum = 0;
                for (float v : mRingColorsWeight) {
                    sum += v;
                }
                
                positions = new float[mRingColorsWeight.length];
                float prePosition = 0;
                for (int i = 0; i < mRingColorsWeight.length; i++) {
                    positions[i] = mRingColorsWeight[i] / sum * mMaxAngle / 360 + prePosition;
                    if (positions[i] > 1) {
                        positions[i] = 1;
                    }
                    prePosition = positions[i];
                }
            }
            
            SweepGradient gradient = new SweepGradient(centerX, centerY, mRingColors, positions);
            Matrix matrix = new Matrix();
            // 加上旋转,因为绘制圆弧时发生了旋转，否则渐变将出现问题
            if (style == Style.RECT) {
                matrix.setRotate(-90 + mRotateAngle, centerX, centerX);
            } else {
                // 切角为圆，则渐变的旋转角度需要往后覆盖突出的圆形
                // 圆环进度条1度所占周长的长度
                double v = getRadius() * Math.PI * 2 / 360;
                // 计算突出的圆形直径相当于圆环进度条移动几度的长度
                double v1 = mRingWidth / v;
                matrix.setRotate(-90 + mRotateAngle - (float) v1, centerX, centerX);
            }
            gradient.setLocalMatrix(matrix);
            mArcPaint.setShader(gradient);
        } else {
            mArcPaint.setShader(null);
        }
        
        canvas.drawArc(f, -90 + mRotateAngle, mMaxAngle, false, mArcBgPaint);
        canvas.drawArc(f, -90 + mRotateAngle, angle, false, mArcPaint);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mValueAnimator != null && mValueAnimator.isStarted()) {
            mValueAnimator.cancel();
            mValueAnimator = null;
        }
    }
    
    private void drawCenterText(Canvas c) {
        mCenterTextPaint.setColor(mCenterTextColor);
        mCenterTextPaint.setTextSize(mCenterTextSize);
        
        float x = mWidth / 2f;
        float y = mHeight / 2f;
        
        float innerRadius = (int) (getRadius() - mRingWidth);
        
        RectF holeRect = mRectBuffer[1];
        holeRect.left = x - innerRadius;
        holeRect.top = y - innerRadius;
        holeRect.right = x + innerRadius;
        holeRect.bottom = y + innerRadius;
        RectF boundingRect = mRectBuffer[2];
        boundingRect.set(holeRect);
        
        float radiusPercent = mCenterTextRadiusPercent / 100f;
        if (radiusPercent > 0.0) {
            boundingRect.inset(
                (boundingRect.width() - boundingRect.width() * radiusPercent) / 2.f,
                (boundingRect.height() - boundingRect.height() * radiusPercent) / 2.f
            );
        }
        
        CharSequence centerText = mCenterText == null ? String.valueOf(getProgress()) : mCenterText;
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
    }
    
    /**
     * 获取圆环半径
     */
    public int getRadius() {
        return Math.min(mWidth / 2, mHeight / 2);
    }
    
    /**
     * 设置圆环最大值
     */
    public void setMax(float max) {
        mMax = max;
        invalidate();
    }
    
    /**
     * 获取圆环最大值
     */
    public float getMax() {
        return mMax;
    }
    
    /**
     * 设置圆环当前进度值
     */
    public void setProgress(float progress) {
        mProgress = progress;
        invalidate();
    }
    
    /**
     * 获取圆环当前进度值
     */
    public float getProgress() {
        return mProgress;
    }
    
    /**
     * 设置圆环中心文本字体
     */
    public void setCenterTextTypeface(Typeface typeface) {
        mCenterTextPaint.setTypeface(typeface);
    }
    
    /**
     * 设置圆环中心文本
     */
    public void setCenterText(CharSequence centerText) {
        mCenterText = centerText;
        invalidate();
    }
    
    /**
     * 获取圆环中心文本
     */
    public CharSequence getCenterText() {
        return mCenterText;
    }
    
    /**
     * 设置是否绘制中心圆
     *
     * @param drawHole {@code false} 不绘制，默认值
     */
    public void setDrawHole(boolean drawHole) {
        mDrawHole = drawHole;
        invalidate();
    }
    
    /**
     * 是否绘制中心圆
     */
    public boolean isDrawHole() {
        return mDrawHole;
    }
    
    /**
     * 设置中心圆颜色
     *
     * @param resId 颜色资源Id
     */
    public void setHoleColorRes(@ColorRes int resId) {
        setHoleColor(ContextCompat.getColor(getContext(), resId));
    }
    
    /**
     * 设置中心圆颜色
     *
     * @param color 颜色值
     */
    public void setHoleColor(@ColorInt int color) {
        mHoleColor = color;
        invalidate();
    }
    
    /**
     * 获取中心圆颜色
     */
    public int getHoleColor() {
        return mHoleColor;
    }
    
    /**
     * 设置圆环背景色
     *
     * @param resId 颜色资源Id
     */
    public void setRingBackgroundRes(@ColorRes int resId) {
        setRingBackground(ContextCompat.getColor(getContext(), resId));
    }
    
    /**
     * 设置圆环背景色
     *
     * @param color 颜色值
     */
    public void setRingBackground(@ColorInt int color) {
        mRingBg = color;
        invalidate();
    }
    
    /**
     * 设置圆环颜色
     *
     * @param resId 颜色资源Id
     */
    public void setRingColorRes(@ColorRes int resId) {
        setRingColor(ContextCompat.getColor(getContext(), resId));
    }
    
    /**
     * 设置圆环颜色
     *
     * @param color 颜色值
     */
    public void setRingColor(@ColorInt int color) {
        mRingColor = color;
        invalidate();
    }
    
    /**
     * 设置圆环渐变色,如果{@link #mMaxAngle}设置的是360，建议最后一个颜色值和第一个值相同，这样有更好的渐变
     */
    public void setRingColors(int[] colors) {
        mRingColors = colors;
        invalidate();
    }
    
    /**
     * 设置圆环渐变色,如果{@link #mMaxAngle}设置的是360，建议最后一个颜色值和第一个值相同，这样有更好的渐变
     */
    public void setRingColors2(int... colors) {
        mRingColors = colors;
        invalidate();
    }
    
    /**
     * 设置渐变色所占的比重
     *
     * @param colorsWeight 为NULL则所有颜色比重一致
     */
    public void setRingColorsWeight(float[] colorsWeight) {
        mRingColorsWeight = colorsWeight;
        invalidate();
    }
    
    /**
     * 设置圆环的宽度
     *
     * @param width 单位px
     */
    public void setRingWidth(float width) {
        mRingWidth = width;
        invalidate();
    }
    
    /**
     * 设置旋转角度,默认从顶部顺时针绘制
     *
     * @param angle 0~360
     */
    public void setRotateAngle(float angle) {
        if (angle < 0) {
            angle = 0;
        } else if (angle > 360) {
            angle = 360;
        }
        mRotateAngle = angle;
        invalidate();
    }
    
    /**
     * 设置是否绘制圆环边框
     */
    public void setDrawBorder(boolean drawBorder) {
        mDrawBorder = drawBorder;
        invalidate();
    }
    
    /**
     * 设置圆环边框宽度
     */
    public void setBorderWidth(float width) {
        mBorderWidth = width;
        invalidate();
    }
    
    /**
     * 设置圆环边框颜色
     */
    public void setBorderColorRes(@ColorRes int resId) {
        setBorderColor(ContextCompat.getColor(getContext(), resId));
    }
    
    /**
     * 设置圆环边框颜色
     */
    public void setBorderColor(@ColorInt int color) {
        mBorderColor = color;
        invalidate();
    }
    
    /**
     * 设置圆环边框路径效果
     */
    public void setBorderPathEffect(PathEffect pathEffect) {
        mBorderPaint.setPathEffect(pathEffect);
        invalidate();
    }
    
    /**
     * 设置圆环边框与圆环之间的偏移值
     */
    public void setBorderOffset(float offset) {
        mBorderOffset = offset;
        invalidate();
    }
    
    /**
     * 执行进度动画
     */
    public void animate(long duration, ProgressChangeListener listener) {
        if (mValueAnimator != null && mValueAnimator.isStarted()) {
            mValueAnimator.cancel();
        }
        
        mValueAnimator = ValueAnimator.ofFloat(0, 1f);
        mValueAnimator.setDuration(duration);
        final float progress = getProgress();
        mValueAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            setProgress(value * progress);
            if (listener != null) {
                listener.onChange(getProgress());
            }
        });
        mValueAnimator.start();
    }
    
    /**
     * 当yValuePosition == PieDataSet.ValuePosition.OUTSIDE_SLICE时，用于标识文本所绘制的位置
     */
    @IntDef({Style.RECT, Style.ROUND})
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Style {
        /**
         * 矩形切角
         */
        int RECT = 0;
        
        /**
         * 圆弧切角
         */
        int ROUND = 1;
    }
    
    /**
     * 执行动画时，进度变化监听
     */
    public interface ProgressChangeListener {
        void onChange(float progress);
    }
}
