package com.keqiang.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import me.zhouzhuo810.magpiex.utils.ColorUtil;

/**
 * 下拉查询条件的文本控件，（文本+箭头）
 *
 * @author zhouzhuo810
 * @date 7/16/21 5:11 PM
 */
public class DropItemView extends ConstraintLayout {
    
    /**
     * 收起状态
     */
    public static final int STATUS_COLLAPSE = 0;
    /**
     * 展开状态
     */
    public static final int STATUS_EXPAND = 1;
    
    private static final int DEFAULT_TEXT_SIZE_PX = 41;
    private static final int DEFAULT_ICON_SIZE_PX = 41;
    private static final int DEFAULT_SPACING_PX = 20;
    private static final int DEFAULT_TEXT_COLOR_EXPAND = 0xff3A559B;
    private static final int DEFAULT_TEXT_COLOR_COLLAPSE = 0xff333333;
    private static final int DEFAULT_ICON_COLOR_EXPAND = 0xff3A559B;
    private static final int DEFAULT_ICON_COLOR_COLLAPSE = 0x66000000;
    
    
    private int mTextSize;
    private int mIconSize;
    private int mTextColorExpand;
    private int mTextColorCollapse;
    private int mIconColorExpand;
    private int mIconColorCollapse;
    private boolean mExpand;
    private int mAngleExpand;
    private int mAngleCollapse;
    private int mSpacing;
    private int mMaxLines;
    
    private TextView mTvTitle;
    private AppCompatImageView mIvArrow;
    
    private OnDropStatusChangeListener mOnDropStatusChangeListener;
    
    public DropItemView(@NonNull Context context) {
        this(context, null);
    }
    
