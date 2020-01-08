package com.keqiang.indexbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import me.zhouzhuo810.magpiex.utils.SimpleUtil;


/**
 * 快速索引工具,设置paddingTop或paddingBottom可使整体内容向上或向下偏移
 *
 * Created by zz on 2016/5/12.
 */
public class IndexBar extends View {
    // 26个字母
    public static final String[] LETTERS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
        "W", "X", "Y", "Z", "#"};
    
    /**
     * 每个索引值绘制的最大高度
     */
    private int mMaxHeight = SimpleUtil.getScaledValue(60);
    private String[] letters;
    private float mItemHeight = -1;
    private Paint mPaint;
    private float mTextSize = -1;
    private OnLetterTouchListener letterTouchListener;
    
    private Bitmap mLetterBitmap;
    private boolean mReDrawLetterBitmap;
    private TextView mTvToast;
    private boolean mShowToast = true;
    private PopupWindow mToastPop;
    private Handler mHandler;
    private long mToastHideDelayTime = 500;
    
    public IndexBar(Context context) {
        super(context);
        
        init(context);
    }
    
    public IndexBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context);
    }
    
    public IndexBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        init(context);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public IndexBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    
    private void init(Context context) {
        letters = LETTERS;
        mReDrawLetterBitmap = true;
        mHandler = new Handler();
        
        mTvToast = new TextView(context);
        mTvToast.setTextSize(TypedValue.COMPLEX_UNIT_PX, SimpleUtil.getScaledValue(50));
        mTvToast.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        mTvToast.setBackgroundResource(R.drawable.sort_lv_bg);
        mTvToast.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mTvToast.setGravity(Gravity.CENTER);
        
        mToastPop = new PopupWindow(mTvToast, SimpleUtil.getScaledValue(160),
            SimpleUtil.getScaledValue(120));
        mToastPop.setFocusable(false);
        
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.colorAccent));
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }
    
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (letters == null || letters.length == 0) {
            return;
        }
        
        if (mReDrawLetterBitmap) {
            mReDrawLetterBitmap = false;
            
            Canvas mCanvas = new Canvas();
            int mW = getMeasuredWidth();
            int mH = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
            mLetterBitmap = Bitmap.createBitmap(mW, mH, Bitmap.Config.ARGB_8888);
            if (mLetterBitmap == null) {
                return;
            }
            
            mCanvas.setBitmap(mLetterBitmap);
            float widthCenter = mW / 2.0f;
            if (mTextSize > -1) {
                mPaint.setTextSize(mTextSize);
            } else {
                mPaint.setTextSize(mItemHeight - 4);
            }
            for (int i = 0; i < letters.length; i++) {
                mCanvas.drawText(letters[i], widthCenter - mPaint.measureText(letters[i]) / 2, mItemHeight * i + mItemHeight, mPaint);
            }
        }
        
        if (getPaddingBottom() > 0) {
            canvas.save();
            canvas.translate(0, getPaddingBottom());
            canvas.drawBitmap(mLetterBitmap, 0, 0, mPaint);
            canvas.restore();
        } else {
            canvas.drawBitmap(mLetterBitmap, 0, 0, mPaint);
        }
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (letterTouchListener == null || letters == null || letters.length == 0) {
            return super.onTouchEvent(event);
        }
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                getParent().requestDisallowInterceptTouchEvent(true);
                int position = (int) (event.getY() / mItemHeight);
                if (position >= 0 && position < letters.length) {
                    if (mShowToast) {
                        showToast(letters[position]);
                    }
                    
                    letterTouchListener.onLetterTouch(letters[position], position);
                }
            }
            return true;
            
            case MotionEvent.ACTION_MOVE: {
                int position = (int) (event.getY() / mItemHeight);
                if (position >= 0 && position < letters.length) {
                    if (mShowToast) {
                        showToast(letters[position]);
                    }
                    
                    letterTouchListener.onLetterTouch(letters[position], position);
                }
            }
            
            return true;
            
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                hideToast();
                getParent().requestDisallowInterceptTouchEvent(false);
                letterTouchListener.onActionUp();
                return true;
        }
        
        return super.onTouchEvent(event);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mReDrawLetterBitmap = true;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        if (letters == null || letters.length == 0 || height <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        
        mItemHeight = height * 1f / letters.length;
        mItemHeight = Math.min(mItemHeight, mMaxHeight);
        
        height = (int) (mItemHeight * letters.length + getPaddingTop() + getPaddingBottom() + 0.5f);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mToastPop != null) {
            mToastPop.dismiss();
            mToastPop = null;
        }
        mTvToast = null;
        mLetterBitmap = null;
        mHandler.removeCallbacksAndMessages(null);
    }
    
    public void showToast(String text) {
        if (!mToastPop.isShowing()) {
            mToastPop.showAtLocation(this, Gravity.CENTER, 0, 0);
        }
        mTvToast.setText(text);
    }
    
    public void hideToast() {
        if (mToastPop.isShowing()) {
            mHandler.postDelayed(() -> mToastPop.dismiss(), mToastHideDelayTime);
        }
    }
    
    /**
     * 设置显示的快速索引值
     */
    public void setLetters(String[] letters) {
        this.letters = letters;
        requestLayout();
    }
    
    
    public void setLetterTouchListener(OnLetterTouchListener letterTouchListener) {
        this.letterTouchListener = letterTouchListener;
    }
    
    public void setTextSize(float textSize) {
        this.mTextSize = textSize;
        invalidate();
    }
    
    public void setTextColor(@ColorInt int color) {
        mPaint.setColor(color);
        invalidate();
    }
    
    /**
     * 设置点击索引后是否在屏幕中间显示索引内容
     *
     * @param showToast 默认为true
     */
    public void setShowToast(boolean showToast) {
        mShowToast = showToast;
    }
    
    /**
     * 设置点击索引后在屏幕中间显示索引内容的文字大小
     *
     * @param size 单位px,默认50px
     */
    public void setToastTextSize(int size) {
        mTvToast.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }
    
    /**
     * 设置点击索引后在屏幕中间显示索引内容的文字颜色，默认白色
     */
    public void setToastTextColorRes(@ColorRes int colorRes) {
        mTvToast.setTextColor(ContextCompat.getColor(getContext(), colorRes));
    }
    
    /**
     * 设置点击索引后在屏幕中间显示索引内容的文字颜色，默认白色
     */
    public void setToastTextColor(@ColorInt int color) {
        mTvToast.setTextColor(color);
    }
    
    /**
     * 设置点击索引后在屏幕中间显示索引内容的背景，默认{@link R.drawable#sort_lv_bg}
     */
    public void setToastBg(Drawable bg) {
        mTvToast.setBackground(bg);
    }
    
    /**
     * 设置点击索引后在屏幕中间显示索引内容的背景颜色,默认{@link R.drawable#sort_lv_bg}
     */
    public void setToastBg(@ColorInt int color) {
        mTvToast.setBackgroundColor(color);
    }
    
    /**
     * 设置点击索引后在屏幕中间显示索引内容所依附父布局背景颜色，默认为白色
     */
    public void setToastParentBgRes(@ColorRes int colorRes) {
        mTvToast.setBackgroundColor(ContextCompat.getColor(getContext(), colorRes));
    }
    
    /**
     * 设置点击索引后在屏幕中间显示索引内容所依附父布局背景颜色，默认为白色
     */
    public void setToastParentBg(@ColorInt int color) {
        mTvToast.setBackgroundColor(color);
    }
    
    /**
     * 设置每个索引值在界面绘制的最大高度，索引值默认平分当前View的高度
     */
    public void setLetterMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
        invalidate();
    }
    
    public void setToastHideDelayTime(long delayTime) {
        mToastHideDelayTime = delayTime;
    }
    
    public interface OnLetterTouchListener {
        void onLetterTouch(String letter, int position);
        
        void onActionUp();
    }
}
