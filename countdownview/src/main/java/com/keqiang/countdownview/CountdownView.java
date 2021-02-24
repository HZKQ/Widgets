package com.keqiang.countdownview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * 倒计时
 *
 * @author Created by 汪高皖 on 2018/8/1 0001 08:29
 */
public class CountdownView extends View {
    public static final IValueFormat DEFAULT_VALUE_FORMAT = second -> second + "s";
    
    private int mWidth;
    private int mHeight;
    
    private int mDuration;
    private int mCurDuration;
    private Paint mPaint;
    private RectF mRectF;
    
    private float mTextSize;
    private int mTextColor;
    private int mBorderColor;
    private float mBorderWidth;
    private ValueAnimator mAnimator;
    private CountdownListener mCountdownListener;
    private IValueFormat mIValueFormat = DEFAULT_VALUE_FORMAT;
    
    /**
     * 是否反向，如果反向，则从完整的圆到圆消失
     */
    private boolean mReverse;
    
    /**
     * 是否点击跳过
     */
    private boolean mClickEnd;
    
    public CountdownView(Context context) {
        super(context);
        init(context, null);
    }
    
    public CountdownView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public CountdownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CountdownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectF = new RectF();
        
        if (attrs != null) {
            TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.CountdownView);
            mTextSize = t.getDimensionPixelSize(R.styleable.CountdownView_cdv_text_size, 40);
            mTextColor = t.getColor(R.styleable.CountdownView_cdv_text_color, Color.BLACK);
            mBorderWidth = t.getDimensionPixelSize(R.styleable.CountdownView_cdv_border_width, 3);
            mBorderColor = t.getColor(R.styleable.CountdownView_cdv_border_color, Color.BLACK);
            t.recycle();
        } else {
            mTextColor = Color.BLACK;
            mBorderColor = Color.BLACK;
            mTextSize = 40;
            mBorderWidth = 3;
        }
        
        if (!isInEditMode()) {
            mTextSize = SimpleUtil.getScaledValue((int) mTextSize);
            mBorderWidth = SimpleUtil.getScaledValue((int) mBorderWidth);
        } else {
            mDuration = 3 * 1000;
            mCurDuration = mDuration;
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInEditMode() && mAnimator == null) {
            return;
        }
        
        if (mDuration > 0) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mBorderWidth);
            mPaint.setColor(mBorderColor);
            if (mReverse || isInEditMode()) {
                canvas.drawArc(mRectF, 270, 360f * mCurDuration / mDuration, false, mPaint);
            } else {
                canvas.drawArc(mRectF, 270, 360 * (1f - mCurDuration * 1f / mDuration), false, mPaint);
            }
        }
        
        String ceil = mIValueFormat.format((int) Math.ceil(mCurDuration / 1000d));
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(mTextSize);
        mPaint.setStyle(Paint.Style.FILL);
        float textWidth = mPaint.measureText(ceil);
        Paint.FontMetrics metrics = mPaint.getFontMetrics();
        float textHeight = metrics.ascent + metrics.descent;
        canvas.drawText(ceil, mWidth / 2f - textWidth / 2, mHeight / 2f - textHeight / 2, mPaint);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        
        int min = Math.min(mWidth, mHeight);
        float left = (mWidth - min) / 2f;
        float top = (mHeight - min) / 2f;
        mRectF.set(left + mBorderWidth, top + mBorderWidth, min + left - mBorderWidth, min + top - mBorderWidth);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            if (widthMode == MeasureSpec.UNSPECIFIED) {
                widthSize = getPaddingStart() + getPaddingEnd();
            } else {
                widthSize = Math.min(widthSize, getPaddingStart() + getPaddingEnd());
            }
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        }
        
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            if (heightMode == MeasureSpec.UNSPECIFIED) {
                heightSize = getPaddingStart() + getPaddingEnd();
            } else {
                heightSize = Math.min(heightSize, getPaddingStart() + getPaddingEnd());
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
            mAnimator = null;
        }
    }
    
    /**
     * 开始倒计时
     *
     * @param second            倒计时时长，单位S
     * @param countdownListener 倒计时监听
     */
    public void startCountdown(int second, final CountdownListener countdownListener) {
        if (second <= 0) {
            return;
        }
        
        mClickEnd = false;
        mCountdownListener = countdownListener;
        mDuration = second * 1000;
        mCurDuration = mDuration;
        mAnimator = ValueAnimator.ofInt(mDuration);
        mAnimator.setDuration(mDuration);
        mAnimator.addUpdateListener(animation -> {
            mCurDuration = mDuration - (int) animation.getAnimatedValue();
            invalidate();
            if (mCurDuration == 0 && mCountdownListener != null) {
                mCountdownListener.onEnd();
            }
        });
        mAnimator.start();
    }
    
    /**
     * 结束倒计时，调用此方法将直接触发{@link CountdownListener#onEnd()}
     */
    public void endCountDown() {
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
            mAnimator = null;
        }
        
        if (mClickEnd) {
            return;
        }
        
        mClickEnd = true;
        if (mCountdownListener != null) {
            mCountdownListener.onEnd();
        }
    }
    
    /**
     * 倒计时文本大小
     *
     * @param textSize 单位px
     */
    public void setTextSize(float textSize) {
        this.mTextSize = textSize;
        invalidate();
    }
    
    /**
     * 倒计时文本颜色
     */
    public void setTextColor(@ColorInt int textColor) {
        this.mTextColor = textColor;
        invalidate();
    }
    
    /**
     * 倒计时圆圈颜色
     */
    public void setBorderColor(@ColorInt int borderColor) {
        this.mBorderColor = borderColor;
        invalidate();
    }
    
    /**
     * 倒计时圆圈线条宽度
     *
     * @param borderWidth 单位px
     */
    public void setBorderWidth(float borderWidth) {
        this.mBorderWidth = borderWidth;
        invalidate();
    }
    
    /**
     * 设置倒计时圆圈是否反转绘制
     *
     * @param reverse {@code true}：圆圈从有到无，{@code false}：圆圈从无到有
     */
    public void setReverse(boolean reverse) {
        mReverse = reverse;
        invalidate();
    }
    
    /**
     * 倒计时圆圈是否反转绘制
     */
    public boolean isReverse() {
        return mReverse;
    }
    
    /**
     * 设置值格式化类
     */
    public void setValueFormat(IValueFormat IValueFormat) {
        mIValueFormat = IValueFormat;
        invalidate();
    }
}
