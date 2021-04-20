package com.keqiang.seekbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * 双向滑动控件
 */
public class TwoWaySeekBar extends View {
    public static final int PROGRESS_TEXT_GRAVITY_TOP = 0;
    public static final int PROGRESS_TEXT_GRAVITY_BOTTOM = 1;
    
    @IntDef({PROGRESS_TEXT_GRAVITY_TOP, PROGRESS_TEXT_GRAVITY_BOTTOM})
    @Target({ElementType.PARAMETER})
    @Retention(RetentionPolicy.SOURCE)
    private @interface ProgressTextGravity {}
    
    private static final int CLICK_ON_START = 1; // 手指在前滑块上滑动
    private static final int CLICK_ON_END = 2; // 手指在后滑块上滑动
    private static final int CLICK_IN_START_AREA = 3; // 手指点击离前滑块近
    private static final int CLICK_IN_END_AREA = 4; // 手指点击离后滑块近
    private static final int CLICK_OUT_AREA = 5; // 手指点击在view外
    private static final int CLICK_INVALID = 0;
    private static final int[] STATE_NORMAL = {};
    private static final int[] STATE_PRESSED = {
        android.R.attr.state_pressed, android.R.attr.state_window_focused,
    };
    
    private static final ProgressTextValueFormat DEFAULT_VALUE_FORMAT = value -> value + "%";
    
    /**
     * 左滑块到View左边的滑动条背景
     */
    private Drawable mStartScrollBar;
    
    /**
     * 左滑块到右滑块中间的滑动条背景
     */
    private Drawable mCenterScrollBar;
    
    /**
     * 右滑块到View右边的滑动条背景
     */
    private Drawable mEndScrollBar;
    
    /**
     * 左滑块
     */
    private Drawable mThumbStart;
    
    /**
     * 右滑块
     */
    private Drawable mThumbEnd;
    
    /**
     * 左滑块点击背景
     */
    private Drawable mThumbStartBg;
    
    /**
     * 右滑块点击背景
     */
    private Drawable mThumbEndBg;
    
    /**
     * 滑动条高度
     */
    private int mScrollBarHeight;
    
    /**
     * 滑动块直径R
     */
    private int mThumbSize;
    
    /**
     * 滑块背景相比滑块缩放比例
     */
    private float mThumbBgScale = 1.1f;
    
    /**
     * 用户是否主动设置了滑块size
     */
    private boolean mUserSetThumbSize;
    
    /**
     * 左滑块中心坐标
     */
    private double mOffsetStart = 0;
    
    /**
     * 右滑块中心坐标
     */
    private double mOffsetEnd = 0;
    
    /**
     * 总刻度是固定距离 两边各去掉半个滑块距离
     */
    private int mDistance;
    
    /**
     * 手指按下的类型
     */
    private int mFlag = CLICK_INVALID;
    
    /**
     * 最大值
     */
    private int max = 100;
    
    /**
     * 默认左滑块位置百分比
     */
    private int mProgressStart = 0;
    
    /**
     * 默认右滑块位置百分比
     */
    private int mProgressEnd = 100;
    
    /**
     * 滑动监听
     */
    private OnSeekBarChangeListener mBarChangeListener;
    
    /**
     * 多项监听
     */
    private OnSeekBarMultiChangeListener mBarMultiChangeListener;
    
    /**
     * 用于绘制滑块位置值
     */
    private Paint mTextPaint;
    private boolean mShowProgressText = true;
    private int mProgressTextGravity = PROGRESS_TEXT_GRAVITY_BOTTOM;
    private int mProgressTextMargin;
    private ProgressTextValueFormat mProgressTextValueFormat;
    
    // 记录用户手指按下位置以及是否需要调整当前点击滑块是左滑块还是右滑块
    private float mDownX;
    private boolean mClickAdjustWho;
    
    public TwoWaySeekBar(Context context) {
        this(context, null);
    }
    
