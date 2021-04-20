package com.keqiang.indexbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
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
 * @author Created by zz on 2016/5/12.
 */
public class IndexBar extends View {
    // 26个字母 + 10 数字 + #
    public static final String[] LETTERS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
        "W", "X", "Y", "Z", "#"};
    
    /**
     * 文字大小，如果所有索引绘制高度 <= 当前View高度使用此值
     */
    private float mTextSize;
    /**
     * 最新文字大小，如果所有索引绘制高度 > 当前View高度,则文字等比例缩放后不能小于此值
     */
    private float minTextSize;
    private String[] letters;
    private float mLetterSpacing = 1.5f;
    private OnIndexTouchListener mOnIndexTouchListener;
    private OnLetterChosenListener mOnLetterChosenListener;
    
    private Paint mPaint;
    private TextView mTvToast;
    private boolean mShowToast = true;
    private PopupWindow mToastPop;
    private Handler mHandler;
    private long mToastHideDelayTime = 500;
    
    /**
     * 索引字符总高度
     */
    private float mIndexTotalHeight;
    
    /**
     * 每个索引字符所占高度
     */
    private float mIndexHeight = -1;
    
    /**
     * 每一个索引字符的缩放比例
     */
    private float mSizeScale = 1;
    
    /**
     * 缩放比例是否需要计算
     */
    private boolean mScaleNeedCalculation = true;
    
    /**
     * 最终需要绘制的单个字符高度
     */
    private float mLetterHeight;
    
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
        letters = LETTERS;
        mHandler = new Handler();
        mTextSize = 26;
        minTextSize = 16;
        int textColor = getResources().getColor(R.color.colorAccent);
        if (attrs != null) {
            TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.IndexBar);
            mTextSize = t.getDimensionPixelSize(R.styleable.IndexBar_ib_text_size, 26);
            minTextSize = t.getDimensionPixelSize(R.styleable.IndexBar_ib_min_text_size, 16);
            textColor = t.getColor(R.styleable.IndexBar_ib_text_color, textColor);
            boolean existLettersAttr = t.hasValue(R.styleable.IndexBar_ib_letters);
            String letters = t.getString(R.styleable.IndexBar_ib_letters);
            mLetterSpacing = t.getFloat(R.styleable.IndexBar_ib_letter_spacing, 1.5f);
            t.recycle();
            
            if (!TextUtils.isEmpty(letters)) {
                this.letters = new String[letters.length()];
                for (int i = 0; i < letters.length(); i++) {
                    this.letters[i] = String.valueOf(letters.charAt(i));
                }
            } else if (!existLettersAttr) {
                this.letters = null;
            }
        }
        
        if (!isInEditMode()) {
            mTextSize = SimpleUtil.getScaledValueByHeight(mTextSize);
            minTextSize = SimpleUtil.getScaledValueByHeight(minTextSize);
            
            mTvToast = new TextView(context);
            mTvToast.setTextSize(TypedValue.COMPLEX_UNIT_PX, SimpleUtil.getScaledValue(50, true));
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
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (letters == null || letters.length == 0) {
            return;
        }
        
        float drawW = getWidth() - getPaddingLeft() - getPaddingRight();
        float drawH = mIndexHeight * letters.length;
        if (drawW <= 0 || drawH <= 0) {
            return;
        }
        
        mIndexTotalHeight = drawH;
        canvas.save();
        canvas.translate((getWidth() - drawW) / 2f + getPaddingStart() - getPaddingEnd(),
            (getHeight() - drawH) / 2f + getPaddingTop() - getPaddingBottom());
        
        float widthCenter = drawW / 2.0f;
        float startY = (mIndexHeight + mLetterHeight) / 2f;
        for (int i = 0; i < letters.length; i++) {
            String letter = letters[i];
            if (TextUtils.isEmpty(letter)) {
                continue;
            }
            
            canvas.drawText(letter, widthCenter - mPaint.measureText(letter) / 2, startY + mIndexHeight * i, mPaint);
        }
        canvas.restore();
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (letters == null || letters.length == 0 || mIndexTotalHeight <= 0
            || (!mShowToast && mOnIndexTouchListener == null && mOnLetterChosenListener == null)) {
            return super.onTouchEvent(event);
        }
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                getParent().requestDisallowInterceptTouchEvent(true);
                // 按下的坐标除去偏移值 / 每个字符所占高度，计算字符所在位置
                int position = (int) ((event.getY() - getPaddingTop() + getPaddingBottom() - (getHeight() - mIndexTotalHeight) / 2) / mIndexHeight);
                if (position >= 0 && position < letters.length) {
                    if (mShowToast) {
                        showToast(letters[position]);
                    }
                    
                    if (mOnIndexTouchListener != null) {
                        mOnIndexTouchListener.onTouch(letters[position], position);
                    }
                    
                    if (mOnLetterChosenListener != null) {
                        mOnLetterChosenListener.onChosen(letters[position], position);
                    }
                }
            }
            return true;
            
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                hideToast();
                getParent().requestDisallowInterceptTouchEvent(false);
                if (mOnIndexTouchListener != null) {
                    mOnIndexTouchListener.onActionUp();
                }
                return true;
            
            default:
                break;
        }
        
        return super.onTouchEvent(event);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mSizeScale = calculationItemScale(heightMeasureSpec);
        mPaint.setTextSize(mTextSize * mSizeScale);
        
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            float measureText = -1;
            if (letters != null && letters.length > 0) {
                for (String letter : letters) {
                    if (TextUtils.isEmpty(letter)) {
                        continue;
                    }
                    
                    float width = mPaint.measureText(letter);
                    if (width > measureText) {
                        measureText = width;
                    }
                }
            }
            
            if (measureText == -1) {
                if (widthMode == MeasureSpec.UNSPECIFIED) {
                    widthSize = getPaddingStart() + getPaddingEnd();
                } else {
                    widthSize = Math.min(widthSize, (int) (getPaddingStart() + getPaddingEnd() + 0.5f));
                }
            } else {
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
        mLetterHeight = fontMetrics.descent - fontMetrics.ascent;
        mIndexHeight = mLetterHeight * mLetterSpacing;
        if (heightMode == MeasureSpec.EXACTLY) {
            // xml中指定大小或MATCH_PARENT
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        
        // 未指定大小，需要自己测量高度
        if (letters == null || letters.length == 0) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(getPaddingTop() + getPaddingBottom(), MeasureSpec.EXACTLY));
        } else {
            int totalHeight = (int) (mIndexHeight * letters.length + getPaddingTop() + getPaddingBottom() + 0.5f);
            if (heightMode == MeasureSpec.UNSPECIFIED || heightSize >= totalHeight) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((int) totalHeight, MeasureSpec.EXACTLY));
            } else {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
            }
        }
    }
    
    /**
     * 计算索引字符缩放比例
     */
    private float calculationItemScale(int heightMeasureSpec) {
        if (letters == null || letters.length == 0) {
            return 1;
        }
        
        if (!mScaleNeedCalculation) {
            return mSizeScale;
        }
        
        mScaleNeedCalculation = false;
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (minTextSize < mTextSize && (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST)) {
            mPaint.setTextSize(mTextSize);
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            float measuredHeight = fontMetrics.descent - fontMetrics.ascent;
            float itemTotalHeight = letters.length * measuredHeight * mLetterSpacing;
            float padding = getPaddingTop() + getPaddingBottom();
            if (heightSize >= itemTotalHeight + padding) {
                return 1;
            }
            
            float scale = (heightSize - padding) / itemTotalHeight;
            if (minTextSize < mTextSize && mTextSize * scale < minTextSize) {
                return minTextSize / mTextSize;
            }
            
            return scale;
        } else if (minTextSize > mTextSize) {
            return minTextSize / mTextSize;
        } else {
            return 1;
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
        mIndexTotalHeight = 0;
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
        if ((letters == null && this.letters != null)
            || (letters != null && this.letters == null)
            || (letters != null && this.letters.length != letters.length)) {
            mScaleNeedCalculation = true;
        }
        this.letters = letters;
        requestLayout();
    }
    
    public void setTextSize(float textSize) {
        if (textSize != mTextSize) {
            mScaleNeedCalculation = true;
        }
        mTextSize = textSize;
        requestLayout();
    }
    
    /**
     * 设置最小文字大小，如果索引控件指定高度，则在保证索引字符不小于最小文字大小的前提下尽可能将所有索引绘制出来。
     * 此值设置得越小，越能保证所有机型下绘制最全索引数据
     */
    public void setMinTextSize(float minTextSize) {
        if (minTextSize != minTextSize) {
            mScaleNeedCalculation = true;
        }
        this.minTextSize = minTextSize;
        requestLayout();
    }
    
    /**
     * 设置索引字符之间的间距
     */
    public void setLetterSpacing(float letterSpacing) {
        if (letterSpacing != mLetterSpacing) {
            mScaleNeedCalculation = true;
        }
        mLetterSpacing = letterSpacing;
        requestLayout();
    }
    
    public void setTextColor(@ColorInt int color) {
        mPaint.setColor(color);
        invalidate();
    }
    
    /**
     * 设置索引触摸监听
     */
    public void setOnIndexTouchListener(OnIndexTouchListener listener) {
        this.mOnIndexTouchListener = listener;
    }
    
    /**
     * 设置索引字符选中监听
     */
    public void setOnLetterChosenListener(OnLetterChosenListener listener) {
        mOnLetterChosenListener = listener;
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
    
    public void setToastHideDelayTime(long delayTime) {
        mToastHideDelayTime = delayTime;
    }
    
    /**
     * 索引触摸监听
     */
    public interface OnIndexTouchListener {
        /**
         * 索引被触摸(包括点击和移动)
         *
         * @param letter   当前触摸区域的索引值
         * @param position 当前触摸索引值在索引列表中的位置
         */
        void onTouch(String letter, int position);
        
        /**
         * 手指抬起
         */
        void onActionUp();
    }
    
    /**
     * 索引选中监听
     */
    public interface OnLetterChosenListener {
        /**
         * 索引字符被选中
         *
         * @param letter   当前触摸区域的索引值
         * @param position 当前触摸索引值在索引列表中的位置
         */
        void onChosen(String letter, int position);
    }
}