    public DropItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public DropItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }
    
    public DropItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }
    
    
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        String title = null;
        Drawable icon = null;
        int gravity = 0;
        if (attrs != null) {
            TypedArray typedArray = null;
            try {
                typedArray = context.obtainStyledAttributes(attrs, R.styleable.DropItemView, defStyleAttr, 0);
                mExpand = typedArray.getBoolean(R.styleable.DropItemView_div_is_expand, false);
                mAngleCollapse = typedArray.getInt(R.styleable.DropItemView_div_icon_angle_collapse, 0);
                mAngleExpand = typedArray.getInt(R.styleable.DropItemView_div_icon_angle_expand, 180);
                mMaxLines = typedArray.getInt(R.styleable.DropItemView_div_text_max_lines, 2);
                title = typedArray.getString(R.styleable.DropItemView_div_text);
                icon = typedArray.getDrawable(R.styleable.DropItemView_div_icon);
                mIconSize = typedArray.getDimensionPixelSize(R.styleable.DropItemView_div_icon_size, DEFAULT_ICON_SIZE_PX);
                mTextSize = typedArray.getDimensionPixelSize(R.styleable.DropItemView_div_text_size, DEFAULT_TEXT_SIZE_PX);
                mSpacing = typedArray.getDimensionPixelSize(R.styleable.DropItemView_div_text_icon_spacing, DEFAULT_SPACING_PX);
                mTextColorExpand = typedArray.getColor(R.styleable.DropItemView_div_text_color_expand, DEFAULT_TEXT_COLOR_EXPAND);
                mTextColorCollapse = typedArray.getColor(R.styleable.DropItemView_div_text_color_collapse, DEFAULT_TEXT_COLOR_COLLAPSE);
                mIconColorExpand = typedArray.getColor(R.styleable.DropItemView_div_icon_color_expand, DEFAULT_ICON_COLOR_EXPAND);
                mIconColorCollapse = typedArray.getColor(R.styleable.DropItemView_div_icon_color_collapse, DEFAULT_ICON_COLOR_COLLAPSE);
                gravity = typedArray.getInt(R.styleable.DropItemView_div_text_gravity, 0);
            } finally {
                if (typedArray != null) {
                    typedArray.recycle();
                }
            }
        }
        
        mTvTitle = new TextView(context);
        mTvTitle.setText(title);
        mTvTitle.setEllipsize(TextUtils.TruncateAt.END);
        mTvTitle.setMaxLines(mMaxLines);
        mTvTitle.setId(R.id.drop_item_view_id_1);
        switch (gravity) {
            case 1:
                mTvTitle.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                break;
            case 2:
                mTvTitle.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                break;
            default:
                mTvTitle.setGravity(Gravity.CENTER);
                break;
        }
        
        mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.constrainedWidth = true;
        params.horizontalBias = 0.5f;
        params.horizontalChainStyle = LayoutParams.CHAIN_PACKED;
        params.topToTop = LayoutParams.PARENT_ID;
        params.bottomToBottom = LayoutParams.PARENT_ID;
        params.startToStart = LayoutParams.PARENT_ID;
        params.endToStart = R.id.drop_item_view_id_2;
        
        mIvArrow = new AppCompatImageView(context);
        mIvArrow.setId(R.id.drop_item_view_id_2);
        mIvArrow.setImageDrawable(icon);
        LayoutParams paramsIcon = new LayoutParams(mIconSize, mIconSize);
        paramsIcon.topToTop = LayoutParams.PARENT_ID;
        paramsIcon.bottomToBottom = LayoutParams.PARENT_ID;
        paramsIcon.endToEnd = LayoutParams.PARENT_ID;
        paramsIcon.startToEnd = R.id.drop_item_view_id_1;
        paramsIcon.leftMargin = mSpacing;
        
        addView(mTvTitle, params);
        addView(mIvArrow, paramsIcon);
        
        if (mExpand) {
            expand();
        } else {
            collapse();
        }
        
        setOnClickListener(v -> {
            if (isExpand()) {
                collapse();
            } else {
                expand();
            }
        });
    }
    
    /**
     * 设置下拉状态变化监听
     *
     * @param onDropStatusChangeListener OnDropStatusChangeListener
     */
    public void setOnDropStatusChangeListener(OnDropStatusChangeListener onDropStatusChangeListener) {
        mOnDropStatusChangeListener = onDropStatusChangeListener;
    }
    
    /**
     * 展开
     */
    public DropItemView expand() {
        return expand(true);
    }
    
    /**
     * 展开
     *
     * @param notify 是否触发监听
     * @return DropItemView
     */
    public DropItemView expand(boolean notify) {
        mExpand = true;
        if (mIvArrow != null) {
            mIvArrow.setRotation(mAngleExpand);
            ColorUtil.setIconColor(mIvArrow, mIconColorExpand);
        }
        if (mTvTitle != null) {
            mTvTitle.setTextColor(mTextColorExpand);
        }
        if (mOnDropStatusChangeListener != null && notify) {
            mOnDropStatusChangeListener.onStatusChange(this, true);
        }
        return this;
    }
    
    /**
     * 收起
     */
    public DropItemView collapse() {
        return collapse(true);
    }
    
    /**
     * 收起
     *
     * @param notify 是否触发监听
     * @return DropItemView
     */
    public DropItemView collapse(boolean notify) {
        mExpand = false;
        if (mIvArrow != null) {
            mIvArrow.setRotation(mAngleCollapse);
            ColorUtil.setIconColor(mIvArrow, mIconColorCollapse);
        }
        if (mTvTitle != null) {
            mTvTitle.setTextColor(mTextColorCollapse);
        }
        if (mOnDropStatusChangeListener != null && notify) {
            mOnDropStatusChangeListener.onStatusChange(this, false);
        }
        return this;
    }
    
    /**
     * 当前状态是否展开
     *
     * @return 是否
     */
    public boolean isExpand() {
        return mExpand;
    }
    
    /**
     * 设置文本
     *
     * @param text 文本
     * @return DropItemView
     */
    public DropItemView text(CharSequence text) {
        if (mTvTitle != null) {
            mTvTitle.setText(text);
        }
        return this;
    }
    
    /**
     * 图标
     *
     * @param res 图标资源
     * @return DropItemView
     */
    public DropItemView icon(@DrawableRes int res) {
        if (mIvArrow != null) {
            mIvArrow.setImageResource(res);
            if (mExpand) {
                mIvArrow.setRotation(mAngleExpand);
                ColorUtil.setIconColor(mIvArrow, mIconColorExpand);
            } else {
                mIvArrow.setRotation(mAngleCollapse);
                ColorUtil.setIconColor(mIvArrow, mIconColorCollapse);
            }
        }
        return this;
    }
    
    
    public TextView getTvTitle() {
        return mTvTitle;
    }
    
    public AppCompatImageView getIvArrow() {
        return mIvArrow;
    }
    
    public interface OnDropStatusChangeListener {
        /**
         * 下拉状态改变
         *
         * @param div      DropItemView
         * @param isExpand 下拉/收起
         */
        void onStatusChange(DropItemView div, boolean isExpand);
    }
}