    public TwoWaySeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public TwoWaySeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        int textColor;
        int textSize;
        mThumbStartBg = ContextCompat.getDrawable(context, R.drawable.bg_circle_gray_alpha);
        mThumbEndBg = ContextCompat.getDrawable(context, R.drawable.bg_circle_gray_alpha);
        if (attrs != null) {
            TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.TwoWaySeekBar);
            mStartScrollBar = t.getDrawable(R.styleable.TwoWaySeekBar_ts_start_bar);
            mCenterScrollBar = t.getDrawable(R.styleable.TwoWaySeekBar_ts_center_bar);
            mEndScrollBar = t.getDrawable(R.styleable.TwoWaySeekBar_ts_end_bar);
            mThumbStart = t.getDrawable(R.styleable.TwoWaySeekBar_ts_thumb);
            mThumbEnd = t.getDrawable(R.styleable.TwoWaySeekBar_ts_thumb);
            mThumbSize = t.getDimensionPixelSize(R.styleable.TwoWaySeekBar_ts_thumb_size, -1);
            mScrollBarHeight = t.getDimensionPixelSize(R.styleable.TwoWaySeekBar_ts_bar_height, 20);
            mShowProgressText = t.getBoolean(R.styleable.TwoWaySeekBar_ts_show_progress_text, true);
            mProgressTextGravity = t.getInt(R.styleable.TwoWaySeekBar_ts_progress_text_gravity, PROGRESS_TEXT_GRAVITY_BOTTOM);
            textSize = t.getDimensionPixelSize(R.styleable.TwoWaySeekBar_ts_progress_text_size, 36);
            textColor = t.getColor(R.styleable.TwoWaySeekBar_ts_progress_text_color, 0xCC000000);
            mProgressTextMargin = t.getDimensionPixelSize(R.styleable.TwoWaySeekBar_ts_progress_text_margin, 10);
            max = t.getInteger(R.styleable.TwoWaySeekBar_ts_max, 100);
            if (isAttributeMatch(attrs, "ts_thumb_bg")) {
                mThumbStartBg = t.getDrawable(R.styleable.TwoWaySeekBar_ts_thumb_bg);
                mThumbEndBg = t.getDrawable(R.styleable.TwoWaySeekBar_ts_thumb_bg);
            }
            
