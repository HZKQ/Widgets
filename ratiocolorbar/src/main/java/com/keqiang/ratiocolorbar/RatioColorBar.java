package com.keqiang.ratiocolorbar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.keqiang.ratiocolorbar.entity.RatioBarData;

import java.util.ArrayList;
import java.util.List;

import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * 彩虹条，该彩虹条数据都是连续的，会将{@link #setColorBars(List)}列表按顺序连续绘制，{@link RatioBarData#getValue()}代表彩虹条占比，数值越大，绘制宽度越宽
 *
 * @author Created by zhouzhuo810 on 2017/12/28.
 */
public class RatioColorBar extends View {
    private boolean showBorder;
    private boolean showPadding;
    private int borderWidth;
    private int borderColor;
    private int padding;
    
    private Paint borderPaint;
    private Paint barPaint;
    
    private int selection = -1;
    
    private List<RatioBarDataInner> colorBars;
    private OnBarSelectedChangeListener mOnBarSelectedChangeListener;
    
    //彩虹条bar弹窗相关组件
    private PopupWindow mRainbowBarPop;
    private View mContentView;
    private RelativeLayout mDecorView;
    private ImageView mUpArrow;
    private ImageView mDownArrow;
    
    
    /**
     * 用户设置的显示到弹窗的View
     */
    private View mUserContentView;
    
    /**
     * 用于指示popwindow位置的箭头宽度
     */
    private int mArrowWidth;
    
    /**
     * 用于指示popwindow位置的箭头高度
     */
    private int mArrowHeight;
    
    public interface OnBarSelectedChangeListener {
        /**
         * @param contentView  用户设置的用于弹窗弹出时展示需要显示的内容
         * @param position     当前选择的Item的下标
         * @param selected     当前选择的Item是否被选中
         * @param ratioBarData 当前选择的Item的关联数据
         */
        void onSelectedChange(@Nullable View contentView, int position, boolean selected, RatioBarData ratioBarData);
    }
    
    public void setOnBarSelectedChangeListener(OnBarSelectedChangeListener onBarSelectedChangeListener) {
        this.mOnBarSelectedChangeListener = onBarSelectedChangeListener;
    }
    
    public RatioColorBar(Context context) {
        super(context);
        init(context, null);
    }
    
    public RatioColorBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public RatioColorBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RatioColorBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.RatioColorBar);
            showBorder = t.getBoolean(R.styleable.RatioColorBar_rcb_show_border, false);
            showPadding = t.getBoolean(R.styleable.RatioColorBar_rcb_show_padding, false);
            borderColor = t.getColor(R.styleable.RatioColorBar_rcb_border_color, 0xffeeeeee);
            borderWidth = t.getDimensionPixelSize(R.styleable.RatioColorBar_rcb_border_width, 1);
            padding = t.getDimensionPixelSize(R.styleable.RatioColorBar_rcb_padding, 1);
            t.recycle();
        } else {
            showBorder = false;
            showPadding = false;
            borderColor = 0xffeeeeee;
            borderWidth = 1;
            padding = 1;
        }
        
        mArrowWidth = 10;
        mArrowHeight = 10;
        
        if (!isInEditMode()) {
            borderWidth = SimpleUtil.getScaledValue(borderWidth);
            padding = SimpleUtil.getScaledValue(padding);
            mArrowWidth = SimpleUtil.getScaledValue(mArrowWidth);
            mArrowHeight = SimpleUtil.getScaledValue(mArrowHeight);
        }
        
        initPaints();
        initPop();
    }
    
    private void initPaints() {
        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        
        barPaint = new Paint();
        barPaint.setAntiAlias(true);
        barPaint.setStyle(Paint.Style.FILL);
    }
    
    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    private void initPop() {
        mContentView = LayoutInflater.from(getContext()).inflate(R.layout.pop_view_color_bar, null);
        // 5.0版本之前用Inflater渲染组件的时候，如果没有给其指定父控件（root为null），则渲染器不会去解析width 和 height属性，所以在调用measure时就会导致空指针异常
        // 5.0(包括5.0)之后，加不加都可以
        mContentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        SimpleUtil.scaleView(mContentView);
        mDecorView = mContentView.findViewById(R.id.ll_decorView);
        mUpArrow = mContentView.findViewById(R.id.up_arrow);
        mDownArrow = mContentView.findViewById(R.id.down_arrow);
        
        mRainbowBarPop = new PopupWindow(mContentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
        mRainbowBarPop.setBackgroundDrawable(new ColorDrawable());
        mRainbowBarPop.setOutsideTouchable(true);
        mRainbowBarPop.setTouchable(true);
        mRainbowBarPop.setFocusable(false);
        mRainbowBarPop.setTouchInterceptor((v, event) -> {
            // 不拦截返回键，按返回键不会dismiss
            return false;
        });
    }
    
    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float startX = 0;
        float endX = getWidth();
        float startY = 0;
        float endY = getHeight();
        
        if (showBorder) {
            startX += borderWidth;
            endX -= borderWidth;
            startY += borderWidth;
            endY -= borderWidth;
            canvas.drawRect(startX, startY, endX, endY, borderPaint);
        }
        
        if (colorBars != null) {
            if (showPadding) {
                startX += padding;
                endX -= padding;
                startY += padding;
                endY -= padding;
            }
            
            float dx = endX - startX;
            float sum = 0;
            for (RatioBarDataInner colorBar : colorBars) {
                sum += colorBar.ratioBarData.getValue();
            }
            
            if (sum != 0) {
                float realStartX = startX;
                for (RatioBarDataInner colorBar : colorBars) {
                    int color = colorBar.ratioBarData.getColor();
                    float ratioWidth = dx * colorBar.ratioBarData.getValue() / sum;
                    barPaint.setColor(color);
                    colorBar.startX = realStartX;
                    colorBar.endX = realStartX + (ratioWidth < 1f ? 1f : ratioWidth);
                    canvas.drawRect(realStartX, startY, realStartX + ratioWidth, endY, barPaint);
                    realStartX += ratioWidth;
                }
            }
        }
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOnBarSelectedChangeListener == null) {
            return super.onTouchEvent(event);
        }
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                doTouch(judgeTouchPosition(event.getX()));
                break;
            case MotionEvent.ACTION_MOVE:
                doTouch(judgeTouchPosition(event.getX()));
                break;
            
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return true;
    }
    
    /**
     * 执行用户触摸操作
     *
     * @param curSelect 当前触摸位置所对应Item的位置
     */
    private void doTouch(int curSelect) {
        if (selection == curSelect) {
            if (selection == -1 || mRainbowBarPop.isShowing()) {
                return;
            }
        } else if (selection != -1) {
            colorBars.get(selection).selected = false;
        }
        
        if (curSelect >= 0) {
            colorBars.get(curSelect).selected = true;
            RatioBarDataInner data = colorBars.get(curSelect);
            int[] positions = new int[2];
            getLocationOnScreen(positions);
            if (selection != curSelect && mOnBarSelectedChangeListener != null) {
                mOnBarSelectedChangeListener.onSelectedChange(mUserContentView, curSelect, data.selected, data.ratioBarData);
            }
            showItemInfoPop(data.startX + positions[0], (data.endX - data.startX) / 2, positions[1]);
        }
        selection = curSelect;
    }
    
    /**
     * 判断触摸点所对应的Item
     *
     * @param downX 触摸位置的x坐标
     * @return position
     */
    private int judgeTouchPosition(float downX) {
        if (colorBars != null) {
            for (int i = 0; i < colorBars.size(); i++) {
                RatioBarDataInner ratioBarData = colorBars.get(i);
                if (downX > ratioBarData.startX && downX < ratioBarData.endX) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    public void showItemInfoPop(final float startX, final float halfWidth, final float y) {
        mContentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int width = mContentView.getMeasuredWidth();
        int height = mContentView.getMeasuredHeight();
        
        // 计算PopWindowX轴起始位置，如果最左边有足够空间，
        // 则始终居中显示，如果没有则往右偏移
        int x = (int) startX;
        x += halfWidth - width / 2f;
        if (x < 0) {
            x = 0;
        }
        
        // 如果彩虹条上方 有足够空间展示mContentView就展示在上方，否则展示在下方
        int yy = (int) (y - height);
        if (y < height + mArrowHeight) {
            // 多 + mArrowHeight高度是不希望PopWindow离顶部一点缝隙都没有，而且是一个防止内容显示不下的容错高度
            // 此时展示在下方
            yy = (int) (y + getHeight());
        }
        
        mContentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 自动调整箭头的位置
                autoAdjustArrowPos(startX, halfWidth, y);
                mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        
        if (mRainbowBarPop.isShowing()) {
            mRainbowBarPop.update(x, yy, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            mRainbowBarPop.showAtLocation(this, Gravity.TOP | Gravity.START, x, yy);
        }
    }
    
    /**
     * 计算箭头的位置，计算结果始终使箭头尖位于当前选中彩虹条正中间
     *
     * @param startX    所触摸彩虹条颜色块起始X坐标
     * @param halfWidth 所触摸彩虹条颜色块宽度
     * @param y         当前View的y坐标
     */
    private void autoAdjustArrowPos(float startX, float halfWidth, float y) {
        int[] positions = new int[2];
        mContentView.getLocationOnScreen(positions);
        
        if (positions[1] < y) {
            mUpArrow.setVisibility(View.INVISIBLE);
            mDownArrow.setVisibility(View.VISIBLE);
        } else {
            mUpArrow.setVisibility(View.VISIBLE);
            mDownArrow.setVisibility(View.INVISIBLE);
        }
        
        int leftMargin = (int) (halfWidth + startX - positions[0]);
        leftMargin -= mArrowWidth;
        if (leftMargin < 0) {
            leftMargin = 0;
        }
        
        LinearLayout.LayoutParams upArrowParams = (LinearLayout.LayoutParams) mUpArrow.getLayoutParams();
        upArrowParams.leftMargin = leftMargin;
        LinearLayout.LayoutParams downArrowParams = (LinearLayout.LayoutParams) mDownArrow.getLayoutParams();
        downArrowParams.leftMargin = leftMargin;
        
        mUpArrow.setLayoutParams(upArrowParams);
        mDownArrow.setLayoutParams(downArrowParams);
    }
    
    /**
     * 设置彩虹条数据
     *
     * @param colorBars {@link RatioBarData}集合
     */
    public <T extends RatioBarData> void setColorBars(List<T> colorBars) {
        if (this.colorBars == null) {
            this.colorBars = new ArrayList<>();
        } else {
            this.colorBars.clear();
        }
        
        RatioBarDataInner inner;
        if (colorBars != null) {
            for (RatioBarData bar : colorBars) {
                inner = new RatioBarDataInner();
                inner.ratioBarData = bar;
                this.colorBars.add(inner);
            }
        }
        
        invalidate();
    }
    
    /**
     * 设置在弹窗中展示的View
     *
     * @param contentView view对象
     */
    public void setContentView(@NonNull View contentView) {
        SimpleUtil.scaleView(contentView);
        mDecorView.removeAllViews();
        mDecorView.addView(contentView);
        mUserContentView = contentView;
    }
    
    /**
     * 设置在弹窗中展示的View
     *
     * @param resId view资源Id
     */
    public void setContentView(@LayoutRes int resId) {
        setContentView(LayoutInflater.from(getContext()).inflate(resId, null));
    }
    
    /**
     * 返回用户设置的ContentView
     *
     * @return 如果用户没有设置过任何内容则返回null
     */
    @Nullable
    public View getContentView() {
        return mUserContentView;
    }
    
    /**
     * 设置向上的标记，展示彩虹条详情的弹窗在彩虹条底部弹出时展示
     *
     * @param resId 图片资源Id
     */
    public void setUpFlag(@DrawableRes int resId) {
        mUpArrow.setImageResource(resId);
    }
    
    /**
     * 设置向下的标记，展示彩虹条详情的弹窗在彩虹条顶部弹出时展示
     *
     * @param resId 图片资源Id
     */
    public void setDownFlag(@DrawableRes int resId) {
        mDownArrow.setImageResource(resId);
    }
    
    /**
     * 设置弹窗背景色
     *
     * @param colorId 颜色资源ID
     */
    public void setRainbowBarPopBg(@ColorRes int colorId) {
        mContentView.setBackgroundColor(ContextCompat.getColor(getContext(), colorId));
    }
    
    /**
     * 设置弹窗背景色
     *
     * @param color 颜色值
     */
    public void setRainbowBarPopBgColor(@ColorInt int color) {
        mContentView.setBackgroundColor(color);
    }
    
    public void setPopOnDismissListener(PopupWindow.OnDismissListener dismissListener) {
        mRainbowBarPop.setOnDismissListener(dismissListener);
    }
    
    public void hideRainbowBarPop() {
        if (mRainbowBarPop.isShowing()) {
            mRainbowBarPop.dismiss();
        }
    }
    
    
    private class RatioBarDataInner {
        private boolean selected;
        private float startX;
        private float endX;
        private RatioBarData ratioBarData;
    }
}
