package com.keqiang.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * 设置项Item，左边一个ImageView，中间文本，右边一个ImageView
 *
 * @author Created by 汪高皖 on 2020/1/14 13:13
 */
public class SettingItemView extends RelativeLayout {
    
    private ImageView mImageViewLeft;
    private ImageView mImageViewRight;
    private TextView mTextView;
    
    private Drawable mLeftImage;
    private Drawable mRightImage;
    private int mLeftImageSize;
    private int mRightImageSize;
    private boolean mShowLeftImage = true;
    private boolean mShowRightImage = true;
    private String mText;
    private int mTextColor;
    private float mTextSize;
    private int mTextPaddingLeft;
    private int mTextPaddingRight;
    
    public SettingItemView(Context context) {
        this(context, null);
    }
    
    public SettingItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.SettingItemView);
            mLeftImage = t.getDrawable(R.styleable.SettingItemView_siv_left_image);
            mLeftImageSize = t.getDimensionPixelSize(R.styleable.SettingItemView_siv_left_image_size, ViewGroup.LayoutParams.WRAP_CONTENT);
            mShowLeftImage = t.getBoolean(R.styleable.SettingItemView_siv_left_image_show, true);
            mRightImage = t.getDrawable(R.styleable.SettingItemView_siv_right_image);
            mRightImageSize = t.getDimensionPixelSize(R.styleable.SettingItemView_siv_right_image_size, ViewGroup.LayoutParams.WRAP_CONTENT);
            mShowRightImage = t.getBoolean(R.styleable.SettingItemView_siv_right_image_show, true);
            mText = t.getString(R.styleable.SettingItemView_android_text);
            mTextColor = t.getColor(R.styleable.SettingItemView_android_textColor, Color.BLACK);
            mTextSize = t.getDimensionPixelSize(R.styleable.SettingItemView_android_textSize, 32);
            mTextPaddingLeft = t.getDimensionPixelSize(R.styleable.SettingItemView_siv_text_padding_left, 0);
            mTextPaddingRight = t.getDimensionPixelSize(R.styleable.SettingItemView_siv_text_padding_right, 0);
            t.recycle();
        } else {
            mLeftImageSize = ViewGroup.LayoutParams.WRAP_CONTENT;
            mRightImageSize = ViewGroup.LayoutParams.WRAP_CONTENT;
            mTextSize = 32;
        }
        
        if (!isInEditMode()) {
            mLeftImageSize = SimpleUtil.getScaledValue(mLeftImageSize);
            mRightImageSize = SimpleUtil.getScaledValue(mRightImageSize);
            mTextSize = SimpleUtil.getScaledValue((int) mTextSize);
            mTextPaddingLeft = SimpleUtil.getScaledValue((mTextPaddingLeft));
            mTextPaddingRight = SimpleUtil.getScaledValue(mTextPaddingRight);
        }
        
        mImageViewLeft = new ImageView(context);
        mImageViewLeft.setId(getResources().getInteger(R.integer.setting_item_view_left_image_view_id));
        mImageViewLeft.setImageDrawable(mLeftImage);
        LayoutParams params = new LayoutParams(mLeftImageSize, mLeftImageSize);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        mImageViewLeft.setLayoutParams(params);
        addView(mImageViewLeft);
        if (!mShowLeftImage) {
            mImageViewLeft.setVisibility(GONE);
        }
        
        mImageViewRight = new ImageView(context);
        mImageViewRight.setId(getResources().getInteger(R.integer.setting_item_view_right_image_view_id));
        mImageViewRight.setImageDrawable(mRightImage);
        params = new LayoutParams(mRightImageSize, mRightImageSize);
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        mImageViewRight.setLayoutParams(params);
        addView(mImageViewRight);
        if (!mShowRightImage) {
            mImageViewRight.setVisibility(GONE);
        }
        
        mTextView = new TextView(context);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        mTextView.setTextColor(mTextColor);
        mTextView.setText(mText);
        mTextView.setPadding(mTextPaddingLeft, 0, mTextPaddingRight, 0);
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.END_OF, mImageViewLeft.getId());
        params.addRule(RelativeLayout.START_OF, mImageViewRight.getId());
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        mTextView.setLayoutParams(params);
        addView(mTextView);
    }
    
    public void setLeftImage(Drawable leftImage) {
        mLeftImage = leftImage;
        mImageViewLeft.setImageDrawable(mLeftImage);
    }
    
    public void setRightImage(Drawable rightImage) {
        mRightImage = rightImage;
        mImageViewRight.setImageDrawable(mRightImage);
    }
    
    @SuppressWarnings("SuspiciousNameCombination")
    public void setLeftImageSize(int leftImageSize) {
        mLeftImageSize = leftImageSize;
        ViewGroup.LayoutParams layoutParams = mImageViewLeft.getLayoutParams();
        layoutParams.width = mLeftImageSize;
        layoutParams.height = mLeftImageSize;
        mImageViewLeft.setLayoutParams(layoutParams);
    }
    
    @SuppressWarnings("SuspiciousNameCombination")
    public void setRightImageSize(int rightImageSize) {
        mRightImageSize = rightImageSize;
        ViewGroup.LayoutParams layoutParams = mImageViewRight.getLayoutParams();
        layoutParams.width = mRightImageSize;
        layoutParams.height = mRightImageSize;
        mImageViewRight.setLayoutParams(layoutParams);
    }
    
    public void setShowLeftImage(boolean showLeftImage) {
        mShowLeftImage = showLeftImage;
        mImageViewLeft.setVisibility(mShowLeftImage ? VISIBLE : GONE);
    }
    
    public void setShowRightImage(boolean showRightImage) {
        mShowRightImage = showRightImage;
        mImageViewRight.setVisibility(mShowRightImage ? VISIBLE : GONE);
    }
    
    public void setText(String text) {
        mText = text;
        mTextView.setText(mText);
    }
    
    public void setTextColor(int textColor) {
        mTextColor = textColor;
        mTextView.setTextColor(mTextColor);
    }
    
    public void setTextSize(float textSize) {
        mTextSize = textSize;
        mTextView.setTextSize(mTextSize);
    }
    
    public void setTextPaddingLeft(int textPaddingLeft) {
        mTextPaddingLeft = textPaddingLeft;
        mTextView.setPadding(mTextPaddingLeft, 0, mTextPaddingRight, 0);
    }
    
    public void setTextPaddingRight(int textPaddingRight) {
        mTextPaddingRight = textPaddingRight;
        mTextView.setPadding(mTextPaddingLeft, 0, mTextPaddingRight, 0);
    }
}
