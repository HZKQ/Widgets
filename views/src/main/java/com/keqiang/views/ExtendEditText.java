package com.keqiang.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;


/**
 * {@link EditText}扩展，扩展功能如下：
 * <ul>
 *     <li>支持配置一键清除按钮</li>
 *     <li>支持配置获取焦点时是否弹出软键盘，兼容低版本</li>
 *     <li>支持配置文本超出一行时，是否自动靠左排版</li>
 *     <li>支持配置文本仅根据控件宽度自动换行，优化原生汉字、英文、数字混合文本换行位置大量留白问题</li>
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
     * 记录当单行文本且文字超出控件宽度时，用户滑动文字的距离
     */
    private int scrollXLength = 0;
    
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
    
    public ExtendEditText(Context context) {
        this(context, null);
    }
    
    public ExtendEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }
    
    public ExtendEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }
    
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        PLACEHOLDER.setBounds(0, 0, 0, 0);
        mContext = context;
        mClearButton = ContextCompat.getDrawable(context, R.drawable.undo);
        mClearButtonClickRange = new Rect();
        mTextSize = super.getTextSize();
        addTextChangedListener(mTextWatcher);
        parseAttrs(context, attrs, defStyleAttr);
        super.setOnFocusChangeListener(mFocusChangeListener);
        mCompoundDrawablePadding = super.getCompoundDrawablePadding();
        Drawable[] drawables = super.getCompoundDrawables();
        if (drawables[2] == null) {
            setCompoundDrawables(drawables[0], drawables[1], null, drawables[3]);
        }
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
            mHintTextSize = null;
            mAutoGravityRtl = GRAVITY_RTL_END | GRAVITY_RTL_CENTER_HORIZONTAL;
            mAutoWrapByWidth = false;
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
                int scaledValue = SimpleUtil.getScaledValue(mHintTextSize.intValue());
                mHintTextSize = scaledValue * 1f;
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isAutoWrapByWidth()) {
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
        if (!mClearButtonEnabled || isTextEmpty() || !isEnabled()) {
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
        if (mClearButtonEnabled && !isTextEmpty() && isEnabled()) {
            drawClearButton(canvas);
            return;
        }
        
        if (mClearButtonVisibility) {
            super.setCompoundDrawablePadding(mCompoundDrawablePadding);
            mClearButtonVisibility = false;
            mClearButtonClickRange.set(0, 0, 0, 0);
        }
    }
    
    /**
     * 绘制清除文本按钮
     */
    private void drawClearButton(Canvas canvas) {
        //计算清除按钮绘制起点x,y的坐标
        float canvasYOffset = mClearButtonClickRange.height() / 2f - mClearButton.getBounds().height() / 2f;
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
                setInputType(InputType.TYPE_NULL);
                result = super.onTouchEvent(event);
                setInputType(type);
            }
            return result && isEnabled();
        }
    }
    
    /**
     * 焦点监听
     */
    private final OnFocusChangeListener mFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!softInputOnFocusShow) {
                closeInputMethod();
            }
            
            if (mOnFocusChangeListener != null) {
                mOnFocusChangeListener.onFocusChange(v, hasFocus);
            }
        }
    };
    
    @Override
    public void setText(CharSequence text, BufferType type) {
        mDefText = text;
        // 在super.setText(text, type)之上的代码会先于TextWatcher相关方法执行
        super.setText(text, type);
        // 在super.setText(text, type)之下的代码会后于TextWatcher相关方法执行
        mUserEnterContent--;
        if (mUserEnterContent < 0) {
            mUserEnterContent = 0;
        }
    }
    
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        String newText = text.toString();
        if (isEnabled()) {
            mDefText = newText;
            autoGravityRtl(false);
            return;
        }
        
        if (!newText.equals(mAutoWrapText)) {
            reDrawText(this.getWidth());
        } else {
            autoGravityRtl(true);
        }
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mAutoWrapByWidth) {
            super.setText(mDefText);
            if (!TextUtils.isEmpty(mDefText)) {
                setSelection(mDefText.length());
            }
        } else if (mClearButtonEnabled && !isTextEmpty() && enabled) {
            // 设置enable为true时，不会触发onPreDraw，因此手动调用
            post(this :: countClearButtonPosition);
        }
    }
    
    @NonNull
    @Override
    public Editable getText() {
        Editable text = super.getText();
        if (text == null && mEmptyEditable == null) {
            mEmptyEditable = new SpannableStringBuilder();
        }
        return text == null ? mEmptyEditable : text;
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
    
    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        
        }
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        
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
        }
    };
    
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
        mClearButtonEnabled = true;
        invalidate();
    }
    
    /**
     * 关闭一键清除文本按钮
     */
    public void setClearButtonDisable() {
        mClearButtonEnabled = false;
        invalidate();
    }
    
    /**
     * 获取清除文本按钮是否可以
     *
     * @return true:一键清除文本可以
     */
    public boolean getClearButtonEnabled() {
        return mClearButtonEnabled;
    }
    
    /**
     * 设置清除文本按钮图标
     */
    public void setClearButtonIcon(Drawable clearButton) {
        mClearButton = clearButton;
        invalidate();
    }
    
    /**
     * 设置清除按钮大小
     */
    public void setClearButtonSize(int clearButtonSize) {
        mClearButtonWidth = clearButtonSize;
        mClearButtonHeight = clearButtonSize;
        needMeasureClearButtonSizeAgain = true;
        invalidate();
    }
    
    /**
     * 设置清除文本按钮宽度
     *
     * @param clearButtonWidth 指定大小或使用{@link #WRAP_CONTENT}、{@link #MATCH_PARENT}
     */
    public void setClearButtonWidth(int clearButtonWidth) {
        mClearButtonWidth = clearButtonWidth;
        needMeasureClearButtonSizeAgain = true;
        invalidate();
    }
    
    /**
     * 设置清除文本按钮高度
     *
     * @param clearButtonHeight 指定大小或使用{@link #WRAP_CONTENT}、{@link #MATCH_PARENT}
     */
    public void setClearButtonHeight(int clearButtonHeight) {
        mClearButtonHeight = clearButtonHeight;
        needMeasureClearButtonSizeAgain = true;
        invalidate();
    }
    
    /**
     * 设置清除文本按钮左边内边距，当清除文本按钮是固定大小时，该值并不会减小按钮的宽度
     */
    public void setClearButtonPaddingLeft(int clearButtonPaddingLeft) {
        mClearButtonPaddingLeft = clearButtonPaddingLeft;
        invalidate();
    }
    
    /**
     * 设置清除文本按钮右边内边距，当清除文本按钮是固定大小时，该值并不会减小按钮的宽度
     */
    public void setClearButtonPaddingRight(int clearButtonPaddingRight) {
        mClearButtonPaddingRight = clearButtonPaddingRight;
        invalidate();
    }
    
    /**
     * 设置清除文本按钮左边外边距
     */
    public void setClearButtonMarginLeft(int clearButtonMarginLeft) {
        mClearButtonMarginLeft = clearButtonMarginLeft;
        invalidate();
    }
    
    /**
     * 设置清除文本按钮右边外边距
     */
    public void setClearButtonMarginRight(int clearButtonMarginRight) {
        mClearButtonMarginRight = clearButtonMarginRight;
        invalidate();
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
        super.setText(mDefText);
    }
    
    /**
     * 文本是否自动按照宽度换行，而不是按照单词和宽度换行
     */
    public boolean isAutoWrapByWidth() {
        return mAutoWrapByWidth && !isEnabled();
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
    
    /**
     * 重新绘制字符
     */
    private void reDrawText(int viewWidth) {
        mAutoWrapText = null;
        if (!isAutoWrapByWidth() || TextUtils.isEmpty(mDefText)) {
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
}
