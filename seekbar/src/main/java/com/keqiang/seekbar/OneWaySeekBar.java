package com.keqiang.seekbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.math.BigDecimal;

import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * 单向滑动控件
 */
public class OneWaySeekBar extends View {
    public static final int PROGRESS_TEXT_GRAVITY_TOP = 0;
    public static final int PROGRESS_TEXT_GRAVITY_BOTTOM = 1;
    
    private static final int CLICK_ON_THUMB = 1;        // 手指在滑块上滑动
    private static final int CLICK_IN_THUMB_AREA = 3;   // 手指点击滑块附近
    private static final int CLICK_OUT_AREA = 5;        // 手指点击在view外
    private static final int CLICK_INVALID = 0;
    private static final int[] STATE_NORMAL = {};
    private static final int[] STATE_PRESSED = {
        android.R.attr.state_pressed, android.R.attr.state_window_focused,
    };
    
    private static final ProgressTextValueFormat DEFAULT_VALUE_FORMAT = value -> value + "%";
    
    /**
     * 滑块到View左边的滑动条背景
     */
    private Drawable mStartScrollBar;
    
    /**
     * 滑块到View右边的滑动条背景
     */
    private Drawable mEndScrollBar;
    
    /**
     * 左滑块
     */
    private Drawable mThumb;
    
    /**
     * 左滑块点击背景
     */
    private Drawable mThumbBg;
    
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
    private double mOffset = 0;
    
    /**
     * 总刻度是固定距离,两边各去掉半个滑块距离
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
     * 默认滑块位置百分比
     */
    private double mDefaultScreen = 0;
    
    /**
     * 滑动监听
     */
    private OnSeekBarChangeListener mBarChangeListener;
    
    /**
     * 用于绘制滑块位置值
     */
    private Paint mTextPaint;
    private boolean mShowProgressText = true;
    private int mProgressTextGravity = PROGRESS_TEXT_GRAVITY_BOTTOM;
    private int mProgressTextMargin;
    private ProgressTextValueFormat mProgressTextValueFormat;
    
    public OneWaySeekBar(Context context) {
        this(context, null);
    }
    