            mThumbBgScale = t.getFloat(R.styleable.TwoWaySeekBar_ts_thumb_bg_scale, 1.1f);
            t.recycle();
        } else {
            textSize = 36;
            // 黑色
            textColor = 0xCC000000;
            mScrollBarHeight = 20;
            mThumbSize = -1;
            mProgressTextMargin = 10;
        }
        mUserSetThumbSize = mThumbSize != -1;
        mProgressEnd = max;
        
        if (!isInEditMode()) {
            textSize = SimpleUtil.getScaledValue(textSize, true);
            mScrollBarHeight = SimpleUtil.getScaledValue(mScrollBarHeight);
            mProgressTextMargin = SimpleUtil.getScaledValue(mProgressTextMargin);
        }
        
        if (mStartScrollBar == null) {
            mStartScrollBar = ContextCompat.getDrawable(context, R.drawable.bg_round_rectangle_260dp_yellow);
        }
        
        if (mCenterScrollBar == null) {
            mCenterScrollBar = ContextCompat.getDrawable(context, R.drawable.bg_round_rectangle_260dp_blue);
        }
        
        if (mEndScrollBar == null) {
            mEndScrollBar = ContextCompat.getDrawable(context, R.drawable.bg_round_rectangle_260dp_green);
        }
        
        if (mThumbStart == null) {
            mThumbStart = ContextCompat.getDrawable(context, R.drawable.huadong);
        }
        
        if (mThumbEnd == null) {
            mThumbEnd = ContextCompat.getDrawable(context, R.drawable.huadong);
        }
        
        if (mThumbSize == -1) {
            mThumbSize = mThumbStart.getIntrinsicWidth();
        }
        mThumbStart.setState(STATE_NORMAL);
        mThumbEnd.setState(STATE_NORMAL);
        
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(textSize);
        mProgressTextValueFormat = DEFAULT_VALUE_FORMAT;
    }
    
    /**
     * 判断给定的name属性是否在xml中配置
     *
     * @param name 需要判断的属性
     */
    private boolean isAttributeMatch(AttributeSet attrs, String name) {
        if (attrs == null) {
            return false;
        }
        
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            String attributeName = attrs.getAttributeName(i);
            if (attributeName != null && attributeName.equals(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            if (widthMode == MeasureSpec.UNSPECIFIED) {
                widthSize = getPaddingStart() + getPaddingEnd() + mThumbSize + mStartScrollBar.getIntrinsicWidth();
            } else {
                widthSize = Math.min(widthSize, getPaddingStart() + getPaddingEnd() + mThumbSize + mStartScrollBar.getIntrinsicWidth());
            }
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        }
        
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            int height = Math.max(mThumbSize, mScrollBarHeight);
            if (mShowProgressText) {
                Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
                float measuredHeight = fontMetrics.descent - fontMetrics.ascent;
                height += (mProgressTextMargin + measuredHeight) * 2;
            }
            
            if (heightMode == MeasureSpec.UNSPECIFIED) {
                heightSize = height + getPaddingTop() + getPaddingBottom();
            } else {
                heightSize = Math.min(heightSize, height + getPaddingTop() + getPaddingBottom());
            }
            
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        }
        
        mDistance = widthSize - getPaddingStartInner() - getPaddingEndInner() - mThumbSize;
        if (mProgressStart != 0) {
            mOffsetStart = mProgressStart * 1f / max * mDistance + mThumbSize / 2f + getPaddingStartInner();
        } else {
            mOffsetStart = mThumbSize / 2f + getPaddingStartInner();
        }
        
        if (mProgressEnd != max) {
            mOffsetEnd = mProgressEnd * 1f / max * mDistance + mThumbSize / 2f + getPaddingStartInner();
        } else {
            mOffsetEnd = widthSize - getPaddingEndInner() - mThumbSize / 2f;
        }
        
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    /**
     * 在用户设置基础上为点击绘制滑块背景预览放大的宽度，否则滑动到两边时绘制不全
     */
    private int getPaddingStartInner() {
        if (mThumbBgScale <= 1 || (mThumbStartBg == null && mThumbEndBg == null)) {
            return getPaddingStart();
        }
        
        return (int) (getPaddingStart() + mThumbSize * (mThumbBgScale - 1) / 2);
    }
    
    /**
     * 在用户设置基础上为点击绘制滑块背景预览放大的宽度，否则滑动到两边时绘制不全
     */
    private int getPaddingEndInner() {
        if (mThumbBgScale <= 1 || (mThumbStartBg == null && mThumbEndBg == null)) {
            return getPaddingStart();
        }
        
        return (int) (getPaddingEnd() + mThumbSize * (mThumbBgScale - 1) / 2);
    }
    
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.clipRect(getPaddingStart(), getPaddingTop(), getWidth() - getPaddingEnd(), getHeight() - getPaddingBottom());
        
        // 滑动条高度绘制起始和结束
        int top = (getHeight() - mScrollBarHeight) / 2;
        int bottom = top + mScrollBarHeight;
        
        // 左边滑动条部分
        mStartScrollBar.setBounds(mThumbSize / 2 + getPaddingStartInner(), top, (int) mOffsetStart, bottom);
        mStartScrollBar.draw(canvas);
        
        // 中间滑动条部分
        mCenterScrollBar.setBounds((int) mOffsetStart, top, (int) mOffsetEnd, bottom);
        mCenterScrollBar.draw(canvas);
        
        // 右边滑动条部分
        mEndScrollBar.setBounds((int) mOffsetEnd, top, getWidth() - getPaddingEndInner() - mThumbSize / 2, bottom);
        mEndScrollBar.draw(canvas);
        
        // 滑块高度绘制起始和结束
        top = (getHeight() - mThumbSize) / 2;
        bottom = top + mThumbSize;
        
        // 前滑块
        int[] state = mThumbStart.getState();
        if (isInEditMode() || state == STATE_PRESSED || (mOffsetEnd == mOffsetStart && mThumbEnd.getState() == STATE_PRESSED)) {
            drawThumbBg(canvas, mThumbStartBg, mOffsetStart);
        }
        mThumbStart.setBounds((int) (mOffsetStart - mThumbSize / 2), top, (int) (mOffsetStart + mThumbSize / 2), bottom);
        mThumbStart.draw(canvas);
        
        if (mOffsetEnd != mOffsetStart) {
            // 后滑块
            state = mThumbEnd.getState();
            if (isInEditMode() || state == STATE_PRESSED) {
                drawThumbBg(canvas, mThumbEndBg, mOffsetEnd);
            }
            mThumbEnd.setBounds((int) (mOffsetEnd - mThumbSize / 2), top, (int) (mOffsetEnd + mThumbSize / 2), bottom);
            mThumbEnd.draw(canvas);
        }
        
        // 当前滑块刻度
        if (mShowProgressText) {
            int y;
            Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
            float measuredHeight = fontMetrics.descent - fontMetrics.ascent;
            if (mProgressTextGravity == PROGRESS_TEXT_GRAVITY_TOP) {
                y = mScrollBarHeight > mThumbSize ? (getHeight() - mScrollBarHeight) / 2 : (getHeight() - mThumbSize) / 2;
                y -= mProgressTextMargin - measuredHeight / 2;
            } else {
                y = mScrollBarHeight > mThumbSize ? (getHeight() + mScrollBarHeight) / 2 : (getHeight() + mThumbSize) / 2;
                y += mProgressTextMargin + measuredHeight / 2;
            }
            
            String format = mProgressTextValueFormat.format(mProgressStart);
            if (!TextUtils.isEmpty(format)) {
                canvas.drawText(format, (int) mOffsetStart - 2 - 2, y, mTextPaint);
            }
            
            if (mProgressStart != mProgressEnd) {
                format = mProgressTextValueFormat.format(mProgressEnd);
                if (!TextUtils.isEmpty(format)) {
                    canvas.drawText(format, (int) mOffsetEnd - 2, y, mTextPaint);
                }
            }
        }
        
        canvas.restore();
    }
    
    /**
     * 绘制滑块背景
     */
    private void drawThumbBg(Canvas canvas, Drawable bg, double offset) {
        if (bg == null) {
            return;
        }
        
        int bgSize = mThumbBgScale == 1 ? mThumbSize : (int) (mThumbSize * mThumbBgScale);
        int bgTop = (getHeight() - bgSize) / 2;
        int bgBottom = bgTop + bgSize;
        int left = (int) (offset - bgSize / 2);
        int right = (int) (offset + bgSize / 2);
        bg.setBounds(left, bgTop, right, bgBottom);
        bg.draw(canvas);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.dispatchTouchEvent(event);
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (BuildConfig.DEBUG) {
            Log.e(TwoWaySeekBar.class.getSimpleName(), "action:" + e.getAction());
        }
        
        // 按下
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            mFlag = getAreaFlag(e);
            mDownX = e.getX();
            mClickAdjustWho = true;
            if (mFlag == CLICK_ON_START) {
                mThumbStart.setState(STATE_PRESSED);
                refresh();
                if (mBarMultiChangeListener != null) {
                    mBarMultiChangeListener.onStartTrackingTouch(this, true);
                }
            } else if (mFlag == CLICK_ON_END) {
                mThumbEnd.setState(STATE_PRESSED);
                refresh();
                if (mBarMultiChangeListener != null) {
                    mBarMultiChangeListener.onStartTrackingTouch(this, false);
                }
            } else if (mFlag == CLICK_IN_START_AREA) {
                mThumbStart.setState(STATE_PRESSED);
                mThumbEnd.setState(STATE_NORMAL);
                // 如果点击0-mThumbSize/2坐标
                if (e.getX() <= mThumbSize / 2f + getPaddingStartInner()) {
                    updateOffsetStart(mThumbSize / 2f + getPaddingStartInner());
                } else if (e.getX() > getWidth() - getPaddingEndInner() - mThumbSize / 2f) {
                    updateOffsetStart(mThumbSize / 2f + mDistance + getPaddingStartInner());
                    updateOffsetEnd(mOffsetStart);
                } else {
                    updateOffsetStart(formatInt(e.getX()));
                }
                refresh();
            } else if (mFlag == CLICK_IN_END_AREA) {
                mThumbEnd.setState(STATE_PRESSED);
                mThumbStart.setState(STATE_NORMAL);
                if (e.getX() <= mThumbSize / 2f + getPaddingStartInner()) {
                    updateOffsetStart(mThumbSize / 2f + getPaddingStartInner());
                    updateOffsetEnd(mOffsetStart);
                } else if (e.getX() > getWidth() - getPaddingEndInner() - mThumbSize / 2f) {
                    updateOffsetEnd(mThumbSize / 2f + mDistance + getPaddingStartInner());
                } else {
                    updateOffsetEnd(formatInt(e.getX()));
                }
                refresh();
            }
        } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
            // 移动move
            if (mClickAdjustWho && mOffsetStart == mOffsetEnd && mFlag == CLICK_ON_START && e.getX() > mDownX) {
                mFlag = CLICK_ON_END;
                mThumbEnd.setState(STATE_PRESSED);
                mThumbStart.setState(STATE_NORMAL);
            }
            
            if (e.getX() != mDownX) {
                mClickAdjustWho = false;
            }
            
            if (mFlag == CLICK_ON_START) {
                if (e.getX() <= mThumbSize / 2f + getPaddingStartInner()) {
                    updateOffsetStart(mThumbSize / 2f + getPaddingStartInner());
                } else if (e.getX() >= getWidth() - getPaddingEndInner() - mThumbSize / 2f) {
                    updateOffsetStart(mThumbSize / 2f + mDistance + getPaddingStartInner());
                    updateOffsetEnd(mOffsetStart);
                } else {
                    updateOffsetStart(formatInt(e.getX()));
                    if (mOffsetEnd - mOffsetStart <= 0) {
                        updateOffsetEnd((mOffsetStart <= mDistance + mThumbSize / 2f + getPaddingStartInner())
                            ? mOffsetStart : (mDistance + mThumbSize / 2f + getPaddingStartInner()));
                    }
                }
                
                refresh();
            } else if (mFlag == CLICK_ON_END) {
                if (e.getX() <= mThumbSize / 2f + getPaddingStartInner()) {
                    updateOffsetStart(mThumbSize / 2f + getPaddingStartInner());
                    updateOffsetEnd(mOffsetStart);
                } else if (e.getX() >= getWidth() - getPaddingEndInner() - mThumbSize / 2f) {
                    updateOffsetEnd(mThumbSize / 2f + mDistance + getPaddingStartInner());
                } else {
                    updateOffsetEnd(formatInt(e.getX()));
                    if (mOffsetEnd - mOffsetStart <= 0) {
                        updateOffsetStart((mOffsetEnd >= mThumbSize / 2f + getPaddingStartInner()) ? mOffsetEnd : mThumbSize / 2f + getPaddingStartInner());
                    }
                }
                
                refresh();
            }
        } else if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) {
            // 抬起
            mThumbStart.setState(STATE_NORMAL);
            mThumbEnd.setState(STATE_NORMAL);
            refresh();
            if (mBarMultiChangeListener != null) {
                mBarMultiChangeListener.onStopTrackingTouch(this);
            }
        }
        return true;
    }
    
    // 获取当前手指位置
    private int getAreaFlag(MotionEvent e) {
        int top = (getHeight() - mThumbSize) / 2;
        int bottom = mThumbSize + top;
        if (e.getY() >= top && e.getY() <= bottom && e.getX() >= (mOffsetStart - mThumbSize / 2f) && e.getX() <= mOffsetStart + mThumbSize / 2f) {
            return CLICK_ON_START;
        } else if (e.getY() >= top && e.getY() <= bottom && e.getX() >= (mOffsetEnd - mThumbSize / 2f) && e.getX() <= (mOffsetEnd + mThumbSize / 2f)) {
            return CLICK_ON_END;
        } else if (e.getY() >= top && e.getY() <= bottom
            && ((e.getX() >= getPaddingStartInner() && e.getX() < (mOffsetStart - mThumbSize / 2f))
            || ((e.getX() > (mOffsetStart + mThumbSize / 2f)) && e.getX() <= (mOffsetEnd + mOffsetStart) / 2))) {
            return CLICK_IN_START_AREA;
        } else if (e.getY() >= top && e.getY() <= bottom &&
            (((e.getX() > (mOffsetEnd + mOffsetStart) / 2) && e.getX() < (mOffsetEnd - mThumbSize / 2f))
                || (e.getX() > (mOffsetEnd + mThumbSize / 2f) && e.getX() <= getWidth() - getPaddingEndInner()))) {
            return CLICK_IN_END_AREA;
        } else if (!(e.getX() >= 0 && e.getX() <= getWidth() - getPaddingEndInner() && e.getY() >= top && e.getY() <= bottom)) {
            return CLICK_OUT_AREA;
        } else {
            return CLICK_INVALID;
        }
    }
    
    //更新滑块
    private void refresh() {
        invalidate();
    }
    
    private void updateOffsetStart(double offsetLeft) {
        mOffsetStart = offsetLeft;
        mProgressStart = formatInt((mOffsetStart - mThumbSize / 2f - getPaddingStartInner()) * max / mDistance);
        if (mBarChangeListener != null) {
            mBarChangeListener.onProgressChanged(this, mProgressStart, mProgressEnd, true);
        }
    }
    
    private void updateOffsetEnd(double offsetRight) {
        mOffsetEnd = offsetRight;
        mProgressEnd = formatInt((mOffsetEnd - mThumbSize / 2f - getPaddingStartInner()) * max / mDistance);
        if (mBarChangeListener != null) {
            mBarChangeListener.onProgressChanged(this, mProgressStart, mProgressEnd, true);
        }
    }
    
    // 设置滑动结果为整数
    private int formatInt(double value) {
        BigDecimal bd = new BigDecimal(value);
        BigDecimal bd1 = bd.setScale(0, BigDecimal.ROUND_HALF_UP);
        return bd1.intValue();
    }
    
    /**
     * 设置最大值
     *
     * @param max >0
     */
    public void setMax(int max) {
        if (max <= 0) {
            return;
        }
        
        this.max = max;
        invalidate();
    }
    
    /**
     * 设置左滑块进度
     */
    public void setProgressStart(int progressStart) {
        if (progressStart < 0) {
            progressStart = 0;
        } else if (progressStart > 100) {
            progressStart = 100;
        }
        
        if (progressStart == mProgressStart) {
            return;
        }
        
        mProgressStart = progressStart;
        mOffsetStart = mProgressStart * 1f / max * mDistance + mThumbSize / 2f;
        
        if (mProgressEnd < mProgressStart) {
            mProgressEnd = mProgressStart;
            mOffsetEnd = mOffsetStart;
        }
        
        refresh();
        if (mBarChangeListener != null) {
            mBarChangeListener.onProgressChanged(this, mProgressStart, mProgressEnd, false);
        }
        
        if (mBarMultiChangeListener != null) {
            mBarMultiChangeListener.onProgressChanged(this, mProgressStart, mProgressEnd, false);
        }
    }
    
    /**
     * 设置右滑块进度
     */
    public void setProgressEnd(int progressEnd) {
        if (progressEnd < 0) {
            progressEnd = 0;
        } else if (progressEnd > 100) {
            progressEnd = 100;
        }
        
        if (mProgressEnd == progressEnd) {
            return;
        }
        
        mProgressEnd = progressEnd;
        mOffsetEnd = mProgressEnd * 1f / max * mDistance + mThumbSize / 2f;
        
        if (mProgressStart > mProgressEnd) {
            mProgressStart = mProgressEnd;
            mOffsetStart = mOffsetEnd;
        }
        
        refresh();
        if (mBarChangeListener != null) {
            mBarChangeListener.onProgressChanged(this, mProgressStart, mProgressEnd, false);
        }
        
        if (mBarMultiChangeListener != null) {
            mBarMultiChangeListener.onProgressChanged(this, mProgressStart, mProgressEnd, false);
        }
    }
    
    /**
     * 设置滑动条进度改变监听
     */
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener mListener) {
        this.mBarChangeListener = mListener;
    }
    
    /**
     * 设置滑动条多项内容改变监听
     */
    public void setOnSeekBarMultiChangeListener(OnSeekBarMultiChangeListener mListener) {
        this.mBarMultiChangeListener = mListener;
    }
    
    /**
     * 设置左滑块左边滑动条
     */
    public void setStartScrollBar(@NonNull Drawable startScrollBar) {
        this.mStartScrollBar = startScrollBar;
        invalidate();
    }
    
    /**
     * 设置左滑块与右滑块中间滑动条
     */
    public void setCenterScrollBar(@NonNull Drawable centerScrollBar) {
        this.mCenterScrollBar = centerScrollBar;
        invalidate();
    }
    
    /**
     * 设置右滑块右边滑动条
     */
    public void setEndScrollBar(@NonNull Drawable endScrollBar) {
        this.mEndScrollBar = endScrollBar;
        invalidate();
    }
    
    /**
     * 设置左滑块样式
     */
    public void setThumbStart(@NonNull Drawable thumbStart) {
        mThumbStart = thumbStart;
        if (!mUserSetThumbSize) {
            mThumbSize = Math.max(mThumbStart.getIntrinsicHeight(), mThumbEnd.getIntrinsicHeight());
        }
        invalidate();
    }
    
    /**
     * 设置右滑块样式
     */
    public void setThumbEnd(@NonNull Drawable thumbEnd) {
        mThumbEnd = thumbEnd;
        if (!mUserSetThumbSize) {
            mThumbSize = Math.max(mThumbStart.getIntrinsicHeight(), mThumbEnd.getIntrinsicHeight());
        }
        
        invalidate();
    }
    
    /**
     * 设置滑块大小
     */
    public void setThumbSize(int size) {
        mThumbSize = size;
        invalidate();
    }
    
    /**
     * 设置左滑块背景
     */
    public void setThumbStartBg(Drawable thumbStartBg) {
        mThumbStartBg = thumbStartBg;
        invalidate();
    }
    
    /**
     * 设置右滑块背景
     */
    public void setThumbEndBg(Drawable thumbEndBg) {
        mThumbEndBg = thumbEndBg;
        invalidate();
    }
    
    /**
     * 设置滑块背景相比于滑块大小缩放比例
     *
     * @param thumbBgScale <= 0 则不绘制背景
     */
    public void setThumbBgScale(float thumbBgScale) {
        mThumbBgScale = thumbBgScale;
        invalidate();
    }
    
    /**
     * 设置滑动条高度
     */
    public void setScrollBarHeight(int scrollBarHeight) {
        mScrollBarHeight = scrollBarHeight;
        invalidate();
    }
    
    /**
     * 设置是否在滑块下面/上面展示滑动条当前滑动数值
     */
    public void setShowProgressText(boolean showProgressText) {
        mShowProgressText = showProgressText;
        invalidate();
    }
    
    /**
     * 设置文本距离滑动条位置的高度偏移量,如果绘制在顶部，在向上偏移，否则向下偏移
     */
    public void setProgressTextMargin(int progressTextMargin) {
        mProgressTextMargin = progressTextMargin;
        invalidate();
    }
    
    /**
     * 设置进度文本字体大小
     */
    public void setTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
        invalidate();
    }
    
    /**
     * 设置进度文本字体颜色
     */
    public void setTextColor(@ColorInt int color) {
        mTextPaint.setColor(color);
        invalidate();
    }
    
    /**
     * 设置进度文本绘制绘制
     *
     * @param progressTextGravity {@link #PROGRESS_TEXT_GRAVITY_TOP}、{@link #PROGRESS_TEXT_GRAVITY_BOTTOM}
     */
    public void setProgressTextGravity(@ProgressTextGravity int progressTextGravity) {
        mProgressTextGravity = progressTextGravity;
        invalidate();
    }
    
    /**
     * 设置进度文本格式化类
     */
    public void setProgressTextValueFormat(ProgressTextValueFormat progressTextValueFormat) {
        if (progressTextValueFormat == null) {
            mProgressTextValueFormat = DEFAULT_VALUE_FORMAT;
        } else {
            mProgressTextValueFormat = progressTextValueFormat;
        }
        
        invalidate();
    }
    
    /**
     * 滑动条进度改变监听
     */
    public interface OnSeekBarChangeListener {
        /**
         * @param progressStart 左滑块进度
         * @param progressEnd   右滑块进度
         * @param fromUser      是否是用户触摸滑动改变进度值
         */
        void onProgressChanged(TwoWaySeekBar seekBar, int progressStart, int progressEnd, boolean fromUser);
    }
    
    /**
     * 滑动条多项改变监听
     */
    public interface OnSeekBarMultiChangeListener {
        /**
         * @param progressStart 左滑块进度
         * @param progressEnd   右滑块进度
         * @param fromUser      是否是用户触摸滑动改变进度值
         */
        void onProgressChanged(TwoWaySeekBar seekBar, int progressStart, int progressEnd, boolean fromUser);
        
        /**
         * 通知用户已开始触摸
         *
         * @param isStartThumb {@code true}:触摸起始滑动块 {@code false}:触摸结束滑动块
         */
        void onStartTrackingTouch(TwoWaySeekBar seekBar, boolean isStartThumb);
        
        /**
         * 通知用户已结束触摸
         */
        void onStopTrackingTouch(TwoWaySeekBar seekBar);
    }
}