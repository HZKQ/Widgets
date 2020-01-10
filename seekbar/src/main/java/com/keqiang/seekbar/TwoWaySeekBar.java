package com.keqiang.seekbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;

import java.math.BigDecimal;

import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * 双向滑动控件
 */
public class TwoWaySeekBar extends View {
    private static final int CLICK_ON_LOW = 1;        //手指在前滑块上滑动
    private static final int CLICK_ON_HIGH = 2;       //手指在后滑块上滑动
    private static final int CLICK_IN_LOW_AREA = 3;   //手指点击离前滑块近
    private static final int CLICK_IN_HIGH_AREA = 4;  //手指点击离后滑块近
    private static final int CLICK_OUT_AREA = 5;      //手指点击在view外
    private static final int CLICK_INVALID = 0;
    private static final int[] STATE_NORMAL = {};
    private static final int[] STATE_PRESSED = {
        android.R.attr.state_pressed, android.R.attr.state_window_focused,
    };
    
    /**
     * 滑动块顶部离view顶部的距离
     */
    private int mThumbMarginTop = 0;
    
    /**
     * 当前滑块文字距离view顶部距离
     */
    private int mTextViewMarginTop = SimpleUtil.getScaledValue(30);
    
    /**
     * 左滑块到View左边的滑动条背景
     */
    private Drawable leftScrollBarBg;
    
    /**
     * 左滑块到右滑块中间的滑动条背景
     */
    private Drawable centerScrollBarBg;
    
    /**
     * 右滑块到View右边的滑动条背景
     */
    private Drawable rightScrollBarBg;
    
    /**
     * 左滑块
     */
    private Drawable mThumbLeft;
    
    /**
     * 右滑块
     */
    private Drawable mThumbRight;
    
    /**
     * 滑动条宽度，默认取控件宽度
     */
    private int mScrollBarWidth;
    
    /**
     * 滑动条高度，默认取{@link #leftScrollBarBg}高度
     */
    private int mScrollBarHeight = SimpleUtil.getScaledValue(20);
    
    /**
     * 滑动块直径R
     */
    private int mThumbWidth;
    
    /**
     * 左滑块中心坐标
     */
    private double mOffsetLeft = 0;
    
    /**
     * 右滑块中心坐标
     */
    private double mOffsetRight = 0;
    
    /**
     * 总刻度是固定距离 两边各去掉半个滑块距离
     */
    private int mDistance;
    
    /**
     * 手指按下的类型
     */
    private int mFlag = CLICK_INVALID;
    
    /**
     * 默认左滑块位置百分比
     */
    private double defaultScreenLeft = 0;
    
    /**
     * 默认右滑块位置百分比
     */
    private double defaultScreenRight = 100;
    
    /**
     * 滑动监听
     */
    private OnSeekBarChangeListener mBarChangeListener;
    
    /**
     * 用于绘制滑块位置值
     */
    private Paint mTextPaint;
    
    public TwoWaySeekBar(Context context) {
        this(context, null);
    }
    