    public OneWaySeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public OneWaySeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    
    @SuppressWarnings("ConstantConditions")
    private void init(Context context, AttributeSet attrs) {
        int textColor;
        int textSize;
        mThumbBg = ContextCompat.getDrawable(context, R.drawable.bg_circle_gray_alpha);
        if (attrs != null) {
            TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.OneWaySeekBar);
            mStartScrollBar = t.getDrawable(R.styleable.OneWaySeekBar_os_start_bar);
            mEndScrollBar = t.getDrawable(R.styleable.OneWaySeekBar_os_end_bar);
            mThumb = t.getDrawable(R.styleable.OneWaySeekBar_os_thumb);
            mThumbSize = t.getDimensionPixelSize(R.styleable.OneWaySeekBar_os_thumb_size, -1);
            mScrollBarHeight = t.getDimensionPixelSize(R.styleable.OneWaySeekBar_os_bar_height, 20);
            mShowProgressText = t.getBoolean(R.styleable.OneWaySeekBar_os_show_progress_text, true);
            mProgressTextGravity = t.getInt(R.styleable.OneWaySeekBar_os_progress_text_gravity, PROGRESS_TEXT_GRAVITY_BOTTOM);
            textSize = t.getDimensionPixelSize(R.styleable.OneWaySeekBar_os_progress_text_size, 36);
            textColor = t.getColor(R.styleable.OneWaySeekBar_os_progress_text_color, 0xCC000000);
            mProgressTextMargin = t.getDimensionPixelSize(R.styleable.OneWaySeekBar_os_progress_text_margin, 10);
            max = t.getInteger(R.styleable.OneWaySeekBar_os_max, 100);
            if (t.hasValue(R.styleable.OneWaySeekBar_os_thumb_bg)) {
                mThumbBg = t.getDrawable(R.styleable.OneWaySeekBar_os_thumb_bg);
            }
            mThumbBgScale = t.getFloat(R.styleable.OneWaySeekBar_os_thumb_bg_scale, 1.1f);
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
        
        if (!isInEditMode()) {
            textSize = SimpleUtil.getScaledValue(textSize);
            mScrollBarHeight = SimpleUtil.getScaledValue(mScrollBarHeight);
            mProgressTextMargin = SimpleUtil.getScaledValue(mProgressTextMargin);
        }
        
        if (mStartScrollBar == null) {
            mStartScrollBar = ContextCompat.getDrawable(context, R.drawable.bg_round_rectangle_260dp_yellow);
        }
        
        if (mEndScrollBar == null) {
            mEndScrollBar = ContextCompat.getDrawable(context, R.drawable.bg_round_rectangle_260dp_green);
        }
        
        if (mThumb == null) {
            mThumb = ContextCompat.getDrawable(context, R.drawable.huadong);
        }
        
        if (mThumbSize == -1) {
            mThumbSize = mThumb.getIntrinsicWidth();
        }
        mThumb.setState(STATE_NORMAL);
        
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(textSize);
        mProgressTextValueFormat = DEFAULT_VALUE_FORMAT;
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
        
        mOffset = mThumbSize / 2f + getPaddingStartInner();
        mDistance = widthSize - getPaddingStartInner() - getPaddingEndInner() - mThumbSize;
        if (mDefaultScreen != 0) {
            mOffset = formatInt(mDefaultScreen / max * mDistance) + mThumbSize / 2f + getPaddingStartInner();
        }
        
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    /**
     * 在用户设置基础上为点击绘制滑块背景预览放大的宽度，否则滑动到两边时绘制不全
     */
    public int getPaddingStartInner() {
        if (mThumbBg == null || mThumbBgScale <= 0) {
            return getPaddingStart();
        }
        return (int) (getPaddingStart() + mThumbSize * (mThumbBgScale - 1) / 2);
    }
    
    /**
     * 在用户设置基础上为点击绘制滑块背景预览放大的宽度，否则滑动到两边时绘制不全
     */
    public int getPaddingEndInner() {
        if (mThumbBg == null || mThumbBgScale <= 0) {
            return getPaddingStart();
        }
        return (int) (getPaddingEnd() + mThumbSize * (mThumbBgScale - 1) / 2);
    }
    
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 滑动条高度绘制起始和结束
        canvas.save();
        canvas.clipRect(getPaddingStart(), getPaddingTop(), getWidth() - getPaddingEnd(), getHeight() - getPaddingBottom());
        
        int top = (getHeight() - mScrollBarHeight) / 2;
        int bottom = top + mScrollBarHeight;
        
        // 左边滑动条部分
        mStartScrollBar.setBounds(mThumbSize / 2 + getPaddingStartInner(), top, (int) mOffset, bottom);
        mStartScrollBar.draw(canvas);
        
        // 右边滑动条部分
        mEndScrollBar.setBounds((int) mOffset, top, getWidth() - getPaddingEndInner() - mThumbSize / 2, bottom);
        mEndScrollBar.draw(canvas);
        
        // 滑块高度绘制起始和结束
        top = (getHeight() - mThumbSize) / 2;
        bottom = top + mThumbSize;
        
        // 滑块
        int[] state = mThumb.getState();
        if (isInEditMode() || state == STATE_PRESSED) {
            drawThumbBg(canvas, mThumbBg, mOffset);
        }
        mThumb.setBounds((int) (mOffset - mThumbSize / 2), top, (int) (mOffset + mThumbSize / 2), bottom);
        mThumb.draw(canvas);
        
        // 当前滑块刻度
        int progress = formatInt((mOffset - mThumbSize / 2f - getPaddingStartInner()) * max / mDistance);
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
            
            String format = mProgressTextValueFormat.format(progress);
            if (!TextUtils.isEmpty(format)) {
                canvas.drawText(format, (int) mOffset - 2 - 2, y, mTextPaint);
            }
        }
        
        canvas.restore();
        
