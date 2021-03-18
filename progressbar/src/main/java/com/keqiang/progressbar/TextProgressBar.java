package com.keqiang.progressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;

/**
 * 可以设置进度条文本的ProgressBar
 *
 * @author 汪高皖
 */
public class TextProgressBar extends ProgressBar {
    private static final ProgressTextFormatter DEFAULT_FORMATTER =
        textProgressBar -> String.valueOf(textProgressBar.getProgress());
    
    private final ProgressTextColorFactory DEFAULT_COLOR_FACTORY = new ProgressTextColorFactory() {
        @Override
        public int getColor(TextProgressBar textProgressBar, int status) {
            return textColor;
        }
    };
    
    /**
     * 是否启用绘制进度条文本
     */
    private boolean mDrawProgressText = true;
    private int textColor = Color.BLACK;
    private int mProgressTextPadding = 0;
    
    private Paint mTextPaint;
    private Rect mTextRect;
    
    private ProgressTextFormatter mProgressTextFormatter = DEFAULT_FORMATTER;
    private ProgressTextColorFactory mProgressTextColorFactory = DEFAULT_COLOR_FACTORY;
    
    public TextProgressBar(Context context) {
        this(context, null);
    }
    
    public TextProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public TextProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TextProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextRect = new Rect();
        
        TypedArray array = null;
        try {
            array = context.obtainStyledAttributes(attrs, R.styleable.TextProgressBar);
            textColor = array.getColor(R.styleable.TextProgressBar_progress_text_color, Color.BLACK);
            mTextPaint.setTextSize(array.getDimension(R.styleable.TextProgressBar_progress_text_size, 16));
            mProgressTextPadding = array.getDimensionPixelOffset(R.styleable.TextProgressBar_progress_text_padding, 0);
            mDrawProgressText = array.getBoolean(R.styleable.TextProgressBar_isDrawProgressText, true);
        } finally {
            if (array != null) {
                array.recycle();
            }
        }
    }
    
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (!mDrawProgressText) {
            return;
        }
        
        String progressText = mProgressTextFormatter.format(this);
        if (!TextUtils.isEmpty(progressText)) {
            mTextPaint.getTextBounds(progressText, 0, progressText.length(), mTextRect);
            int progressWidth = getWidth() * getProgress() / getMax();
            int textWidth = mTextRect.width();
            if (progressWidth >= textWidth + mProgressTextPadding) {
                mTextPaint.setColor(mProgressTextColorFactory.getColor(this, 1));
                int x = progressWidth - mTextRect.centerX() - mProgressTextPadding;
                int y = (getHeight() / 2) - mTextRect.centerY();
                canvas.drawText(progressText, x, y, mTextPaint);
            } else {
                mTextPaint.setColor(mProgressTextColorFactory.getColor(this, -1));
                int x = progressWidth + mTextRect.centerX() + mProgressTextPadding;
                int y = (getHeight() / 2) - mTextRect.centerY();
                canvas.drawText(progressText, x, y, mTextPaint);
            }
        }
    }
    
    /**
     * 设置进度文本颜色
     *
     * @param color 颜色值
     */
    public void setProgressTextColor(@ColorInt int color) {
        mTextPaint.setColor(color);
        invalidate();
    }
    
    /**
     * 设置进度文本字体大小
     *
     * @param size 单位px，必须大于0
     */
    public void setProgressTextSize(float size) {
        mTextPaint.setTextSize(size);
        invalidate();
    }
    
    /**
     * 设置文本内容距离进度条（通过调用{@link #setProgress(int)}控制的进度条）顶部的距离
     *
     * @param progressTextPadding 单位px
     */
    public void setProgressTextPadding(int progressTextPadding) {
        this.mProgressTextPadding = progressTextPadding;
        invalidate();
    }
    
    /**
     * 设置进度文本内容
     *
     * @param progressTextFormatter 进度条文本内容格式化接口
     */
    public void setProgressTextFormatter(ProgressTextFormatter progressTextFormatter) {
        if (progressTextFormatter == null) {
            mProgressTextFormatter = DEFAULT_FORMATTER;
        } else {
            mProgressTextFormatter = progressTextFormatter;
        }
        invalidate();
    }
    
    /**
     * 设置进度文本绘制时颜色生产工厂
     *
     * @param factory 进度条文本绘制时颜色生产工厂
     */
    public void setProgressTextColorFactory(ProgressTextColorFactory factory) {
        if (factory == null) {
            mProgressTextColorFactory = DEFAULT_COLOR_FACTORY;
        } else {
            mProgressTextColorFactory = factory;
        }
        invalidate();
    }
    
    /**
     * 设置是否启用绘制进度条文本
     *
     * @param isEnable {@code true} 启用，默认值
     *                 {@code false} 不启用
     */
    public void setDrawProgressText(boolean isEnable) {
        mDrawProgressText = isEnable;
        invalidate();
    }
    
    /**
     * 进度条文本格式化接口
     */
    public interface ProgressTextFormatter {
        /**
         * 格式化进度数据
         *
         * @param textProgressBar 当前进度条
         * @return 要显示的进度内容
         */
        String format(TextProgressBar textProgressBar);
    }
    
    /**
     * 用于获取文本绘制时颜色选取
     */
    public interface ProgressTextColorFactory {
        /**
         * 返回当前进度文本的颜色值
         *
         * @param textProgressBar 当前进度条
         * @param status          <0 表示进度条背景比文本宽度小，此时文本显示在进度条外部
         *                        >=0 表示进度条背景比文本宽度大，此时文本绘制在进度条内部
         * @return 颜色值
         */
        int getColor(TextProgressBar textProgressBar, int status);
    }
}