    public TwoWaySeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public TwoWaySeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
    private void init(Context context) {
        // leftScrollBarBg = ContextCompat.getDrawable(context, R.drawable.bg_round_rectangle_260dp_yellow);
        // centerScrollBarBg = ContextCompat.getDrawable(context, R.drawable.bg_round_rectangle_260dp_blue);
        // rightScrollBarBg = ContextCompat.getDrawable(context, R.drawable.bg_round_rectangle_260dp_green);
        // mThumbLeft = ContextCompat.getDrawable(context, R.drawable.huadong);
        // mThumbRight = ContextCompat.getDrawable(context, R.drawable.huadong);
        // mThumbLeft.setState(STATE_NORMAL);
        // mThumbRight.setState(STATE_NORMAL);
        // mThumbWidth = mThumbLeft.getIntrinsicWidth();
        // mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // mTextPaint.setTextAlign(Paint.Align.CENTER);
        // mTextPaint.setColor(ContextCompat.getColor(context, R.color.colorTagText));
        // mTextPaint.setTextSize(AutoUtils.getPercentWidthSize(32));
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取view的总宽度
        int width = MeasureSpec.getSize(widthMeasureSpec);
        mScrollBarWidth = width;
        mOffsetLeft = mThumbWidth / 2f;
        mOffsetRight = width - mThumbWidth / 2f;
        mDistance = width - mThumbWidth;
        if (defaultScreenLeft != 0) {
            mOffsetLeft = formatInt(defaultScreenLeft / 100 * (mDistance)) + mThumbWidth / 2f;
        }
        if (defaultScreenRight != 100) {
            mOffsetRight = formatInt(defaultScreenRight / 100 * (mDistance)) + mThumbWidth / 2f;
        }
        setMeasuredDimension(width, -mThumbWidth - mThumbMarginTop - 2);
    }
    
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }
    
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //当前滑动坐标值
        int top = mThumbMarginTop + mThumbWidth / 2 - mScrollBarHeight / 2;
        int bottom = top + mScrollBarHeight;
        
        // 左边滑块部分
        leftScrollBarBg.setBounds(mThumbWidth / 2, top, (int) mOffsetLeft, bottom);
        leftScrollBarBg.draw(canvas);
        // 两个滑块中间部分
        centerScrollBarBg.setBounds((int) mOffsetLeft, top, (int) mOffsetRight, bottom);
        centerScrollBarBg.draw(canvas);
        // 右边滑块部分
        rightScrollBarBg.setBounds((int) mOffsetRight, top, mScrollBarWidth - mThumbWidth / 2, bottom);
        rightScrollBarBg.draw(canvas);
        // 前滑块
        mThumbLeft.setBounds((int) (mOffsetLeft - mThumbWidth / 2), mThumbMarginTop, (int) (mOffsetLeft + mThumbWidth / 2), mThumbWidth + mThumbMarginTop);
        mThumbLeft.draw(canvas);
        // 后滑块
        mThumbRight.setBounds((int) (mOffsetRight - mThumbWidth / 2), mThumbMarginTop, (int) (mOffsetRight + mThumbWidth / 2), mThumbWidth + mThumbMarginTop);
        mThumbRight.draw(canvas);
        // 当前滑块刻度
        int progressLow = formatInt((mOffsetLeft - mThumbWidth / 2f) * 100 / mDistance);
        int progressHigh = formatInt((mOffsetRight - mThumbWidth / 2f) * 100 / mDistance);
        canvas.drawText(progressLow + "%", (int) mOffsetLeft - 2 - 2, mTextViewMarginTop + mThumbWidth, mTextPaint);
        canvas.drawText(progressHigh + "%", (int) mOffsetRight - 2, mTextViewMarginTop + mThumbWidth, mTextPaint);
        if (mBarChangeListener != null) {
            mBarChangeListener.onProgressChanged(this, progressLow, progressHigh);
        }
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
    
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //按下
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            mFlag = getAreaFlag(e);
            if (mFlag == CLICK_ON_LOW) {
                mThumbLeft.setState(STATE_PRESSED);
            } else if (mFlag == CLICK_ON_HIGH) {
                mThumbRight.setState(STATE_PRESSED);
            } else if (mFlag == CLICK_IN_LOW_AREA) {
                mThumbLeft.setState(STATE_PRESSED);
                mThumbRight.setState(STATE_NORMAL);
                //如果点击0-mThumbWidth/2坐标
                if (e.getX() < 0 || e.getX() <= mThumbWidth / 2) {
                    updateOffsetLow(mThumbWidth / 2f);
                } else if (e.getX() > mScrollBarWidth - mThumbWidth / 2) {
                    updateOffsetLow(mThumbWidth / 2f + mDistance / 2f);
                } else {
                    updateOffsetLow(formatInt(e.getX()));
                }
            } else if (mFlag == CLICK_IN_HIGH_AREA) {
                mThumbRight.setState(STATE_PRESSED);
                mThumbLeft.setState(STATE_NORMAL);
                if (e.getX() >= mScrollBarWidth - mThumbWidth / 2) {
                    updateOffsetHigh(mDistance + mThumbWidth / 2f);
                } else {
                    updateOffsetHigh(formatInt(e.getX()));
                }
            }
            
            //更新滑块
            refresh();
        } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
            //移动move
            if (mFlag == CLICK_ON_LOW) {
                if (e.getX() < 0 || e.getX() <= mThumbWidth / 2) {
                    updateOffsetLow(mThumbWidth / 2f);
                } else if (e.getX() >= mScrollBarWidth - mThumbWidth / 2) {
                    updateOffsetLow(mThumbWidth / 2f + mDistance);
                    updateOffsetHigh(mOffsetLeft);
                } else {
                    updateOffsetLow(formatInt(e.getX()));
                    if (mOffsetRight - mOffsetLeft <= 0) {
                        updateOffsetHigh((mOffsetLeft <= mDistance + mThumbWidth / 2f) ? (mOffsetLeft) : (mDistance + mThumbWidth / 2f));
                    }
                }
            } else if (mFlag == CLICK_ON_HIGH) {
                if (e.getX() < mThumbWidth / 2) {
                    updateOffsetLow(mThumbWidth / 2f);
                    updateOffsetHigh(mThumbWidth / 2f);
                } else if (e.getX() > mScrollBarWidth - mThumbWidth / 2) {
                    updateOffsetHigh(mThumbWidth / 2f + mDistance);
                } else {
                    updateOffsetHigh(formatInt(e.getX()));
                    if (mOffsetRight - mOffsetLeft <= 0) {
                        updateOffsetLow((mOffsetRight >= mThumbWidth / 2f) ? (mOffsetRight) : mThumbWidth / 2f);
                    }
                }
            }
            //更新滑块
            refresh();
        } else if (e.getAction() == MotionEvent.ACTION_UP) {
            //抬起
            mThumbLeft.setState(STATE_NORMAL);
            mThumbRight.setState(STATE_NORMAL);
        }
        return true;
    }
    
    //获取当前手指位置
    public int getAreaFlag(MotionEvent e) {
        int top = mThumbMarginTop;
        int bottom = mThumbWidth + mThumbMarginTop;
        if (e.getY() >= top && e.getY() <= bottom && e.getX() >= (mOffsetLeft - mThumbWidth / 2) && e.getX() <= mOffsetLeft + mThumbWidth / 2) {
            return CLICK_ON_LOW;
        } else if (e.getY() >= top && e.getY() <= bottom && e.getX() >= (mOffsetRight - mThumbWidth / 2) && e.getX() <= (mOffsetRight + mThumbWidth / 2)) {
            return CLICK_ON_HIGH;
        } else if (e.getY() >= top
            && e.getY() <= bottom
            && ((e.getX() >= 0 && e.getX() < (mOffsetLeft - mThumbWidth / 2)) || ((e.getX() > (mOffsetLeft + mThumbWidth / 2))
            && e.getX() <= (mOffsetRight + mOffsetLeft) / 2))) {
            return CLICK_IN_LOW_AREA;
        } else if (e.getY() >= top && e.getY() <= bottom && (((e.getX() > (mOffsetRight + mOffsetLeft) / 2) && e.getX() < (mOffsetRight - mThumbWidth / 2)) || (e
            .getX() > (mOffsetRight + mThumbWidth / 2) && e.getX() <= mScrollBarWidth))) {
            return CLICK_IN_HIGH_AREA;
        } else if (!(e.getX() >= 0 && e.getX() <= mScrollBarWidth && e.getY() >= top && e.getY() <= bottom)) {
            return CLICK_OUT_AREA;
        } else {
            return CLICK_INVALID;
        }
    }
    
    //更新滑块
    private void refresh() {
        invalidate();
    }
    
    //设置前滑块的值
    public void setProgressLow(int progressLow) {
        this.defaultScreenLeft = progressLow;
        mOffsetLeft = formatInt(progressLow / 100f * (mDistance)) + mThumbWidth / 2f;
        refresh();
    }
    
    //设置后滑块的值
    public void setProgressHigh(int progressHigh) {
        this.defaultScreenRight = progressHigh;
        mOffsetRight = formatInt(progressHigh / 100f * (mDistance)) + mThumbWidth / 2f;
        refresh();
    }
    
    //设置前滑块的值
    private void updateOffsetLow(double offsetLow) {
        mOffsetLeft = offsetLow;
        this.defaultScreenLeft = formatInt((mOffsetLeft - mThumbWidth / 2f) * 100f / mDistance);
    }
    
    private void updateOffsetHigh(double offsetHigh) {
        mOffsetRight = offsetHigh;
        this.defaultScreenRight = formatInt((mOffsetRight - mThumbWidth / 2f) * 100f / mDistance);
    }
    
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener mListener) {
        this.mBarChangeListener = mListener;
    }
    
    //回调函数，在滑动时实时调用，改变输入框的值
    public interface OnSeekBarChangeListener {
        //滑动时
        void onProgressChanged(TwoWaySeekBar seekBar, int progressLow,
                               int progressHigh);
    }
    
    //设置滑动结果为整数
    private int formatInt(double value) {
        BigDecimal bd = new BigDecimal(value);
        BigDecimal bd1 = bd.setScale(0, BigDecimal.ROUND_HALF_UP);
        return bd1.intValue();
    }
    
    public void setLeftScrollBarBg(Drawable leftScrollBarBg) {
        this.leftScrollBarBg = leftScrollBarBg;
        invalidate();
    }
    
    public void setCenterScrollBarBg(Drawable centerScrollBarBg) {
        this.centerScrollBarBg = centerScrollBarBg;
        invalidate();
    }
    
    public void setRightScrollBarBg(Drawable rightScrollBarBg) {
        this.rightScrollBarBg = rightScrollBarBg;
        invalidate();
    }
    
    public void setThumbLeft(Drawable thumbLeft) {
        if (thumbLeft == null) {
            return;
        }
        
        mThumbLeft = thumbLeft;
        mThumbWidth = Math.max(mThumbLeft.getIntrinsicHeight(), mThumbRight.getIntrinsicHeight());
        invalidate();
    }
    
    public void setThumbRight(Drawable thumbRight) {
        if (thumbRight == null) {
            return;
        }
        
        mThumbRight = thumbRight;
        mThumbWidth = Math.max(mThumbLeft.getIntrinsicHeight(), mThumbRight.getIntrinsicHeight());
        invalidate();
    }
    
    public void setScrollBarHeight(int scrollBarHeight) {
        mScrollBarHeight = scrollBarHeight;
        invalidate();
    }
    
    /**
     * 设置文本距离滑动条位置的高度偏移量
     */
    public void setTextViewMarginTop(int textViewMarginTop) {
        mTextViewMarginTop = textViewMarginTop;
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
}