package com.keqiang.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * {@link TextView}扩展，扩展功能如下：
 * <ul>
 *     <li>支持配置文本超出一行时，是否自动靠左排版</li>
 *     <li>支持配置文本仅根据控件宽度自动换行，优化原生汉字、英文、数字混合文本换行位置大量留白问题</li>
 * </ul>
 *
 * @author Created by wanggaowan on 1/22/21 10:13 AM
 */
public class ExtendTextView extends AppCompatTextView {
    /**
     * 当文本行数超过一行时，不启用自动靠左排版
     */
    public static final int GRAVITY_RTL_NONE = 0;
    /**
     * 当文本行数超过一行时，如果文本默认排版为靠右排版，则启用自动靠左排版
     */
    public static final int GRAVITY_RTL_END = 1;
    
    /**
     * 当文本行数超过一行时，如果文本默认排版为水平居中排版，则启用自动靠左排版
     */
    public static final int GRAVITY_RTL_CENTER_HORIZONTAL = 2;
    
    @IntDef({GRAVITY_RTL_NONE, GRAVITY_RTL_END, GRAVITY_RTL_CENTER_HORIZONTAL})
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    private @interface GravityRtl {}
    
    private int mDefGravity;
    private CharSequence mDefText;
    
    /**
     * 是否根据文本内容行数自动进行左右排版方式调整
     */
    private int mAutoGravityRtl;
    
    /**
     * 是否自动换行，优化原生汉字、英文、数字混合文本换行位置大量留白问题
     */
    private boolean mAutoWrapByWidth = false;
    private String mAutoWrapText;
    
    public ExtendTextView(@NonNull Context context) {
        this(context, null);
    }
    
