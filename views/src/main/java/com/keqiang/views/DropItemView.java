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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
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
     * 图标紧挨文本靠左显示，当文本超出控件宽度时，图标紧靠右边
     */
    public static final int SHOW_STYLE_START = 0;
    
    /**
     * 图标紧挨文本居中显示，当文本超出控件宽度时，图标紧靠右边
     */
    public static final int SHOW_STYLE_CENTER = 1;
    
    /**
     * 图标紧靠右边展示，文本靠左展示
     */
    public static final int SHOW_STYLE_END = 2;
    
    @IntDef({SHOW_STYLE_START, SHOW_STYLE_CENTER, SHOW_STYLE_END})
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    private @interface ShowStyle {
    }
    
    protected static final int DEFAULT_TEXT_SIZE_PX = 41;
    protected static final int DEFAULT_ICON_SIZE_PX = 41;
    protected static final int DEFAULT_SPACING_PX = 20;
    protected static final int DEFAULT_TEXT_COLOR_EXPAND = 0xff3A559B;
    protected static final int DEFAULT_TEXT_COLOR_COLLAPSE = 0xff333333;
    protected static final int DEFAULT_ICON_COLOR_EXPAND = 0xff3A559B;
    protected static final int DEFAULT_ICON_COLOR_COLLAPSE = 0x66000000;
    
    protected int mTextSize;
    protected int mIconSize;
    protected int mTextColorExpand;
    protected int mTextColorCollapse;
    protected int mIconColorExpand;
    protected int mIconColorCollapse;
    protected boolean mExpand;
    protected int mAngleExpand;
    protected int mAngleCollapse;
    protected int mSpacing;
    protected int mMaxLines;
    
    protected TextView mTvTitle;
    protected AppCompatImageView mIvArrow;
    
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
    
    protected void init(Context context, AttributeSet attrs, int defStyleAttr) {
        String title = null;
        Drawable icon = null;
        int showStyle = SHOW_STYLE_CENTER;
        boolean showIcon = true;
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
                showIcon = typedArray.getBoolean(R.styleable.DropItemView_div_show_icon, true);
                showStyle = typedArray.getInt(R.styleable.DropItemView_div_show_style, SHOW_STYLE_CENTER);
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
        LayoutParams params;
        if (showStyle == SHOW_STYLE_START) {
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.horizontalBias = 0;
        } else if (showStyle == SHOW_STYLE_END) {
            params = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.horizontalBias = 1;
        } else {
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.horizontalBias = 0.5f;
        }
        
        params.constrainedWidth = true;
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
        mIvArrow.setVisibility(showIcon ? VISIBLE : GONE);
        
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
    
    /**
     * 设置展示方式
     *
     * @param showStyle 参考{@link ShowStyle}
     */
    public DropItemView showStyle(@ShowStyle int showStyle) {
        if (mTvTitle.getLayoutParams() == null) {
            return this;
        }
        
        LayoutParams params = (LayoutParams) mTvTitle.getLayoutParams();
        if (showStyle == SHOW_STYLE_START) {
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.horizontalBias = 0;
        } else if (showStyle == SHOW_STYLE_END) {
            params.width = 0;
            params.horizontalBias = 1;
        } else {
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.horizontalBias = 0.5f;
        }
        return this;
    }
    
    /**
     * 设置标题文字
     *
     * @param title CharSequence
     */
    public void setTitle(CharSequence title) {
        if (mTvTitle != null) {
            mTvTitle.setText(title);
        }
    }
    
    /**
     * 设置图片资源
     *
     * @param res DrawableRes
     */
    public void setImageResource(@DrawableRes int res) {
        if (mIvArrow != null) {
            mIvArrow.setImageResource(res);
        }
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
