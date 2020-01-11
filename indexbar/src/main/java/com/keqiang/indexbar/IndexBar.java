package com.keqiang.indexbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
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
    private int mMaxHeight;
    private String[] letters;
    private float mItemHeight = -1;
    private Paint mPaint;
    private float mLetterSpacing = 1.5f;
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
        init(context, null);
    }
    
    public IndexBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public IndexBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public IndexBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        mReDrawLetterBitmap = true;
        mHandler = new Handler();
        mMaxHeight = 60;
        int textSize = 36;
        int textColor = getResources().getColor(R.color.colorAccent);
        if (attrs != null) {
            TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.IndexBar);
            textSize = t.getDimensionPixelSize(R.styleable.IndexBar_ib_text_size, 36);
            textColor = t.getColor(R.styleable.IndexBar_ib_text_color, textColor);
            String letters = t.getString(R.styleable.IndexBar_ib_letters);
            mLetterSpacing = t.getFloat(R.styleable.IndexBar_ib_letter_spacing, 1.5f);
            mMaxHeight = t.getDimensionPixelSize(R.styleable.IndexBar_ib_letter_max_height, 60);
            t.recycle();
            
            if (!TextUtils.isEmpty(letters)) {
                this.letters = new String[letters.length()];
                for (int i = 0; i < letters.length(); i++) {
                    this.letters[i] = String.valueOf(letters.charAt(i));
                }
            }
        }
        
        if (!isInEditMode()) {
            mMaxHeight = SimpleUtil.getScaledValue(mMaxHeight);
            textSize = SimpleUtil.getScaledValue(textSize);
            
            mTvToast = new TextView(context);
            mTvToast.setTextSize(TypedValue.COMPLEX_UNIT_PX, SimpleUtil.getScaledValue(50));
            mTvToast.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
            mTvToast.setBackgroundResource(R.drawable.sort_lv_bg);
            mTvToast.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            mTvToast.setGravity(Gravity.CENTER);
            
            mToastPop = new PopupWindow(mTvToast, SimpleUtil.getScaledValue(160),
                SimpleUtil.getScaledValue(120));
            mToastPop.setFocusable(false);
        }
        
        mPaint = new Paint();
        mPaint.setColor(textColor);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(textSize);
    }
    
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (letters == null || letters.length == 0) {
            return;
        }
        
        int drawW = getWidth() - getPaddingLeft() - getPaddingRight();
        int drawH = (int) (mItemHeight * letters.length + 0.5f);
        
        if (mReDrawLetterBitmap) {
            mReDrawLetterBitmap = false;
            
            Canvas mCanvas = new Canvas();
            mLetterBitmap = Bitmap.createBitmap(drawW, drawH, Bitmap.Config.ARGB_8888);
            if (mLetterBitmap == null) {
                return;
            }
            
            mCanvas.setBitmap(mLetterBitmap);
            float widthCenter = drawW / 2.0f;
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            float measuredHeight = fontMetrics.descent - fontMetrics.ascent;
            int startY = (int) (measuredHeight + (mItemHeight - measuredHeight) / 2);
            for (int i = 0; i < letters.length; i++) {
                mCanvas.drawText(letters[i], widthCenter - mPaint.measureText(letters[i]) / 2, startY + mItemHeight * (i), mPaint);
            }
        }
        
        canvas.save();
        canvas.translate((getWidth() - drawW) / 2 + getPaddingStart() - getPaddingEnd(),
            (getHeight() - drawH) / 2 + getPaddingTop() - getPaddingBottom());
        canvas.drawBitmap(mLetterBitmap, 0, 0, mPaint);
        canvas.restore();
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (letterTouchListener == null || letters == null || letters.length == 0 || mLetterBitmap == null) {
            return super.onTouchEvent(event);
        }
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                getParent().requestDisallowInterceptTouchEvent(true);
                // 按下的坐标除去偏移值 / 每个字符所占高度，计算字符所在位置
                int position = (int) ((event.getY() - getPaddingTop() + getPaddingBottom() - (getHeight() - mLetterBitmap.getHeight()) / 2) / mItemHeight);
                if (position >= 0 && position < letters.length) {
                    if (mShowToast) {
                        showToast(letters[position]);
                    }
                    
                    letterTouchListener.onLetterTouch(letters[position], position);
                }
            }
            return true;
            
            case MotionEvent.ACTION_MOVE: {
                // 按下的坐标除去偏移值 / 每个字符所占高度，计算字符所在位置
                int position = (int) ((event.getY() - getPaddingTop() + getPaddingBottom() - (getHeight() - mLetterBitmap.getHeight()) / 2) / mItemHeight);
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
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            String oneLetter = null;
            if (letters != null && letters.length > 0) {
                for (String letter : letters) {
                    if (TextUtils.isEmpty(letter)) {
                        continue;
                    }
                    
                    if (oneLetter == null || oneLetter.length() < letter.length()) {
                        oneLetter = letter;
                    }
                }
            }
            
            if (TextUtils.isEmpty(oneLetter)) {
                if (widthMode == MeasureSpec.UNSPECIFIED) {
                    widthSize = getPaddingStart() + getPaddingEnd();
                } else {
                    widthSize = Math.min(widthSize, (int) (getPaddingStart() + getPaddingEnd() + 0.5f));
                }
            } else {
                float measureText = mPaint.measureText(oneLetter);
                if (widthMode == MeasureSpec.UNSPECIFIED) {
                    widthSize = (int) (measureText + getPaddingStart() + getPaddingEnd() + 0.5f);
                } else {
                    widthSize = Math.min(widthSize, (int) (measureText + getPaddingStart() + getPaddingEnd() + 0.5f));
                }
            }
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        }
        
        
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float measuredHeight = fontMetrics.descent - fontMetrics.ascent;
        mItemHeight = Math.min(measuredHeight * mLetterSpacing, mMaxHeight);
        if (heightMode == MeasureSpec.EXACTLY) {
            // xml中指定大小或MATCH_PARENT
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        
        // 未指定大小，需要自己测量高度
        if (letters == null || letters.length == 0) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(getPaddingTop() + getPaddingBottom(), MeasureSpec.EXACTLY));
        } else {
            measuredHeight = mItemHeight * letters.length + getPaddingTop() + getPaddingBottom() + 0.5f;
            if (heightMode == MeasureSpec.UNSPECIFIED || heightSize >= measuredHeight) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((int) measuredHeight, MeasureSpec.EXACTLY));
            } else {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
            }
        }
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
        mPaint.setTextSize(textSize);
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
