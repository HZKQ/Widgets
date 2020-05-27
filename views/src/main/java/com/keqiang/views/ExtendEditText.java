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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import me.zhouzhuo810.magpiex.utils.SimpleUtil;


/**
 * 增加在获取焦点时控制软键盘是否弹出。自带一键清除文本按钮，默认启用
 *
 * @author Created by 汪高皖 on 2018-05-28 14:29
 */

@SuppressWarnings("FieldCanBeLocal")
public class ExtendEditText extends AppCompatEditText {
    public static final int MATCH_PARENT = -1;
    public static final int WRAP_CONTENT = -2;
    
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
    private View.OnFocusChangeListener mOnFocusChangeListener;
    
    private View.OnClickListener mClearButtonClickListener;
    
    public ExtendEditText(Context context) {
        super(context);
        init(context, null, 0);
    }
    
    public ExtendEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
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
            setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
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
        
        if (mHintTextSize != null && isTextEmpty()) {
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mHintTextSize);
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
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
        int width;
        int height;
        if (needMeasureClearButtonSizeAgain) {
            //计算清除按钮的大小
            if (mClearButtonWidth == WRAP_CONTENT) {
                width = mClearButton.getIntrinsicWidth();
            } else if (mClearButtonWidth == MATCH_PARENT) {
                width = Math.min(originalEditTextWidth, originalEditTextHeight);
            } else {
                width = mClearButtonWidth;
            }
            
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
            height = bounds.bottom;
        }
        
        
        //计算清除按钮绘制起点x,y的坐标
        float canvasYOffset = originalEditTextHeight / 2f - height / 2f;
        float canvasXOffset = originalEditTextWidth + scrollXLength - width - getPaddingRight()
            - mClearButtonPaddingRight - mClearButtonMarginRight;
        Drawable drawable = getCompoundDrawables()[2];
        if (drawable != null) {
            canvasXOffset -= drawable.getIntrinsicWidth() + mCompoundDrawablePadding;
        }
        
        canvas.translate(canvasXOffset, canvasYOffset);
        mClearButton.draw(canvas);
        canvas.translate(-canvasXOffset, -canvasYOffset);
        
        //清除按钮可点击范围
        mClearButtonClickRange.left = (int) (canvasXOffset - mClearButtonPaddingLeft - scrollXLength);
        mClearButtonClickRange.right = (int) (canvasXOffset + width + mClearButtonPaddingRight - scrollXLength);
        mClearButtonClickRange.top = 0;
        mClearButtonClickRange.bottom = originalEditTextHeight;
        mClearButtonVisibility = true;
        setCompoundDrawablePadding(mCompoundDrawablePadding);
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
            // +mClearButton.getIntrinsicWidth() / 2 是为了防止mClearButtonPaddingLeft和mClearButtonPaddingRight都为0时，
            // 清除按钮可能和右边paddingDrawable挨在一起
            pad += (mClearButtonClickRange.right - mClearButtonClickRange.left)
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
    }
    
    @Override
    public void setOnFocusChangeListener(View.OnFocusChangeListener l) {
        mOnFocusChangeListener = l;
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isTouchClearButton(event)) {
            //触摸清除按钮时，屏蔽EditText触摸时间
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
    private View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
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
        // 在super.setText(text, type)之上的代码会先于TextWatcher相关方法执行
        super.setText(text, type);
        // 在super.setText(text, type)之下的代码会后于TextWatcher相关方法执行
        mUserEnterContent--;
        if (mUserEnterContent < 0) {
            mUserEnterContent = 0;
        }
    }
    
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        
        }
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        
        }
        
        @Override
        public void afterTextChanged(Editable s) {
            boolean textEmpty = isTextEmpty();
            if (textEmpty) {
                mUserEnterContent = 0;
            } else {
                mUserEnterContent++;
            }
            
            if (mHintTextSize != null) {
                if (textEmpty) {
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
        
        if (mHintTextSize != null && isTextEmpty()) {
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
        if (getText() == null) {
            return true;
        }
        
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
    }
    
    /**
     * 关闭一键清除文本按钮
     */
    public void setClearButtonDisable() {
        mClearButtonEnabled = false;
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
    }
    
    /**
     * 设置清除文本按钮宽度
     *
     * @param clearButtonWidth 指定大小或使用{@link #WRAP_CONTENT}、{@link #MATCH_PARENT}
     */
    public void setClearButtonWidth(int clearButtonWidth) {
        mClearButtonWidth = clearButtonWidth;
        needMeasureClearButtonSizeAgain = true;
    }
    
    /**
     * 设置清除文本按钮高度
     *
     * @param clearButtonHeight 指定大小或使用{@link #WRAP_CONTENT}、{@link #MATCH_PARENT}
     */
    public void setClearButtonHeight(int clearButtonHeight) {
        mClearButtonHeight = clearButtonHeight;
        needMeasureClearButtonSizeAgain = true;
    }
    
    /**
     * 设置清除文本按钮左边内边距，当清除文本按钮是固定大小时，该值并不会减小按钮的宽度
     */
    public void setClearButtonPaddingLeft(int clearButtonPaddingLeft) {
        mClearButtonPaddingLeft = clearButtonPaddingLeft;
    }
    
    /**
     * 设置清除文本按钮右边内边距，当清除文本按钮是固定大小时，该值并不会减小按钮的宽度
     */
    public void setClearButtonPaddingRight(int clearButtonPaddingRight) {
        mClearButtonPaddingRight = clearButtonPaddingRight;
    }
    
    /**
     * 设置清除按钮点击事件
     */
    public void setClearButtonClickListener(View.OnClickListener clearButtonClickListener) {
        mClearButtonClickListener = clearButtonClickListener;
    }
    
    /**
     * 判断当前内容是否是用户通过软键盘或其它外部输入设备输入，而非通过setText相关方法设置。如果文本框内容为空，则此值始终返回{@code false}
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
}
