package com.keqiang.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * 编辑内容Item，左边两个TextView，第一个表示是否必填，第二个为标题，中间编辑内容EditText,右边一个TextView，用于部分场景下对编辑内容的说明，比如单位
 *
 * @author Created by wanggaowan on 2021/6/30 17:41
 */
public class EditItemView extends ConstraintLayout {
    
    /**
     * 以只读模式显示
     */
    public static final int SHOW_STYLE_READ = 0;
    
    /**
     * 以编辑模式显示
     */
    public static final int SHOW_STYLE_EDIT = 1;
    
    @IntDef(value = {SHOW_STYLE_READ, SHOW_STYLE_EDIT}, open = true)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    private  @interface ShowStyle {}
    
    protected ExtendTextView mTvMustInput;
    protected ExtendTextView mTvTitle;
    protected ExtendEditText mEtContent;
    protected ExtendTextView mTvUnit;
    
    protected String mHint;
    protected int mShowStyle;
    protected String mustInputText;
    // 是否可编辑，有时候虽然以编辑样式显示，但是不可编辑
    protected boolean mCouldEdit;
    
    public EditItemView(Context context) {
        this(context, null);
    }
    
    public EditItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public EditItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public EditItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }
    
    protected void init(Context context, AttributeSet attrs) {
        mShowStyle = SHOW_STYLE_EDIT;
        mCouldEdit = true;
        float textSize = 32;
        int textColor = Color.BLACK;
        mHint = null;
        
        mTvMustInput = new ExtendTextView(context);
        mTvMustInput.setId(R.id.edit_item_view_id_1);
        mTvMustInput.setGravity(Gravity.END);
        mTvMustInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mTvMustInput.setTextColor(textColor);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToTop = LayoutParams.PARENT_ID;
        params.bottomToBottom = LayoutParams.PARENT_ID;
        params.startToStart = LayoutParams.PARENT_ID;
        mTvMustInput.setLayoutParams(params);
        addView(mTvMustInput);
        
        mTvTitle = new ExtendTextView(context);
        mTvTitle.setId(R.id.edit_item_view_id_2);
        mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mTvTitle.setTextColor(textColor);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToTop = LayoutParams.PARENT_ID;
        params.bottomToBottom = LayoutParams.PARENT_ID;
        params.startToEnd = mTvMustInput.getId();
        mTvTitle.setLayoutParams(params);
        addView(mTvTitle);
        
        mTvUnit = new ExtendTextView(context);
        mTvUnit.setId(R.id.edit_item_view_id_4);
        mTvUnit.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mTvUnit.setTextColor(textColor);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToTop = LayoutParams.PARENT_ID;
        params.bottomToBottom = LayoutParams.PARENT_ID;
        params.endToEnd = LayoutParams.PARENT_ID;
        mTvUnit.setLayoutParams(params);
        addView(mTvUnit);
        
        mEtContent = new ExtendEditText(context, attrs);
        mEtContent.setId(R.id.edit_item_view_id_3);
        mEtContent.setBackground(null);
        params = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToTop = LayoutParams.PARENT_ID;
        params.bottomToBottom = LayoutParams.PARENT_ID;
        params.startToEnd = mTvTitle.getId();
        params.endToStart = mTvUnit.getId();
        mEtContent.setLayoutParams(params);
        addView(mEtContent);
        
        int defaultClearButtonSize = 50;
        int clearButtonWidth = defaultClearButtonSize;
        int clearButtonHeight = defaultClearButtonSize;
        int defaultClearButtonPadding = 10;
        int clearButtonPaddingLeft = defaultClearButtonPadding;
        int clearButtonPaddingRight = defaultClearButtonPadding;
        int clearButtonMarginLeft = 0;
        int clearButtonMarginRight = 0;
        Float hintTextSize = null;
        
        if (attrs != null) {
            TypedArray t = null;
            try {
                t = context.obtainStyledAttributes(attrs, R.styleable.EditItemView);
                mShowStyle = t.getInteger(R.styleable.EditItemView_eiv_show_style, SHOW_STYLE_EDIT);
                mCouldEdit = t.getBoolean(R.styleable.EditItemView_eiv_could_edit, true);
                textColor = t.getColor(R.styleable.EditItemView_android_textColor, Color.BLACK);
                textSize = t.getDimensionPixelSize(R.styleable.EditItemView_android_textSize, 32);
                
                mustInputText = t.getString(R.styleable.EditItemView_eiv_must_input_text);
                mTvMustInput.setText(mustInputText);
                mTvMustInput.setTextColor(t.getColor(R.styleable.EditItemView_eiv_must_input_textColor, textColor));
                mTvMustInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, t.getDimension(R.styleable.EditItemView_eiv_must_input_textSize, textSize));
                int paddingStart = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_must_input_padding_start, 0);
                int paddingEnd = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_must_input_padding_end, 0);
                mTvMustInput.setPadding(paddingStart, 0, paddingEnd, 0);
                
                mTvTitle.setText(t.getString(R.styleable.EditItemView_eiv_title_text));
                mTvTitle.setTextColor(t.getColor(R.styleable.EditItemView_eiv_title_textColor, textColor));
                mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, t.getDimension(R.styleable.EditItemView_eiv_title_textSize, textSize));
                int marginStart = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_title_margin_start, 0);
                int marginEnd = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_title_margin_end, 0);
                setViewMargin(mTvTitle, 0, marginEnd);
                if (marginStart != 0) {
                    setViewSize(mTvMustInput, marginStart, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                
                int width = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_title_width, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (width != ViewGroup.LayoutParams.WRAP_CONTENT) {
                    setViewSize(mTvTitle, width, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                
                mTvUnit.setVisibility(t.getBoolean(R.styleable.EditItemView_eiv_unit_show, false) ? VISIBLE : GONE);
                mTvUnit.setText(t.getString(R.styleable.EditItemView_eiv_unit_text));
                int paddingHorizontal = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_unit_padding_horizontal, 0);
                int paddingVertical = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_unit_padding_vertical, 0);
                mTvUnit.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
                mTvUnit.setBackground(t.getDrawable(R.styleable.EditItemView_eiv_unit_background));
                mTvUnit.setTextColor(t.getColor(R.styleable.EditItemView_eiv_unit_textColor, textColor));
                mTvUnit.setTextSize(TypedValue.COMPLEX_UNIT_PX, t.getDimension(R.styleable.EditItemView_eiv_unit_textSize, textSize));
                marginStart = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_unit_margin_start, 0);
                marginEnd = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_unit_margin_end, 0);
                setViewMargin(mTvUnit, marginStart, marginEnd);
                
                mEtContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                mEtContent.setTextColor(textColor);
                mHint = t.getString(R.styleable.EditItemView_android_hint);
                int height = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_content_height, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                    setViewSize(mEtContent, 0, height);
                }
                
                int minHeight = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_content_min_height, 0);
                if (minHeight != 0) {
                    mEtContent.setMinHeight(minHeight);
                }
                
                int paddingTop = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_content_padding_top, 0);
                int paddingBottom = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_content_padding_bottom, 0);
                mEtContent.setPadding(0, paddingTop, 0, paddingBottom);
                marginStart = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_content_margin_start, 0);
                marginEnd = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_content_margin_end, 0);
                setViewMargin(mEtContent, marginStart, marginEnd);
                int goneMarginStart = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_content_gone_margin_start, marginStart);
                int goneMarginEnd = t.getDimensionPixelSize(R.styleable.EditItemView_eiv_content_gone_margin_end, marginEnd);
                setViewGoneMargin(mEtContent, goneMarginStart, goneMarginEnd);
                
                boolean clearButtonEnabled = t.getBoolean(R.styleable.EditItemView_eiv_content_clearButtonEnabled, true);
                if (clearButtonEnabled) {
                    mEtContent.setClearButtonEnable();
                } else {
                    mEtContent.setClearButtonDisable();
                }
                
                Drawable drawable = t.getDrawable(R.styleable.EditItemView_eiv_content_clearButtonIcon);
                if (drawable != null) {
                    mEtContent.setClearButtonIcon(drawable);
                }
                
                int size = t.getDimensionPixelOffset(R.styleable.EditItemView_eiv_content_clearButtonSize, defaultClearButtonSize);
                clearButtonWidth = size;
                clearButtonHeight = size;
                
                if (t.hasValue(R.styleable.EditItemView_eiv_content_clearButtonWidth)) {
                    clearButtonWidth = t.getDimensionPixelOffset(R.styleable.EditItemView_eiv_content_clearButtonWidth, defaultClearButtonSize);
                }
                
                if (t.hasValue(R.styleable.EditItemView_eiv_content_clearButtonHeight)) {
                    clearButtonHeight = t.getDimensionPixelOffset(R.styleable.EditItemView_eiv_content_clearButtonHeight, defaultClearButtonSize);
                }
                
                clearButtonPaddingLeft = t.getDimensionPixelOffset(R.styleable.EditItemView_eiv_content_clearButtonPaddingStart, defaultClearButtonPadding);
                clearButtonPaddingRight = t.getDimensionPixelOffset(R.styleable.EditItemView_eiv_content_clearButtonPaddingEnd, defaultClearButtonPadding);
                
                clearButtonMarginLeft = t.getDimensionPixelOffset(R.styleable.EditItemView_eiv_content_clearButtonMarginStart, 0);
                clearButtonMarginRight = t.getDimensionPixelOffset(R.styleable.EditItemView_eiv_content_clearButtonMarginEnd, 0);
                
                if (t.hasValue(R.styleable.EditItemView_eiv_content_hintTextSize)) {
                    hintTextSize = t.getDimension(R.styleable.EditItemView_eiv_content_hintTextSize, textSize);
                }
                
                boolean autoWrapByWidth = t.getBoolean(R.styleable.EditItemView_eiv_content_auto_wrap_by_width, false);
                mEtContent.setAutoWrapByWidth(autoWrapByWidth);
                int autoGravityRtl = t.getInt(R.styleable.EditItemView_eiv_content_auto_gravity_rtl, ExtendEditText.GRAVITY_RTL_END | ExtendEditText.GRAVITY_RTL_CENTER_HORIZONTAL);
                mEtContent.setAutoGravityRtl(autoGravityRtl);
                boolean onFocusShowClearButtonEnable = t.getBoolean(R.styleable.EditItemView_eiv_content_onFocusShowClearButtonEnable, false);
                mEtContent.setOnFocusShowClearButtonEnable(onFocusShowClearButtonEnable);
                
                int decimalLimit = t.getInt(R.styleable.EditItemView_eiv_content_decimalLimit, Integer.MAX_VALUE);
                mEtContent.setDecimalLimit(decimalLimit);
                int integerLimit = t.getInt(R.styleable.EditItemView_eiv_content_integerLimit, Integer.MAX_VALUE);
                mEtContent.setIntegerLimit(integerLimit);
                boolean autoRemoveInValidZero = t.getBoolean(R.styleable.EditItemView_eiv_content_autoRemoveInValidZero, true);
                mEtContent.setAutoRemoveInValidZero(autoRemoveInValidZero);
                boolean setTextUseNumberLimit = t.getBoolean(R.styleable.EditItemView_eiv_content_setTextUseNumberLimit, false);
                mEtContent.setSetTextUseNumberLimit(setTextUseNumberLimit);
                
                parseCustomAttrs(t);
            } finally {
                if (t != null) {
                    t.recycle();
                }
            }
        }
        
        if (!isInEditMode()) {
            clearButtonPaddingLeft = SimpleUtil.getScaledValue(clearButtonPaddingLeft);
            clearButtonPaddingRight = SimpleUtil.getScaledValue(clearButtonPaddingRight);
            clearButtonMarginLeft = SimpleUtil.getScaledValue(clearButtonMarginLeft);
            clearButtonMarginRight = SimpleUtil.getScaledValue(clearButtonMarginRight);
            if (hintTextSize != null) {
                hintTextSize = SimpleUtil.getScaledValue(hintTextSize, true);
            }
            
            if (clearButtonWidth != ViewGroup.LayoutParams.WRAP_CONTENT && clearButtonWidth != ViewGroup.LayoutParams.MATCH_PARENT) {
                clearButtonWidth = SimpleUtil.getScaledValue(clearButtonWidth);
            }
            
            if (clearButtonHeight != ViewGroup.LayoutParams.WRAP_CONTENT && clearButtonHeight != ViewGroup.LayoutParams.MATCH_PARENT) {
                clearButtonHeight = SimpleUtil.getScaledValue(clearButtonHeight);
            }
        }
        
        mEtContent.setClearButtonPaddingLeft(clearButtonPaddingLeft);
        mEtContent.setClearButtonPaddingRight(clearButtonPaddingRight);
        mEtContent.setClearButtonMarginLeft(clearButtonMarginLeft);
        mEtContent.setClearButtonMarginRight(clearButtonMarginRight);
        mEtContent.setClearButtonWidth(clearButtonWidth);
        mEtContent.setClearButtonHeight(clearButtonHeight);
        mEtContent.setHintTextSize(hintTextSize);
        
        setShowStyle(mShowStyle, mCouldEdit);
    }
    
    /**
     * 解析自定义属性，用于子类继承使用
     */
    protected void parseCustomAttrs(@NonNull TypedArray typedArray) {
    
    }
    
    protected void setViewMargin(View view, int marginStart, int marginEnd) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.setMarginStart(marginStart);
        layoutParams.setMarginEnd(marginEnd);
        view.setLayoutParams(layoutParams);
    }
    
    protected void setViewGoneMargin(View view, int goneMarginStart, int goneMarginEnd) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.goneStartMargin = goneMarginStart;
        layoutParams.goneEndMargin = goneMarginEnd;
        view.setLayoutParams(layoutParams);
    }
    
    protected void setViewSize(View view, int width, int height) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }
    
    /**
     * 获取当前展示样式
     */
    public int getShowStyle() {
        return mShowStyle;
    }
    
    /**
     * 是否可编辑
     */
    public boolean isCouldEdit() {
        return mCouldEdit && mShowStyle == SHOW_STYLE_EDIT;
    }
    
    /**
     * 设置当前展示样式
     */
    public void setShowStyle(@ShowStyle int showStyle) {
        setShowStyle(showStyle, showStyle == SHOW_STYLE_EDIT);
    }
    
    /**
     * 设置当前展示样式
     *
     * @param couldEdit 是否可编辑，当showStyle == {@link #SHOW_STYLE_EDIT}时，
     *                  可通过couldEdit设置为仅显示编辑样式但是不可编辑
     */
    public void setShowStyle(@ShowStyle int showStyle, boolean couldEdit) {
        mShowStyle = showStyle;
        mCouldEdit = couldEdit;
        
        if (isSetCustomShowStyle()) {
            return;
        }
        
        if (mShowStyle == SHOW_STYLE_READ) {
            mTvMustInput.setText(null);
            mEtContent.setHint(null);
            mEtContent.setEnabled(false);
        } else {
            mTvMustInput.setText(mustInputText);
            mEtContent.setHint(mHint);
            mEtContent.setEnabled(couldEdit);
        }
    }
    
    /**
     * 是否处理自定义显示风格,此时{@link #mShowStyle}、{@link #mCouldEdit}已赋值，可直接使用
     *
     * @return {@code true}:处理自定义逻辑，则该类风格显示逻辑不调用
     */
    protected boolean isSetCustomShowStyle() {
        return false;
    }
    
    /**
     * 设置必填标识文本
     */
    public void setMustInputText(String mustInputText) {
        this.mustInputText = mustInputText;
        if (mShowStyle == SHOW_STYLE_EDIT) {
            mTvMustInput.setText(mustInputText);
        }
    }
    
    /**
     * 设置选择文本提示内容
     */
    public void setHint(String hint) {
        mHint = hint;
        if (mShowStyle == SHOW_STYLE_EDIT) {
            mEtContent.setHint(mHint);
        }
    }
    
    /**
     * 返回必填项控件
     */
    public ExtendTextView getTvMustInput() {
        return mTvMustInput;
    }
    
    /**
     * 返回标题控件
     */
    public ExtendTextView getTvTitle() {
        return mTvTitle;
    }
    
    /**
     * 返回内容控件
     */
    public ExtendEditText getTvContent() {
        return mEtContent;
    }
    
    /**
     * 返回单位控件
     */
    public ExtendTextView getTvUnit() {
        return mTvUnit;
    }
}
