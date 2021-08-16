package com.keqiang.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.keqiang.views.edittext.NumberLimitTextWatcher;
import com.keqiang.views.edittext.NumberOverLimitListener;
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
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;


/**
 * {@link EditText}扩展，扩展功能如下：
 * <ul>
 *     <li>支持配置一键清除按钮</li>
 *     <li>支持配置获取焦点时是否弹出软键盘，兼容低版本</li>
 *     <li>支持设置文本时不触发{@link TextWatcher}监听</li>
 *     <li>当输入类型为Number、NumberDecimal时，支持配置整数最大位数，小数最大位数以及自动去除整数位无效0值</li>
 *     <li>支持配置文本超出一行时，是否自动靠左排版</li>
 *     <li>支持配置文本仅根据控件宽度自动换行，优化原生汉字、英文、数字混合文本换行位置大量留白问题</li>
 *     <li>支持配置非编辑模式下最大行数</li>
 *     <li>修复原生控件设置最大为一行时，未设置InputType，当内容超出一行时，第二行部分可见Bug</li>
 * </ul>
 *
 * @author Created by 汪高皖 on 2018-05-28 14:29
 */
@SuppressWarnings("FieldCanBeLocal")
public class ExtendEditText extends AppCompatEditText {
    public static final int MATCH_PARENT = -1;
    public static final int WRAP_CONTENT = -2;
    
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
    
    private static final Drawable PLACEHOLDER = new ColorDrawable(Color.TRANSPARENT);
    private static final int DEFAULT_CLEAR_BUTTON_PADDING = 10;
    private static final int DEFAULT_CLEAR_BUTTON_SIZE = 50;
    
    private Context mContext;
    private final TextWatcherInner mTextWatcherInner;
    
    private Drawable mClearButton;
    private int mClearButtonWidth;
    private int mClearButtonHeight;
    private int mClearButtonPaddingLeft;
    private int mClearButtonPaddingRight;
    private int mClearButtonMarginLeft;
    private int mClearButtonMarginRight;
    private Rect mClearButtonClickRange;
    private Float mHintTextSize;
    private float mTextSize;
    private int mCompoundDrawablePadding;
    
    /**
     * 是否启用一键清除文本按钮
     * true：启用 false：不启用
     */
    private boolean mClearButtonEnabled = true;
    
    /**
     * 是否获取焦点时才展示清除按钮，默认false
     */
    private boolean mOnFocusShowClearButtonEnable = false;
    
    /**
     * 清除文本按钮是否显示
     */
    private boolean mClearButtonVisibility = false;
    
    /**
     * 是否需要重新测量清除按钮大小
     */
    private boolean needMeasureClearButtonSizeAgain = true;
    
    private int originalEditTextWidth = 0;
    private int originalEditTextHeight = 0;
    
    /**
     * 记录当单行文本且文字超出控件宽度时，用户水平滑动文字的距离
     */
    private int scrollXLength = 0;
    /**
     * 记录当多行文本且文字超出控件高度时，用户垂直滑动文字的距离
     */
    private int scrollYLength = 0;
    
    /**
     * EditText获取焦点时系统软键盘是否弹出
     */
    private boolean softInputOnFocusShow = true;
    
    /**
     * 当前内容是否是用户通过软键盘或其它外部输入设备输入，而非通过setText相关方法设置
     */
    private long mUserEnterContent = 0;
    
    /**
     * 焦点监听
     */
    private OnFocusChangeListener mOnFocusChangeListener;
    
    private View.OnClickListener mClearButtonClickListener;
    
    private boolean mJustDrawPaddingChange = false;
    
    private Editable mEmptyEditable;
    
    private int mDefGravity;
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
    
    private NumberLimitTextWatcherInner mNumberLimitTextWatcher;
    private int mDecimalLimit = Integer.MAX_VALUE;
    private int mIntegerLimit = Integer.MAX_VALUE;
    private boolean mAutoRemoveInValidZero = true;
    private NumberOverLimitListener mNumberOverLimitListener;
    private boolean mSetTextUseNumberLimit;
    
    @Nullable
    private Integer mOriginalMaxLines;
    /**
     * enable为false时最大行数,默认和{@link #getMaxLines()}一致
     */
    @Nullable
    private Integer mDisableMaxLines;
    
    // 原生默认值
    private int mOriginalInputType = EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
    private boolean mNotChangeOriginalInputType;
    
    public ExtendEditText(Context context) {
        this(context, null);
    }
    
