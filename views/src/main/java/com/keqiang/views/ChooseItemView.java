package com.keqiang.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * 选择内容Item，左边两个TextView，第一个表示是否必填，第二个为标题，中间文本内容TextView，右边一个ImageView
 *
 * @author Created by wanggaowan on 2021/6/30 17:41
 */
public class ChooseItemView extends ConstraintLayout {
    
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
    protected ExtendTextView mTvContent;
    protected AppCompatImageView mIvRight;
    
    protected String mHint;
    protected int mShowStyle;
    protected String mustInputText;
    // 是否可编辑，有时候虽然以编辑样式显示，但是不可编辑
    protected boolean mCouldEdit;
    
    public ChooseItemView(Context context) {
        this(context, null);
    }
    
    public ChooseItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ChooseItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ChooseItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        mTvMustInput.setId(R.id.choose_item_view_id_1);
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
        mTvTitle.setId(R.id.choose_item_view_id_2);
        mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mTvTitle.setTextColor(textColor);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToTop = LayoutParams.PARENT_ID;
        params.bottomToBottom = LayoutParams.PARENT_ID;
        params.startToEnd = mTvMustInput.getId();
        mTvTitle.setLayoutParams(params);
        addView(mTvTitle);
        
        mIvRight = new AppCompatImageView(context);
        mIvRight.setId(R.id.choose_item_view_id_4);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToTop = LayoutParams.PARENT_ID;
        params.bottomToBottom = LayoutParams.PARENT_ID;
        params.endToEnd = LayoutParams.PARENT_ID;
        mIvRight.setLayoutParams(params);
        addView(mIvRight);
        
        mTvContent = new ExtendTextView(context, attrs);
        mTvContent.setId(R.id.choose_item_view_id_3);
        mTvContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mTvContent.setTextColor(textColor);
        params = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToTop = LayoutParams.PARENT_ID;
        params.bottomToBottom = LayoutParams.PARENT_ID;
        params.startToEnd = mTvTitle.getId();
        params.endToStart = mIvRight.getId();
        mTvContent.setLayoutParams(params);
        addView(mTvContent);
        
        if (attrs != null) {
            TypedArray t = null;
            try {
                t = context.obtainStyledAttributes(attrs, R.styleable.ChooseItemView);
                mShowStyle = t.getInteger(R.styleable.ChooseItemView_civ_show_style, SHOW_STYLE_EDIT);
                mCouldEdit = t.getBoolean(R.styleable.ChooseItemView_civ_could_edit, true);
                textColor = t.getColor(R.styleable.ChooseItemView_android_textColor, Color.BLACK);
                textSize = t.getDimensionPixelSize(R.styleable.ChooseItemView_android_textSize, 32);
                
                mustInputText = t.getString(R.styleable.ChooseItemView_civ_must_input_text);
                mTvMustInput.setText(mustInputText);
                mTvMustInput.setTextColor(t.getColor(R.styleable.ChooseItemView_civ_must_input_textColor, textColor));
                mTvMustInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, t.getDimension(R.styleable.ChooseItemView_civ_must_input_textSize, textSize));
                int paddingStart = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_must_input_padding_start, 0);
                int paddingEnd = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_must_input_padding_end, 0);
                mTvMustInput.setPadding(paddingStart, 0, paddingEnd, 0);
                
                mTvTitle.setText(t.getString(R.styleable.ChooseItemView_civ_title_text));
                mTvTitle.setTextColor(t.getColor(R.styleable.ChooseItemView_civ_title_textColor, textColor));
                mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, t.getDimension(R.styleable.ChooseItemView_civ_title_textSize, textSize));
                int marginStart = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_title_margin_start, 0);
                int marginEnd = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_title_margin_end, 0);
                setViewMargin(mTvTitle, 0, marginEnd);
                if (marginStart != 0) {
                    setViewSize(mTvMustInput, marginStart, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                
                int width = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_title_width, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (width != ViewGroup.LayoutParams.WRAP_CONTENT) {
                    setViewSize(mTvTitle, width, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                
                mTvContent.setTextColor(textColor);
                mTvContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                mHint = t.getString(R.styleable.ChooseItemView_android_hint);
                int height = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_content_height, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                    setViewSize(mTvContent, 0, height);
                }
                
                int minHeight = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_content_min_height, 0);
                if (minHeight != 0) {
                    mTvContent.setMinHeight(minHeight);
                }
                
                int paddingTop = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_content_padding_top, 0);
                int paddingBottom = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_content_padding_bottom, 0);
                mTvContent.setPadding(0, paddingTop, 0, paddingBottom);
                marginStart = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_content_margin_start, 0);
                marginEnd = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_content_margin_end, 0);
                setViewMargin(mTvContent, marginStart, marginEnd);
                int goneMarginStart = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_content_gone_margin_start, marginStart);
                int goneMarginEnd = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_content_gone_margin_end, marginEnd);
                setViewGoneMargin(mTvContent, goneMarginStart, goneMarginEnd);
                
                boolean autoWrapByWidth = t.getBoolean(R.styleable.ChooseItemView_civ_content_auto_wrap_by_width, false);
                mTvContent.setAutoWrapByWidth(autoWrapByWidth);
                int autoGravityRtl = t.getInt(R.styleable.ChooseItemView_civ_content_auto_gravity_rtl, ExtendEditText.GRAVITY_RTL_END | ExtendEditText.GRAVITY_RTL_CENTER_HORIZONTAL);
                mTvContent.setAutoGravityRtl(autoGravityRtl);
                
                mIvRight.setImageDrawable(t.getDrawable(R.styleable.ChooseItemView_civ_right_image));
                int imageSize = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_right_image_size, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (imageSize != ViewGroup.LayoutParams.WRAP_CONTENT) {
                    setViewSize(mIvRight, imageSize, imageSize);
                }
                int padding = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_right_image_padding, 0);
                mIvRight.setPadding(padding, padding, padding, padding);
                marginStart = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_right_image_margin_start, 0);
                marginEnd = t.getDimensionPixelSize(R.styleable.ChooseItemView_civ_right_image_margin_end, 0);
                setViewMargin(mIvRight, marginStart, marginEnd);
                
                parseCustomAttrs(t);
            } finally {
                if (t != null) {
                    t.recycle();
                }
            }
        }
        
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
            mTvContent.setHint(null);
            mIvRight.setVisibility(GONE);
            setEnabled(false);
        } else {
            mTvMustInput.setText(mustInputText);
            mTvContent.setHint(mHint);
            mIvRight.setVisibility(VISIBLE);
            setEnabled(couldEdit);
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
            mTvContent.setHint(mHint);
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
    public ExtendTextView getTvContent() {
        return mTvContent;
    }
    
    /**
     * 返回右侧图标控件
     */
    public AppCompatImageView getIvRight() {
        return mIvRight;
    }
}
