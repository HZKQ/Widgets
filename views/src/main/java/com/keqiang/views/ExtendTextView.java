package com.keqiang.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.keqiang.views.edittext.SimpleTextWatcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * {@link TextView}扩展，扩展功能如下：
 * <ul>
 *     <li>支持设置文本时不触发{@link TextWatcher}监听</li>
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
    
    private final TextWatcherInner mTextWatcherInner;
    
    private int mOriginalGravity;
    private CharSequence mOriginalText;
    
    /**
     * 是否根据文本内容行数自动进行左右排版方式调整
     */
    private int mAutoGravityRtl;
    
    /**
     * 是否自动换行，优化原生汉字、英文、数字混合文本换行位置大量留白问题
     */
    private boolean mAutoWrapByWidth = false;
    private CharSequence mAutoWrapText;
    
    public ExtendTextView(@NonNull Context context) {
        this(context, null);
    }
    
    public ExtendTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }
    
    public ExtendTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTextWatcherInner = new TextWatcherInner();
        mOriginalGravity = getGravity();
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
            lineCount = mAutoWrapText.toString().split("\n").length;
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
        
        if (lineCount <= 1 && gravity != mOriginalGravity) {
            super.setGravity(mOriginalGravity);
            return true;
        } else {
            return false;
        }
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
        mOriginalText = text;
        
        // TextView只要调用addTextChangedListener()新增文本监听或调用setKeyListener()设置按键监听，
        // 均视为编辑状态，此状态下setEllipsize()将无效，因此设置文本之前，如果外部未设置文本监听，则移除
        if (mTextWatcherInner != null && mTextWatcherInner.isOutSideNotNeedListen()) {
            super.removeTextChangedListener(mTextWatcherInner);
        }
        
        super.setText(text, type);
        
        if (mTextWatcherInner != null && mTextWatcherInner.isOutSideNotNeedListen()) {
            super.addTextChangedListener(mTextWatcherInner);
        }
    }
    
    /**
     * 如果{@link #isAutoWrapByWidth()}为true，则使用此方法得到的文本可能是裁剪后内容，
     * 与调用{@link #setText(CharSequence, BufferType)}相关方法传递的原始文本不一致，
     * 如果要得到原始文本，请调用{@link #getOriginalText()}
     */
    @Override
    public CharSequence getText() {
        return super.getText();
    }
    
    /**
     * 获取原始文本
     */
    public CharSequence getOriginalText() {
        return mOriginalText;
    }
    
    /**
     * 设置文本但不触发{@link TextWatcher}监听
     */
    public void setTextNoListen(CharSequence charSequence) {
        mTextWatcherInner.setCallListener(false);
        setText(charSequence);
        mTextWatcherInner.setCallListener(true);
    }
    
    /**
     * 设置文本但不触发{@link TextWatcher}监听
     */
    public void setTextNoListen(CharSequence text, BufferType type) {
        mTextWatcherInner.setCallListener(false);
        setText(text, type);
        mTextWatcherInner.setCallListener(true);
    }
    
    /**
     * 设置文本但不触发{@link TextWatcher}监听
     */
    public void setTextNoListen(@StringRes int resid) {
        mTextWatcherInner.setCallListener(false);
        setText(resid);
        mTextWatcherInner.setCallListener(true);
    }
    
    /**
     * 设置文本但不触发{@link TextWatcher}监听
     */
    public void setTextNoListen(@StringRes int resid, BufferType type) {
        mTextWatcherInner.setCallListener(false);
        setText(resid, type);
        mTextWatcherInner.setCallListener(true);
    }
    
    /**
     * 设置文本但不触发{@link TextWatcher}监听
     */
    public void setTextNoListen(char[] text, int start, int len) {
        mTextWatcherInner.setCallListener(false);
        setText(text, start, len);
        mTextWatcherInner.setCallListener(true);
    }
    
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        String newText = text == null ? "" : text.toString();
        String oldText = mAutoWrapText == null ? "" : mAutoWrapText.toString();
        if (!newText.equals(oldText)) {
            reDrawText(this.getWidth());
        } else {
            autoGravityRtl(true);
        }
    }
    
    @Override
    public void setGravity(int gravity) {
        mOriginalGravity = gravity;
        // super.setGravity(gravity)进行了重新赋值，因此此处需要重新赋值
        if ((mOriginalGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
            mOriginalGravity |= Gravity.START;
        }
        if ((mOriginalGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
            mOriginalGravity |= Gravity.TOP;
        }
        
        super.setGravity(gravity);
    }
    
    /**
     * 重新绘制字符
     */
    private void reDrawText(int viewWidth) {
        mAutoWrapText = null;
        if (!isAutoWrapByWidth() || TextUtils.isEmpty(mOriginalText)) {
            autoGravityRtl(false);
            return;
        }
        
        int width = viewWidth - getPaddingStart() - getPaddingEnd();
        Drawable[] drawables = getCompoundDrawables();
        if (drawables[0] != null) {
            width -= getCompoundDrawablePadding();
        }
        
        if (drawables[2] != null) {
            width -= getCompoundDrawablePadding();
        }
        
        int height = getMeasuredHeight();
        if (width <= 0 || height <= 0) {
            autoGravityRtl(false);
            return;
        }
        
        int maxLine = getMaxLines() == -1 ? Integer.MAX_VALUE : getMaxLines();
        CharSequence newText = TextViewUtils.autoSplitText(mOriginalText, getPaint(), width, maxLine);
        if (!TextUtils.isEmpty(newText)) {
            mAutoWrapText = newText;
            super.setText(newText);
        } else {
            autoGravityRtl(false);
        }
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
        
        if (mAutoWrapByWidth == isAutoWrapByWidth()) {
            return;
        }
        
        super.setText(mOriginalText);
    }
    
    /**
     * 文本是否自动按照控件宽度换行，而不是按照单词+宽度策略换行
     */
    public boolean isAutoWrapByWidth() {
        return mAutoWrapByWidth;
    }
    
    private static final class TextWatcherInner implements TextWatcher {
        
        private List<TextWatcher> mTextWatchers;
        /**
         * 是否需要触发监听
         */
        private boolean mCallListener = true;
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (mTextWatchers != null) {
                for (TextWatcher watcher : mTextWatchers) {
                    if (mCallListener) {
                        watcher.beforeTextChanged(s, start, count, after);
                    } else if (watcher instanceof SimpleTextWatcher
                        && ((SimpleTextWatcher) watcher).isForceCall()) {
                        watcher.beforeTextChanged(s, start, count, after);
                    }
                }
            }
        }
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mCallListener && mTextWatchers != null) {
                for (TextWatcher watcher : mTextWatchers) {
                    if (mCallListener) {
                        watcher.onTextChanged(s, start, before, count);
                    } else if (watcher instanceof SimpleTextWatcher
                        && ((SimpleTextWatcher) watcher).isForceCall()) {
                        watcher.onTextChanged(s, start, before, count);
                    }
                }
            }
        }
        
        @Override
        public void afterTextChanged(Editable s) {
            if (mCallListener && mTextWatchers != null) {
                for (TextWatcher watcher : mTextWatchers) {
                    if (mCallListener) {
                        watcher.afterTextChanged(s);
                    } else if (watcher instanceof SimpleTextWatcher
                        && ((SimpleTextWatcher) watcher).isForceCall()) {
                        watcher.afterTextChanged(s);
                    }
                }
            }
        }
        
        public void setCallListener(boolean callListener) {
            mCallListener = callListener;
        }
        
        /**
         * 外部设置的文本监听器是否为空
         */
        boolean isOutSideListenerEmpty() {
            return mTextWatchers == null || mTextWatchers.size() == 0;
        }
        
        /**
         * 外部是否不需要监听内容变化
         */
        boolean isOutSideNotNeedListen() {
            return isOutSideListenerEmpty() || !mCallListener;
        }
        
        public void addTextWatcher(TextWatcher textWatcher) {
            if (mTextWatchers == null) {
                mTextWatchers = new ArrayList<>();
            }
            mTextWatchers.add(textWatcher);
        }
        
        public void removeTextWatcher(TextWatcher textWatcher) {
            if (mTextWatchers != null) {
                int i = mTextWatchers.indexOf(textWatcher);
                if (i >= 0) {
                    mTextWatchers.remove(i);
                }
            }
        }
        
        public void removeAllTextWatcher() {
            mTextWatchers.clear();
        }
    }
}