    public ExtendEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }
    
    public ExtendEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTextWatcherInner = new TextWatcherInner();
        init(context, attrs, defStyleAttr);
    }
    
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        PLACEHOLDER.setBounds(0, 0, 0, 0);
        mContext = context;
        mClearButton = ContextCompat.getDrawable(context, R.drawable.undo);
        mClearButtonClickRange = new Rect();
        mTextSize = super.getTextSize();
        parseAttrs(context, attrs, defStyleAttr);
        mCompoundDrawablePadding = super.getCompoundDrawablePadding();
        Drawable[] drawables = super.getCompoundDrawables();
        if (drawables[2] == null) {
            setCompoundDrawables(drawables[0], drawables[1], null, drawables[3]);
        }
        
        mOriginalMaxLines = getMaxLines();
        mOriginalInputType = getInputType();
        if (!isEnabled() && mDisableMaxLines != null) {
            super.setMaxLines(mDisableMaxLines);
        }
        
        if (isNeedFixSingleLineBug(getMaxLines(), mOriginalInputType)) {
            setSuperInputType(mOriginalInputType & ~EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
        }
        
        mNumberLimitTextWatcher = new NumberLimitTextWatcherInner(this, mDecimalLimit, mIntegerLimit, mAutoRemoveInValidZero);
        super.addTextChangedListener(mNumberLimitTextWatcher);
        super.addTextChangedListener(mTextWatcherInner);
        super.setOnFocusChangeListener(mFocusChangeListener);
    }
    
    /**
     * 是否需要修复原生控件设置最大为一行时，未设置InputType，当内容超出一行时，第二行部分可见Bug
     */
    private boolean isNeedFixSingleLineBug(int maxLines, int inputType) {
        if (maxLines != 1) {
            return false;
        }
        
        return (inputType & EditorInfo.TYPE_MASK_CLASS) == EditorInfo.TYPE_CLASS_TEXT
            && (inputType & EditorInfo.TYPE_MASK_FLAGS) == EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
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
            mClearButtonWidth = DEFAULT_CLEAR_BUTTON_SIZE;
            mClearButtonHeight = DEFAULT_CLEAR_BUTTON_SIZE;
            mClearButtonPaddingLeft = mClearButtonPaddingRight = 0;
            mClearButtonMarginLeft = mClearButtonMarginRight = 0;
            mOnFocusShowClearButtonEnable = false;
            mHintTextSize = null;
            mAutoGravityRtl = GRAVITY_RTL_END | GRAVITY_RTL_CENTER_HORIZONTAL;
            mAutoWrapByWidth = false;
            mDisableMaxLines = null;
            
            if (!isInEditMode()) {
                mClearButtonWidth = SimpleUtil.getScaledValue(mClearButtonWidth);
                mClearButtonHeight = SimpleUtil.getScaledValue(mClearButtonHeight);
            }
            return;
        }
        
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExtendEditText, defStyleAttr, 0);
            mClearButtonEnabled = typedArray.getBoolean(R.styleable.ExtendEditText_ee_clearButtonEnabled, true);
            
            Drawable drawable = typedArray.getDrawable(R.styleable.ExtendEditText_ee_clearButtonIcon);
            if (drawable != null) {
                mClearButton = drawable;
            }
            
            int size = typedArray.getDimensionPixelOffset(R.styleable.ExtendEditText_ee_clearButtonSize, DEFAULT_CLEAR_BUTTON_SIZE);
            mClearButtonWidth = size;
            mClearButtonHeight = size;
            
            if (typedArray.hasValue(R.styleable.ExtendEditText_ee_clearButtonWidth)) {
                mClearButtonWidth = typedArray.getDimensionPixelOffset(R.styleable.ExtendEditText_ee_clearButtonWidth, DEFAULT_CLEAR_BUTTON_SIZE);
            }
            
            if (typedArray.hasValue(R.styleable.ExtendEditText_ee_clearButtonHeight)) {
                mClearButtonHeight = typedArray.getDimensionPixelOffset(R.styleable.ExtendEditText_ee_clearButtonHeight, DEFAULT_CLEAR_BUTTON_SIZE);
            }
            
            mClearButtonPaddingLeft = typedArray.getDimensionPixelOffset(R.styleable.ExtendEditText_ee_clearButtonPaddingLeft, DEFAULT_CLEAR_BUTTON_PADDING);
            mClearButtonPaddingRight = typedArray.getDimensionPixelOffset(R.styleable.ExtendEditText_ee_clearButtonPaddingRight, DEFAULT_CLEAR_BUTTON_PADDING);
            
            mClearButtonMarginLeft = typedArray.getDimensionPixelOffset(R.styleable.ExtendEditText_ee_clearButtonMarginLeft, 0);
            mClearButtonMarginRight = typedArray.getDimensionPixelOffset(R.styleable.ExtendEditText_ee_clearButtonMarginRight, 0);
            
            if (typedArray.hasValue(R.styleable.ExtendEditText_ee_hintTextSize)) {
                mHintTextSize = typedArray.getDimension(R.styleable.ExtendEditText_ee_hintTextSize, mTextSize);
            } else {
                mHintTextSize = null;
            }
            
            mAutoWrapByWidth = typedArray.getBoolean(R.styleable.ExtendEditText_ee_auto_wrap_by_width, false);
            mAutoGravityRtl = typedArray.getInt(R.styleable.ExtendEditText_ee_auto_gravity_rtl, GRAVITY_RTL_END | GRAVITY_RTL_CENTER_HORIZONTAL);
            mOnFocusShowClearButtonEnable = typedArray.getBoolean(R.styleable.ExtendEditText_ee_onFocusShowClearButtonEnable, false);
            
            mDecimalLimit = typedArray.getInt(R.styleable.ExtendEditText_ee_decimalLimit, Integer.MAX_VALUE);
            mIntegerLimit = typedArray.getInt(R.styleable.ExtendEditText_ee_integerLimit, Integer.MAX_VALUE);
            mAutoRemoveInValidZero = typedArray.getBoolean(R.styleable.ExtendEditText_ee_autoRemoveInValidZero, true);
            mSetTextUseNumberLimit = typedArray.getBoolean(R.styleable.ExtendEditText_ee_setTextUseNumberLimit, false);
            
            if (typedArray.hasValue(R.styleable.ExtendEditText_ee_disableMaxLines)) {
                mDisableMaxLines = typedArray.getInteger(R.styleable.ExtendEditText_ee_disableMaxLines, -1);
            }
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
        
        if (!isInEditMode()) {
            mClearButtonPaddingLeft = SimpleUtil.getScaledValue(mClearButtonPaddingLeft);
            mClearButtonPaddingRight = SimpleUtil.getScaledValue(mClearButtonPaddingRight);
            mClearButtonMarginLeft = SimpleUtil.getScaledValue(mClearButtonMarginLeft);
            mClearButtonMarginRight = SimpleUtil.getScaledValue(mClearButtonMarginRight);
            if (mHintTextSize != null && mHintTextSize != mTextSize) {
                mHintTextSize = SimpleUtil.getScaledValue(mHintTextSize, true);
            }
            
            if (mClearButtonWidth != WRAP_CONTENT && mClearButtonWidth != MATCH_PARENT) {
                mClearButtonWidth = SimpleUtil.getScaledValue(mClearButtonWidth);
            }
            
            if (mClearButtonHeight != WRAP_CONTENT && mClearButtonHeight != MATCH_PARENT) {
                mClearButtonHeight = SimpleUtil.getScaledValue(mClearButtonHeight);
            }
        }
        
        if (mHintTextSize != null && TextUtils.isEmpty(getText().toString().trim())) {
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mHintTextSize);
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        autoGravityRtl(false);
    }
    
    @Override
    public boolean onPreDraw() {
        if (countClearButtonPosition()) {
            return false;
        }
        
        return super.onPreDraw();
    }
    
    /**
     * 计算清除按钮位置、大小数据
     *
     * @return {@code true}:表示有效计算，取消本次绘制流程
     */
    private boolean countClearButtonPosition() {
        if (!needDrawClearButton()) {
            return false;
        }
        
        if (!mJustDrawPaddingChange) {
            int width;
            if (needMeasureClearButtonSizeAgain) {
                // 计算清除按钮的大小
                if (mClearButtonWidth == WRAP_CONTENT) {
                    width = mClearButton.getIntrinsicWidth();
                } else if (mClearButtonWidth == MATCH_PARENT) {
                    width = Math.min(originalEditTextWidth, originalEditTextHeight);
                } else {
                    width = mClearButtonWidth;
                }
                
                int height;
                if (mClearButtonHeight == WRAP_CONTENT) {
                    height = mClearButton.getIntrinsicHeight();
                } else if (mClearButtonHeight == MATCH_PARENT) {
                    height = Math.min(originalEditTextHeight, originalEditTextWidth);
                } else {
                    height = mClearButtonHeight;
                }
                
                mClearButton.setBounds(0, 0, width, height);
                needMeasureClearButtonSizeAgain = false;
            } else {
                Rect bounds = mClearButton.getBounds();
                width = bounds.right;
            }
            
            // 计算清除按钮绘制起点x的坐标
            float canvasXOffset = originalEditTextWidth - width - getPaddingRight()
                - mClearButtonPaddingRight - mClearButtonMarginRight - mCompoundDrawablePadding;
            Drawable drawable = getCompoundDrawables()[2];
            if (drawable != null) {
                canvasXOffset -= drawable.getIntrinsicWidth();
            }
            
            // 清除按钮可点击范围
            mClearButtonClickRange.left = (int) (canvasXOffset - mClearButtonPaddingLeft);
            mClearButtonClickRange.right = (int) (canvasXOffset + width + mClearButtonPaddingRight);
            mClearButtonClickRange.top = 0;
            mClearButtonClickRange.bottom = originalEditTextHeight;
        }
        
        if (isInEditMode()) {
            return false;
        }
        
        int pad = mCompoundDrawablePadding + mClearButtonClickRange.width() + mClearButtonMarginLeft + mClearButtonMarginRight;
        if (pad == super.getCompoundDrawablePadding()) {
            mJustDrawPaddingChange = false;
            return false;
        }
        
        mJustDrawPaddingChange = true;
        super.setCompoundDrawablePadding(pad);
        return true;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            // 预览模式下在onPreDraw调用以下逻辑导致不绘制，无法预览，目前这种方式预览
            // 在内容超出输入框宽度时，会有部分内容被挡住，但是实际运行不会出现此问题
            int pad = mCompoundDrawablePadding + mClearButtonClickRange.width()
                + mClearButtonMarginLeft + mClearButtonMarginRight;
            super.setCompoundDrawablePadding(pad);
        }
        
        if (mAutoGravityRtl != GRAVITY_RTL_NONE) {
            if (!autoGravityRtl(true)) {
                if (getLayout() == null) {
                    // 调用super，创建layout
                    super.onDraw(canvas);
                    autoGravityRtl(false);
                    return;
                }
            } else {
                // 重新进行左右排版
                return;
            }
        }
        
        super.onDraw(canvas);
        
        if (needDrawClearButton()) {
            drawClearButton(canvas);
            return;
        }
        
        if (mClearButtonVisibility) {
            super.setCompoundDrawablePadding(mCompoundDrawablePadding);
            mClearButtonVisibility = false;
        }
    }
    
    /**
     * 判断是否需要绘制clearButton
     */
    private boolean needDrawClearButton() {
        return mClearButtonEnabled && !isTextEmpty() && isEnabled() && (!mOnFocusShowClearButtonEnable || hasFocus());
    }
    
    /**
     * 绘制清除文本按钮
     */
    private void drawClearButton(Canvas canvas) {
        //计算清除按钮绘制起点x,y的坐标
        float canvasYOffset = mClearButtonClickRange.height() / 2f - mClearButton.getBounds().height() / 2f + scrollYLength;
        float canvasXOffset = mClearButtonClickRange.left + mClearButtonPaddingLeft + scrollXLength;
        
        canvas.save();
        canvas.translate(canvasXOffset, canvasYOffset);
        mClearButton.draw(canvas);
        canvas.restore();
        mClearButtonVisibility = true;
    }
    
    @Override
    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
        if (horiz != oldHoriz) {
            scrollXLength = horiz;
        }
        
        if (vert != oldVert) {
            scrollYLength = vert;
        }
    }
    
    @Override
    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        if (right == null) {
            super.setCompoundDrawables(left, top, PLACEHOLDER, bottom);
        } else {
            super.setCompoundDrawables(left, top, right, bottom);
        }
    }
    
    @NonNull
    @Override
    public Drawable[] getCompoundDrawables() {
        Drawable[] drawables = super.getCompoundDrawables();
        if (drawables[2] != null && drawables[2] == PLACEHOLDER) {
            drawables[2] = null;
        }
        return drawables;
    }
    
    /**
     * 设置DrawablePadding，
     *
     * @param pad 当清除文本按钮可用且处于显示状态时，此值会加上清除按钮的Padding
     */
    @Override
    public void setCompoundDrawablePadding(int pad) {
        mCompoundDrawablePadding = pad;
        if (mClearButtonEnabled && mClearButtonVisibility) {
            pad += mClearButtonClickRange.width()
                + mClearButtonMarginLeft
                + mClearButtonMarginRight;
        }
        
        if (pad == super.getCompoundDrawablePadding()) {
            return;
        }
        super.setCompoundDrawablePadding(pad);
    }
    
    /**
     * 获取DrawablePadding，
     */
    @Override
    public int getCompoundDrawablePadding() {
        return mCompoundDrawablePadding;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        originalEditTextWidth = w;
        originalEditTextHeight = h;
        if (w != oldw) {
            reDrawText(w);
        } else {
            autoGravityRtl(false);
        }
    }
    
    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        mOnFocusChangeListener = l;
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isTouchClearButton(event)) {
            //触摸清除按钮时，屏蔽EditText触摸事件
            if (event.getAction() == MotionEvent.ACTION_UP) {
                setText("");
                if (mClearButtonClickListener != null) {
                    mClearButtonClickListener.onClick(this);
                }
            }
            return true;
        } else {
            boolean result;
            if (softInputOnFocusShow) {
                result = super.onTouchEvent(event);
            } else {
                int type = getInputType();
                setSuperInputType(InputType.TYPE_NULL);
                result = super.onTouchEvent(event);
                setSuperInputType(type);
            }
            return result && isEnabled();
        }
    }
    
    /**
     * 焦点监听
     */
    private final OnFocusChangeListener mFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(final View v, boolean hasFocus) {
            if (!softInputOnFocusShow) {
                closeInputMethod();
            }
            
            if (mOnFocusShowClearButtonEnable && hasFocus && needDrawClearButton()) {
                setCursorVisible(false);
                countClearButtonPosition();
                post(() -> {
                    setCursorVisible(true);
                    if (mOnFocusChangeListener != null) {
                        mOnFocusChangeListener.onFocusChange(v, true);
                    }
                });
            } else if (mOnFocusChangeListener != null) {
                mOnFocusChangeListener.onFocusChange(v, hasFocus);
            }
        }
    };
    
    @Override
    public void setText(CharSequence text, BufferType type) {
        if (mNumberLimitTextWatcher != null) {
            mNumberLimitTextWatcher.setCallListener(mSetTextUseNumberLimit);
        }
        
        // 在super.setText(text, type)之上的代码会先于TextWatcher相关方法执行
        super.setText(text, type);
        // 在super.setText(text, type)之下的代码会后于TextWatcher相关方法执行
        
        if (mNumberLimitTextWatcher != null) {
            mNumberLimitTextWatcher.setCallListener(true);
        }
        
        mOriginalText = text;
        mUserEnterContent--;
        if (mUserEnterContent < 0) {
            mUserEnterContent = 0;
        }
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
    public void addTextChangedListener(TextWatcher watcher) {
        if (mTextWatcherInner == null) {
            super.addTextChangedListener(watcher);
        } else {
            mTextWatcherInner.addTextWatcher(watcher);
        }
    }
    
    public void addTextChangedListener(int index, TextWatcher watcher) {
        if (mTextWatcherInner != null) {
            mTextWatcherInner.addTextWatcher(index, watcher);
        }
    }
    
    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
        if (mTextWatcherInner == null) {
            super.removeTextChangedListener(watcher);
        } else {
            mTextWatcherInner.removeTextWatcher(watcher);
        }
    }
    
    public void removeAllTextWatcher() {
        mTextWatcherInner.removeAllTextWatcher();
    }
    
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (isEnabled()) {
            mOriginalText = text;
            autoGravityRtl(false);
            return;
        }
        
        String newText = text == null ? "" : text.toString();
        String oldText = mAutoWrapText == null ? "" : mAutoWrapText.toString();
        if (!newText.equals(oldText)) {
            reDrawText(this.getWidth());
        } else {
            autoGravityRtl(true);
        }
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            // setInputType一定要在setMaxLines之前调用，否则setMaxLines调用后可能达不到预期效果
            // setMaxLines需要使用的inputType可能并不是最新值
            int inputType = mOriginalInputType & EditorInfo.TYPE_MASK_VARIATION;
            if (inputType != EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
                && inputType != EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD
                && inputType != EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD) {
                if ((mDisableMaxLines == null ? getMaxLines() : mDisableMaxLines) == 1) {
                    setSuperInputType(EditorInfo.TYPE_CLASS_TEXT);
                } else {
                    setSuperInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
                }
            }
            
            if (mDisableMaxLines != null) {
                super.setMaxLines(mDisableMaxLines);
            }
        } else {
            // setInputType一定要在setMaxLines之前调用，否则setMaxLines调用后可能达不到预期效果
            // setMaxLines需要使用的inputType可能并不是最新值
            if (mOriginalMaxLines != null) {
                if (isNeedFixSingleLineBug(mOriginalMaxLines, mOriginalInputType)) {
                    setSuperInputType(mOriginalInputType & ~EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
                } else {
                    setSuperInputType(mOriginalInputType);
                }
                super.setMaxLines(mOriginalMaxLines);
            }
        }
        
        super.setEnabled(enabled);
        
        if (mAutoWrapByWidth) {
            super.setText(mOriginalText);
            if (!TextUtils.isEmpty(mOriginalText)) {
                setSelection(mOriginalText.length());
            }
        } else if (needDrawClearButton()) {
            // 设置enable为true时，不会触发onPreDraw，因此手动调用
            post(this :: countClearButtonPosition);
        }
    }
    
    @Override
    public void setMaxLines(int maxLines) {
        this.mOriginalMaxLines = maxLines;
        if (isEnabled() || mDisableMaxLines == null) {
            if (isNeedFixSingleLineBug(maxLines, mOriginalInputType)) {
                setSuperInputType(mOriginalInputType & ~EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
            }
            super.setMaxLines(maxLines);
        }
    }
    
    /**
     * 设置 {@link #isEnabled()}为false时最大行数
     *
     * @param disableMaxLines 如果为null，则始终跟随{@link #setMaxLines(int)}设置的值
     */
    public void setDisableMaxLines(@Nullable Integer disableMaxLines) {
        mDisableMaxLines = disableMaxLines;
        if (!isEnabled() && disableMaxLines != null) {
            super.setMaxLines(disableMaxLines);
        } else if (mOriginalMaxLines != null && getMaxLines() != mOriginalMaxLines) {
            if (isNeedFixSingleLineBug(mOriginalMaxLines, mOriginalInputType)) {
                setSuperInputType(mOriginalInputType & ~EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
            }
            super.setMaxLines(mOriginalMaxLines);
        }
    }
    
    /**
     * 当前控件允许的最大行数，根据{@link #isEnabled()}返回不同值
     *
     * @return {@link #isEnabled()}为true，则返回{@link #getOriginalMaxLines()}<br/>
     * {@link #isEnabled()}为false：如果{@link #getDisableMaxLines()}为null，则返回{@link #getOriginalMaxLines()}，否则返回该值
     */
    @Override
    public int getMaxLines() {
        return super.getMaxLines();
    }
    
    /**
     * {@link #isEnabled()}为true时可显示的最大行数
     */
    public int getOriginalMaxLines() {
        return mOriginalMaxLines == null ? getMaxLines() : mOriginalMaxLines;
    }
    
    /**
     * {@link #isEnabled()}为false时可显示的最大行数
     */
    @Nullable
    public Integer getDisableMaxLines() {
        return mDisableMaxLines;
    }
    
    @Override
    public void setInputType(int type) {
        mOriginalInputType = type;
        if (isNeedFixSingleLineBug(getMaxLines(), type)) {
            type &= ~EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
        }
        setSuperInputType(type);
    }
    
    private void setSuperInputType(int type) {
        mNotChangeOriginalInputType = true;
        super.setInputType(type);
    }
    
    @Override
    public void setRawInputType(int type) {
        if (!mNotChangeOriginalInputType) {
            // 此时是直接调用当前方法，而不是调用setRawInputType是被setInputType调用
            mOriginalInputType = type;
            if (isNeedFixSingleLineBug(getMaxLines(), type)) {
                type &= ~EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
            }
        }
        
        mNotChangeOriginalInputType = false;
        super.setRawInputType(type);
    }
    
    /**
     * 当前控件输入类型，根据{@link #isEnabled()}返回不同值
     *
     * @return {@link #isEnabled()}为true，如果{@link #getMaxLines()} 为1且{@link #getOriginalInputType()}多行文本，
     * 则返回{@link #getOriginalInputType()}但移除{@link EditorInfo#TYPE_TEXT_FLAG_MULTI_LINE}，否则返回{@link #getOriginalInputType()}<br/>
     * {@link #isEnabled()}为false：如果{@link #getOriginalInputType()}为密码输入类型，则返回{@link #getOriginalInputType()}。
     * 如果{@link #getMaxLines()} 为1则返回{@link EditorInfo#TYPE_CLASS_TEXT}，否则返回{@link EditorInfo#TYPE_CLASS_TEXT} | {@link EditorInfo#TYPE_TEXT_FLAG_MULTI_LINE}
     */
    @Override
    public int getInputType() {
        return super.getInputType();
    }
    
    /**
     * 获取用户设置的原始输入类型
     */
    public int getOriginalInputType() {
        return mOriginalInputType;
    }
    
    /**
     * 如果{@link #isAutoWrapByWidth()}为true，则使用此方法得到的文本可能是裁剪后内容，
     * 与调用{@link #setText(CharSequence, BufferType)}相关方法传递的原始文本不一致，
     * 如果要得到原始文本，请调用{@link #getOriginalText()}
     */
    @NonNull
    @Override
    public Editable getText() {
        Editable text = super.getText();
        if (text == null && mEmptyEditable == null) {
            mEmptyEditable = new SpannableStringBuilder();
        }
        return text == null ? mEmptyEditable : text;
    }
    
    /**
     * 获取原始文本
     */
    public CharSequence getOriginalText() {
        return mOriginalText;
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
     * 是否触摸了清除文本的按钮
     *
     * @param event 触摸事件
     * @return {@code true} 表示点击了清除按钮，此时不需要将点击事件传给父类，否则会弹出软键盘
     * {@code false} 表示清除按钮没有被点击，此时清除按钮可能没有显示
     */
    private boolean isTouchClearButton(MotionEvent event) {
        return mClearButtonEnabled
            && mClearButtonVisibility
            && event.getX() >= mClearButtonClickRange.left
            && event.getX() <= mClearButtonClickRange.right;
    }
    
    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        if (unit == TypedValue.COMPLEX_UNIT_PX) {
            mTextSize = size;
        } else {
            mTextSize = super.getTextSize();
        }
        
        if (mHintTextSize != null && TextUtils.isEmpty(getText().toString().trim())) {
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mHintTextSize);
        }
    }
    
    @Override
    public float getTextSize() {
        return mTextSize;
    }
    
    /**
     * 判断文本是否为空
     *
     * @return true：为空
     */
    public boolean isTextEmpty() {
        return "".equals(getText().toString().trim());
    }
    
    /**
     * 设置EditText聚焦时软键盘是否自动弹出.<br>
     * 当设置为不弹出键盘时，那么已经打开的键盘将会关闭
     */
    public void setSoftInputOnFocusShow(boolean show) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            softInputOnFocusShow = show;
        } else {
            //交由系统处理以后，我们自己的处理就忽略掉
            softInputOnFocusShow = true;
            super.setShowSoftInputOnFocus(show);
        }
        
        if (!show) {
            closeInputMethod();
        }
    }
    
    /**
     * 关闭系统软键盘
     */
    private void closeInputMethod() {
        InputMethodManager manager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }
    
    /**
     * 启用一键清除文本按钮
     */
    public void setClearButtonEnable() {
        if (mClearButtonEnabled) {
            return;
        }
        
        mClearButtonEnabled = true;
        invalidateForClearButton();
    }
    
    /**
     * 关闭一键清除文本按钮
     */
    public void setClearButtonDisable() {
        if (!mClearButtonEnabled) {
            return;
        }
        
        mClearButtonEnabled = false;
        invalidate();
    }
    
    /**
     * 获取清除文本按钮是否可以
     *
     * @return true:一键清除文本可以
     */
    public boolean isClearButtonEnabled() {
        return mClearButtonEnabled;
    }
    
    /**
     * 设置清除文本按钮图标
     */
    public void setClearButtonIcon(Drawable clearButton) {
        mClearButton = clearButton;
        invalidateForClearButton();
    }
    
    /**
     * 设置清除按钮大小
     */
    public void setClearButtonSize(int clearButtonSize) {
        mClearButtonWidth = clearButtonSize;
        mClearButtonHeight = clearButtonSize;
        needMeasureClearButtonSizeAgain = true;
        invalidateForClearButton();
    }
    
    /**
     * 设置清除文本按钮宽度
     *
     * @param clearButtonWidth 指定大小或使用{@link #WRAP_CONTENT}、{@link #MATCH_PARENT}
     */
    public void setClearButtonWidth(int clearButtonWidth) {
        mClearButtonWidth = clearButtonWidth;
        needMeasureClearButtonSizeAgain = true;
        invalidateForClearButton();
    }
    
    /**
     * 设置清除文本按钮高度
     *
     * @param clearButtonHeight 指定大小或使用{@link #WRAP_CONTENT}、{@link #MATCH_PARENT}
     */
    public void setClearButtonHeight(int clearButtonHeight) {
        mClearButtonHeight = clearButtonHeight;
        needMeasureClearButtonSizeAgain = true;
        invalidateForClearButton();
    }
    
    /**
     * 设置清除文本按钮左边内边距，当清除文本按钮是固定大小时，该值并不会减小按钮的宽度
     */
    public void setClearButtonPaddingLeft(int clearButtonPaddingLeft) {
        mClearButtonPaddingLeft = clearButtonPaddingLeft;
        invalidateForClearButton();
    }
    
    /**
     * 设置清除文本按钮右边内边距，当清除文本按钮是固定大小时，该值并不会减小按钮的宽度
     */
    public void setClearButtonPaddingRight(int clearButtonPaddingRight) {
        mClearButtonPaddingRight = clearButtonPaddingRight;
        invalidateForClearButton();
    }
    
    /**
     * 设置清除文本按钮左边外边距
     */
    public void setClearButtonMarginLeft(int clearButtonMarginLeft) {
        mClearButtonMarginLeft = clearButtonMarginLeft;
        invalidateForClearButton();
    }
    
    /**
     * 设置清除文本按钮右边外边距
     */
    public void setClearButtonMarginRight(int clearButtonMarginRight) {
        mClearButtonMarginRight = clearButtonMarginRight;
        invalidateForClearButton();
    }
    
    /**
     * 设置清除按钮点击事件
     */
    public void setClearButtonClickListener(OnClickListener clearButtonClickListener) {
        mClearButtonClickListener = clearButtonClickListener;
        
    }
    
    /**
     * 判断文本框中的内容是否是用户主动指定的。如果文本框内容为空，则此值始终返回{@code false}
     */
    public boolean isUserEnterContent() {
        return mUserEnterContent > 0;
    }
    
    /**
     * 通过此方法可重置是否是用户输入的内容，也可标记通过setText设置的相关内容是用户设置
     */
    public void setUserEnterContent(boolean userEnterContent) {
        if (userEnterContent) {
            mUserEnterContent = Integer.MAX_VALUE / 10;
        } else {
            mUserEnterContent = 0;
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
     * 设置文本是否自动按照宽度换行，而不是按照单词和宽度换行，解决原生汉字、英文、数字混合文本在中文环境下换行位置不尽如人意的问题。
     * 此值仅在非编辑模式下生效
     */
    public void setAutoWrapByWidth(boolean autoWrapByWidth) {
        if (autoWrapByWidth == mAutoWrapByWidth) {
            return;
        }
        mAutoWrapByWidth = autoWrapByWidth;
        super.setText(mOriginalText);
    }
    
    /**
     * 文本是否自动按照宽度换行，而不是按照单词和宽度换行
     */
    public boolean isAutoWrapByWidth() {
        return mAutoWrapByWidth && !isEnabled();
    }
    
    /**
     * 设置是否获取焦点时才展示清除按钮，默认false
     */
    public void setOnFocusShowClearButtonEnable(boolean onFocusShowClearButtonEnable) {
        mOnFocusShowClearButtonEnable = onFocusShowClearButtonEnable;
        invalidate();
    }
    
    /**
     * 是否获取焦点时才展示清除按钮
     */
    public boolean isOnFocusShowClearButtonEnable() {
        return mOnFocusShowClearButtonEnable;
    }
    
    /**
     * 当输入模式为{@linkplain EditorInfo#TYPE_NUMBER_FLAG_DECIMAL NumberDecimal}时，设置限制输入的小数位数
     */
    public void setDecimalLimit(int decimalLimit) {
        if (decimalLimit != mDecimalLimit && decimalLimit >= 0) {
            mDecimalLimit = decimalLimit;
            mNumberLimitTextWatcher.setDecimalLimit(mDecimalLimit);
        }
    }
    
    /**
     * 获取当输入模式为{@linkplain EditorInfo#TYPE_NUMBER_FLAG_DECIMAL NumberDecimal}时，限制输入的小数位数
     */
    public final int getDecimalLimit() {
        return mDecimalLimit;
    }
    
    /**
     * 当输入模式为{@linkplain EditorInfo#TYPE_CLASS_NUMBER NUMBER}或
     * {@linkplain EditorInfo#TYPE_NUMBER_FLAG_DECIMAL NumberDecimal}时，设置限制输入的整数位数
     */
    public void setIntegerLimit(int integerLimit) {
        if (integerLimit != mIntegerLimit && integerLimit >= 1) {
            mIntegerLimit = integerLimit;
            mNumberLimitTextWatcher.setIntegerLimit(mIntegerLimit);
        }
    }
    
    /**
     * 获取当输入模式为{@linkplain EditorInfo#TYPE_CLASS_NUMBER Number}或
     * {@linkplain EditorInfo#TYPE_NUMBER_FLAG_DECIMAL NumberDecimal}时，限制输入的整数位数
     */
    public final int getIntegerLimit() {
        return mIntegerLimit;
    }
    
    /**
     * 当输入模式为{@linkplain EditorInfo#TYPE_CLASS_NUMBER Number}或
     * {@linkplain EditorInfo#TYPE_NUMBER_FLAG_DECIMAL NumberDecimal}时,设置是否自动去除整数位无效0值
     */
    public void setAutoRemoveInValidZero(boolean autoRemoveInValidZero) {
        if (autoRemoveInValidZero != mAutoRemoveInValidZero) {
            mAutoRemoveInValidZero = autoRemoveInValidZero;
            mNumberLimitTextWatcher.setAutoRemoveInValidZero(mAutoRemoveInValidZero);
        }
    }
    
    /**
     * 获取当输入模式为{@linkplain EditorInfo#TYPE_CLASS_NUMBER Number}或
     * {@linkplain EditorInfo#TYPE_NUMBER_FLAG_DECIMAL NumberDecimal}时,是否自动去除整数位无效0值
     */
    public boolean isAutoRemoveInValidZero() {
        return mAutoRemoveInValidZero;
    }
    
    /**
     * 设置当输入模式为{@linkplain EditorInfo#TYPE_CLASS_NUMBER Number}或
     * {@linkplain EditorInfo#TYPE_NUMBER_FLAG_DECIMAL NumberDecimal},
     * 通过{@link #setText(CharSequence)}相关方法设置文本时，是否启用位数限制规则
     */
    public void setSetTextUseNumberLimit(boolean setTextUseNumberLimit) {
        mSetTextUseNumberLimit = setTextUseNumberLimit;
    }
    
    /**
     * 当输入模式为{@linkplain EditorInfo#TYPE_CLASS_NUMBER Number}或
     * {@linkplain EditorInfo#TYPE_NUMBER_FLAG_DECIMAL NumberDecimal},
     * 通过{@link #setText(CharSequence)}相关方法设置文本时，是否启用位数限制规则
     */
    public boolean isSetTextUseNumberLimit() {
        return mSetTextUseNumberLimit;
    }
    
    /**
     * 当输入模式为{@linkplain EditorInfo#TYPE_CLASS_NUMBER Number}或
     * {@linkplain EditorInfo#TYPE_NUMBER_FLAG_DECIMAL NumberDecimal}时，设置数值超过限制位数回调监听
     */
    public void setNumberOverLimitListener(NumberOverLimitListener numberOverLimitListener) {
        mNumberOverLimitListener = numberOverLimitListener;
    }
    
    /**
     * 设置提示文本字体大小
     *
     * @param hintTextSize 为null时取{@link #getTextSize()}
     */
    public void setHintTextSize(Float hintTextSize) {
        mHintTextSize = hintTextSize;
        if (TextUtils.isEmpty(getText().toString().trim())) {
            if (mHintTextSize == null) {
                super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            } else {
                super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mHintTextSize);
            }
        }
    }
    
    /**
     * 重绘清除按钮数据
     */
    protected void invalidateForClearButton() {
        if (needDrawClearButton()) {
            // 设置enable为true时，不会触发onPreDraw，因此手动调用invalidate()
            post(this :: countClearButtonPosition);
        } else {
            invalidate();
        }
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
        
        if (lineCount <= 1 && gravity != mDefGravity) {
            super.setGravity(mDefGravity);
            return true;
        } else {
            return false;
        }
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
        
        int width = viewWidth - getPaddingLeft() - getPaddingRight();
        Drawable[] drawables = getCompoundDrawables();
        if (drawables[0] != null) {
            width -= super.getCompoundDrawablePadding();
        }
        
        if (drawables[2] != null) {
            width -= super.getCompoundDrawablePadding();
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
    
    private final class NumberLimitTextWatcherInner extends NumberLimitTextWatcher {
        
        private boolean mCallListener = true;
        
        public NumberLimitTextWatcherInner(@NonNull EditText editText, int decimalCount, int integerLimit, boolean autoRemoveInValidZero) {
            super(editText, decimalCount, integerLimit, autoRemoveInValidZero);
        }
        
        @Override
        public void onTextChanged(CharSequence ss, int start, int before, int count) {
            if (mCallListener) {
                super.onTextChanged(ss, start, before, count);
            }
        }
        
        @Override
        public void lengthOverLimit(EditText editText, boolean isDecimalOver, boolean isIntegerOver) {
            if (mNumberOverLimitListener != null) {
                mNumberOverLimitListener.onOver(ExtendEditText.this, isDecimalOver, isIntegerOver);
            }
        }
        
        public void setCallListener(boolean callListener) {
            mCallListener = callListener;
        }
    }
    
    private final class TextWatcherInner implements TextWatcher {
        
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
            String string = s.toString().trim();
            if (TextUtils.isEmpty(string)) {
                mUserEnterContent = 0;
            } else {
                mUserEnterContent++;
            }
            
            if (mHintTextSize != null) {
                if (TextUtils.isEmpty(string)) {
                    ExtendEditText.super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mHintTextSize);
                } else {
                    ExtendEditText.super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
                }
            }
            
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
        
        public void addTextWatcher(TextWatcher textWatcher) {
            if (mTextWatchers == null) {
                mTextWatchers = new ArrayList<>();
            }
            mTextWatchers.add(textWatcher);
        }
        
        public void addTextWatcher(int index, TextWatcher textWatcher) {
            if (mTextWatchers == null) {
                mTextWatchers = new ArrayList<>();
            }
            
            if (index >= mTextWatchers.size() - 1) {
                mTextWatchers.add(textWatcher);
            } else {
                mTextWatchers.add(Math.max(index, 0), textWatcher);
            }
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