        if (mBarChangeListener != null) {
            mBarChangeListener.onProgressChanged(this, progress);
        }
    }
    
    /**
     * 绘制滑块背景
     */
    private void drawThumbBg(Canvas canvas, Drawable bg, double offset) {
        if (bg == null) {
            return;
        }
        
        int bgSize = (int) (mThumbSize * mThumbBgScale);
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
        // 按下
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            mFlag = getAreaFlag(e);
            if (mFlag == CLICK_ON_THUMB) {
                mThumb.setState(STATE_PRESSED);
                refresh();
            } else if (mFlag == CLICK_IN_THUMB_AREA) {
                mThumb.setState(STATE_PRESSED);
                // 如果点击0-mThumbSize/2坐标
                if (e.getX() < mThumbSize / 2f + getPaddingStartInner()) {
                    updateOffset(mThumbSize / 2f + getPaddingStartInner());
                } else if (e.getX() > getWidth() - getPaddingEndInner() - mThumbSize / 2f) {
                    updateOffset(mThumbSize / 2f + mDistance + getPaddingStartInner());
                } else {
                    updateOffset(formatInt(e.getX()));
                }
                refresh();
            }
        } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
            // 移动move
            if (mFlag == CLICK_ON_THUMB) {
                if (e.getX() <= mThumbSize / 2f + getPaddingStartInner()) {
                    updateOffset(mThumbSize / 2f + getPaddingStartInner());
                } else if (e.getX() >= getWidth() - getPaddingEndInner() - mThumbSize / 2f) {
                    updateOffset(mThumbSize / 2f + mDistance + getPaddingStartInner());
                } else {
                    updateOffset(formatInt(e.getX()));
                }
                refresh();
            }
            
        } else if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) {
            // 抬起
            mThumb.setState(STATE_NORMAL);
            refresh();
        }
        return true;
    }
    
    // 获取当前手指位置
    public int getAreaFlag(MotionEvent e) {
        int top = (getHeight() - mThumbSize) / 2;
        int bottom = mThumbSize + top;
        if (e.getY() >= top && e.getY() <= bottom && e.getX() >= (mOffset - mThumbSize / 2f) && e.getX() <= mOffset + mThumbSize / 2f) {
            return CLICK_ON_THUMB;
        } else if (e.getY() >= top
            && e.getY() <= bottom
            && ((e.getX() >= getPaddingStartInner() && e.getX() < (mOffset - mThumbSize / 2f))
            || ((e.getX() > (mOffset + mThumbSize / 2f) && e.getX() <= getWidth() - getPaddingEndInner())))) {
            return CLICK_IN_THUMB_AREA;
        } else if (!(e.getX() >= getPaddingStartInner() && e.getX() <= getWidth() - getPaddingEndInner() && e.getY() >= top && e.getY() <= bottom)) {
            return CLICK_OUT_AREA;
        } else {
            return CLICK_INVALID;
        }
    }
    
    //更新滑块
    private void refresh() {
        invalidate();
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
     * 设置左滑块的值
     */
    public void setProgress(int progress) {
        this.mDefaultScreen = progress;
        mOffset = formatInt(progress * 1f / max * mDistance) + mThumbSize / 2f;
        refresh();
    }
    
    private void updateOffset(double offsetLeft) {
        mOffset = offsetLeft;
        this.mDefaultScreen = formatInt((mOffset - mThumbSize / 2f - getPaddingStartInner()) * max / mDistance);
    }
    
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener mListener) {
        this.mBarChangeListener = mListener;
    }
    
    //设置滑动结果为整数
    private int formatInt(double value) {
        BigDecimal bd = new BigDecimal(value);
        BigDecimal bd1 = bd.setScale(0, BigDecimal.ROUND_HALF_UP);
        return bd1.intValue();
    }
    
    public void setStartScrollBar(@NonNull Drawable startScrollBar) {
        this.mStartScrollBar = startScrollBar;
        invalidate();
    }
    
    public void setEndScrollBar(@NonNull Drawable endScrollBar) {
        this.mEndScrollBar = endScrollBar;
        invalidate();
    }
    
    public void setThumb(@NonNull Drawable thumb) {
        mThumb = thumb;
        if (!mUserSetThumbSize) {
            mThumbSize = mThumb.getIntrinsicHeight();
        }
        invalidate();
    }
    
    public void setThumbSize(int size) {
        mThumbSize = size;
        invalidate();
    }
    
    public void setThumbBg(Drawable thumbBg) {
        mThumbBg = thumbBg;
        invalidate();
    }
    
    /**
     * @param thumbBgScale <=0则不绘制背景
     */
    public void setThumbBgScale(float thumbBgScale) {
        mThumbBgScale = thumbBgScale;
        invalidate();
    }
    
    public void setScrollBarHeight(int scrollBarHeight) {
        mScrollBarHeight = scrollBarHeight;
        invalidate();
    }
    
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
    
    public void setTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
        invalidate();
    }
    
    public void setTextColor(@ColorInt int color) {
        mTextPaint.setColor(color);
        invalidate();
    }
    
    /**
     * 设置进度文本绘制绘制
     *
     * @param progressTextGravity {@link #PROGRESS_TEXT_GRAVITY_TOP}、{@link #PROGRESS_TEXT_GRAVITY_BOTTOM}
     */
    public void setProgressTextGravity(int progressTextGravity) {
        mProgressTextGravity = progressTextGravity;
        invalidate();
    }
    
    public void setProgressTextValueFormat(ProgressTextValueFormat progressTextValueFormat) {
        if (progressTextValueFormat == null) {
            mProgressTextValueFormat = DEFAULT_VALUE_FORMAT;
        } else {
            mProgressTextValueFormat = progressTextValueFormat;
        }
        
        invalidate();
    }
    
    /**
     * 滑动监听
     */
    public interface OnSeekBarChangeListener {
        /**
         * @param progress 滑块的值
         */
        void onProgressChanged(OneWaySeekBar oneWaySeekBar, int progress);
    }
}