    public ExtendTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ExtendTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDefGravity = getGravity();
        parseAttrs(context, attrs, defStyleAttr);
    }
    
    
    /**
     * 解析自定义属性
     *
     * @param attrs        属性集合
     * @param defStyleAttr 定义的属性资源
     */
    @SuppressWarnings("ResourceType")
    private void parseAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) {
            mAutoWrapByWidth = false;
            mAutoGravityRtl = GRAVITY_RTL_END | GRAVITY_RTL_CENTER_HORIZONTAL;
            return;
        }
        
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExtendTextView, defStyleAttr, 0);
            mAutoWrapByWidth = typedArray.getBoolean(R.styleable.ExtendTextView_et_auto_wrap_by_width, false);
            mAutoGravityRtl = typedArray.getInt(R.styleable.ExtendTextView_et_auto_gravity_rtl, GRAVITY_RTL_END | GRAVITY_RTL_CENTER_HORIZONTAL);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }
    
    @SuppressLint("RtlHardcoded")
    @Override
    protected void onDraw(Canvas canvas) {
        if (mAutoGravityRtl != GRAVITY_RTL_NONE) {
            if (!autoGravityRtl(true)) {
                if (getLayout() == null) {
                    // 调用super，创建layout
                    super.onDraw(canvas);
                    autoGravityRtl(false);
                } else {
                    super.onDraw(canvas);
                }
            }
        } else {
            super.onDraw(canvas);
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        autoGravityRtl(false);
    }
    
    /**
     * 自动根据文字行数进行左右排版
     *
     * @param byAutoWrapText 如果{@link #getLayout()}为null,是否根据{@link #mAutoWrapText}进行行数判断
     * @return {@code true}重新进行左右排版
     */
    @SuppressLint("RtlHardcoded")
    private boolean autoGravityRtl(boolean byAutoWrapText) {
        if (mAutoGravityRtl == GRAVITY_RTL_NONE) {
            return false;
        }
        
        Layout layout = getLayout();
        if (layout == null && !byAutoWrapText) {
            return false;
        }
        
        int gravity = getGravity();
        int lineCount = getLineCount();
        if (lineCount == 0 && mAutoWrapText != null) {
            lineCount = mAutoWrapText.split("\n").length;
        }
        
        if ((mAutoGravityRtl & GRAVITY_RTL_END) == GRAVITY_RTL_END) {
            if ((gravity & Gravity.END) == Gravity.END || (gravity & Gravity.RIGHT) == Gravity.RIGHT) {
                if (lineCount > 1) {
                    gravity &= ~Gravity.END;
                    gravity |= Gravity.START;
                    super.setGravity(gravity);
                    return true;
                }
                
                return false;
            }
        }
        
        if ((mAutoGravityRtl & GRAVITY_RTL_CENTER_HORIZONTAL) == GRAVITY_RTL_CENTER_HORIZONTAL) {
            // 判断水平居中之前，需要先判断不是居左或居右，因为居左居右均包含Gravity.CENTER_HORIZONTAL，具体可看Gravity定义的常量值
            if ((gravity & Gravity.LEFT) != Gravity.LEFT
                && (gravity & Gravity.RIGHT) != Gravity.RIGHT
                && (gravity & Gravity.CENTER_HORIZONTAL) == Gravity.CENTER_HORIZONTAL) {
                if (lineCount > 1) {
                    gravity &= ~Gravity.CENTER_HORIZONTAL;
                    gravity |= Gravity.START;
                    super.setGravity(gravity);
                    return true;
                }
                
                return false;
            }
        }
        
        if (lineCount <= 1 && gravity != mDefGravity) {
            super.setGravity(mDefGravity);
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!mAutoWrapByWidth) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            autoGravityRtl(false);
            return;
        }
        
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        if (mode == MeasureSpec.UNSPECIFIED) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            autoGravityRtl(false);
            return;
        }
        
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        reDrawText(parentWidth);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw) {
            reDrawText(w);
        } else {
            autoGravityRtl(false);
        }
    }
    
    @Override
    public void setText(CharSequence text, BufferType type) {
        mDefText = text;
        super.setText(text, type);
    }
    
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        String newText = text.toString();
        if (!newText.equals(mAutoWrapText)) {
            reDrawText(this.getWidth());
        } else {
            autoGravityRtl(true);
        }
    }
    
    @Override
    public void setGravity(int gravity) {
        mDefGravity = gravity;
        // super.setGravity(gravity)进行了重新赋值，因此此处需要重新赋值
        if ((mDefGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
            mDefGravity |= Gravity.START;
        }
        if ((mDefGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
            mDefGravity |= Gravity.TOP;
        }
        
        super.setGravity(gravity);
    }
    
    /**
     * 重新绘制字符
     */
    private void reDrawText(int viewWidth) {
        int width = viewWidth - getPaddingLeft() - getPaddingRight();
        Drawable[] drawables = getCompoundDrawables();
        if (drawables[0] != null) {
            width -= getCompoundDrawablePadding();
        }
        
        if (drawables[2] != null) {
            width -= getCompoundDrawablePadding();
        }
        
        
        int height = getMeasuredHeight();
        mAutoWrapText = null;
        if (!mAutoWrapByWidth || TextUtils.isEmpty(mDefText) || width <= 0 || height <= 0) {
            autoGravityRtl(false);
            return;
        }
        
        String newText = autoSplitText(mDefText.toString(), width);
        if (!TextUtils.isEmpty(newText)) {
            mAutoWrapText = newText;
            super.setText(newText, BufferType.NORMAL);
        } else {
            autoGravityRtl(false);
        }
    }
    
    /**
     * 在超出View宽度的文本后面加上"\n"符
     *
     * @param rawText 需要处理的文本
     * @param width   当前文本可绘制宽度
     * @return 处理后的文本
     */
    private String autoSplitText(String rawText, int width) {
        final Paint tvPaint = getPaint();
        if (tvPaint.measureText(rawText) <= width) {
            return rawText;
        }
        
        String[] rawTextLines = rawText.replaceAll("\r", "").split("\n");
        StringBuilder sbNewText = new StringBuilder();
        
        for (int i = 0; i < rawTextLines.length; i++) {
            if (i > 0) {
                sbNewText.append("\n");
            }
            
            String rawTextLine = rawTextLines[i];
            if (tvPaint.measureText(rawTextLine) <= width) {
                // 如果整行宽度在控件可用宽度之内,就不处理了
                sbNewText.append(rawTextLine);
            } else {
                // 如果整行宽度超过控件可用宽度,则按字符测量,在超过可用宽度的前一个字符处手动换行
                float lineWidth = 0;
                for (int cnt = 0; cnt != rawTextLine.length(); ++cnt) {
                    String ch = rawTextLine.substring(cnt, cnt + 1);
                    lineWidth += tvPaint.measureText(ch);
                    if (lineWidth == width) {
                        sbNewText.append(ch).append("\n");
                        lineWidth = 0;
                    } else if (lineWidth < width) {
                        sbNewText.append(ch);
                    } else {
                        sbNewText.append("\n");
                        lineWidth = 0;
                        --cnt;
                    }
                }
            }
        }
        return sbNewText.toString();
    }
    
    /**
     * 设置是否根据文本内容行数自动进行左右排版方式调整。
     * 如果为{@code true},则超过一行，主动按start到end排版，不管默认排版是否是start到end或center_horizontal
     */
    public void setAutoGravityRtl(@GravityRtl int autoGravityRtl) {
        mAutoGravityRtl = autoGravityRtl;
        invalidate();
    }
    
    /**
     * 是否根据文本内容行数自动进行左右排版方式调整
     */
    @GravityRtl
    public int isAutoGravityRtl() {
        return mAutoGravityRtl;
    }
    
    /**
     * 设置文本是否自动按照宽度换行，而不是按照单词和宽度换行，解决原生汉字、英文、数字混合文本在中文环境下换行位置不尽如人意的问题
     */
    public void setAutoWrapByWidth(boolean autoWrapByWidth) {
        if (autoWrapByWidth == mAutoWrapByWidth) {
            return;
        }
        mAutoWrapByWidth = autoWrapByWidth;
        super.setText(mDefText);
    }
    
    /**
     * 文本是否自动按照宽度换行，而不是按照单词和宽度换行
     */
    public boolean isAutoWrapByWidth() {
        return mAutoWrapByWidth;
    }
}
