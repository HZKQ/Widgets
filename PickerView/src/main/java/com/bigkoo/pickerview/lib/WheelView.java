package com.bigkoo.pickerview.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.bigkoo.pickerview.BuildConfig;
import com.bigkoo.pickerview.R;
import com.bigkoo.pickerview.adapter.WheelAdapter;
import com.bigkoo.pickerview.listener.OnItemSelectedListener;
import com.bigkoo.pickerview.model.IPickerViewData;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * 3d滚轮控件
 */
public class WheelView<T> extends View {
    
    /**
     * 点击，滑翔(滑到尽头)，拖拽事件
     */
    public enum ACTION {
        CLICK, FLING, DRAG
    }
    
    /**
     * 分隔线类型
     */
    public enum DividerType {
        FILL, WRAP
    }
    
    /**
     * 分隔线类型
     */
    private DividerType dividerType;
    
    Handler handler;
    private GestureDetector gestureDetector;
    OnItemSelectedListener onItemSelectedListener;
    
    private boolean isOptions = false;
    private boolean isCenterLabel = true;
    
    // Timer mTimer;
    private final ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mFuture;
    
    private Paint paintOuterText;
    private Paint paintCenterText;
    private Paint paintIndicator;
    
    private WheelAdapter<T> adapter;
    
    /**
     * 附加单位
     */
    private String label;
    
    /**
     * 选中项的文字大小
     */
    private int textSize;
    
    /**
     * 未选中项文字大小
     */
    private int textOutSize;
    
    private int maxTextWidth;
    private int maxTextHeight;
    private int textXOffset;
    // 每行高度
    float itemHeight;
    
    // 字体样式，默认是等宽字体
    private Typeface typeface = Typeface.MONOSPACE;
    
    // 灰色
    private int textColorOut = 0xFFa8a8a8;
    // 黑色
    private int textColorCenter = 0xFF2a2a2a;
    // 亮灰色
    private int dividerColor = 0xFFd5d5d5;
    
    // 条目间距倍数
    private float lineSpacingMultiplier = 2F;
    boolean isLoop;
    
    // 第一条线Y坐标值
    private float firstLineY;
    // 第二条线Y坐标
    private float secondLineY;
    // 中间label绘制的Y坐标
    private float centerY;
    
    // 滚动总高度y值
    float totalScrollY;
    // 初始化默认选中项
    int initPosition;
    // 选中的Item是第几个
    private int selectedItem;
    // 之前选中项位置
    private int preCurrentIndex;
    // 绘制几个条目，实际上第一项和最后一项Y轴压缩成0%了，所以可见的数目实际为5
    private int itemsVisible = 7;
    // 记录可见的Item对象
    private Object[] mVisibles;
    // WheelView 控件高度
    private int measuredHeight;
    // WheelView 控件宽度
    private int measuredWidth;
    // 半径
    private int radius;
    
    private int mOffset = 0;
    private float previousY = 0;
    private long startTime = 0;
    
    // 修改这个值可以改变滑行速度
    private static final int VELOCITY_FLING = 5;
    private int widthMeasureSpec;
    
    private int mGravity = Gravity.CENTER;
    // 中间选中文字开始绘制位置
    private int drawCenterContentStart = 0;
    // 非中间文字开始绘制位置
    private int drawOutContentStart = 0;
    // 非中间文字则用此控制高度，压扁形成3d错觉
    private static final float SCALE_CONTENT = 0.9F;
    //偏移量
    private float mCenterContentOffset;
    // 默认文本绘制时错切值
    private static final float DEFAULT_TEXT_TARGET_SKEWX = 0.8f;
    
    public WheelView(Context context) {
        this(context, null);
    }
    
    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 默认大小
        textSize = getResources().getDimensionPixelSize(R.dimen.pickerview_textsize);
        textOutSize = textSize;
        
        // 屏幕密度比（0.75/1.0/1.5/2.0/3.0）
        float density = getResources().getDisplayMetrics().density;
        // 根据密度不同进行适配
        if (density < 1) {
            mCenterContentOffset = 2.4F;
        } else if (density < 1.5) {
            mCenterContentOffset = 3.6F;
        } else if (density < 2) {
            mCenterContentOffset = 4.5F;
        } else if (density < 3) {
            mCenterContentOffset = 6.0F;
        } else if (density >= 3) {
            mCenterContentOffset = density * 2.5F;
        }
        
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WheelView, 0, 0);
            mGravity = a.getInt(R.styleable.WheelView_wv_gravity, Gravity.CENTER);
            textColorOut = a.getColor(R.styleable.WheelView_wv_textColorOut, textColorOut);
            textColorCenter = a.getColor(R.styleable.WheelView_wv_textColorCenter, textColorCenter);
            dividerColor = a.getColor(R.styleable.WheelView_wv_dividerColor, dividerColor);
            textSize = a.getDimensionPixelSize(R.styleable.WheelView_wv_textSize, textSize);
            textOutSize = a.getDimensionPixelSize(R.styleable.WheelView_wv_textOutSize, textOutSize);
            lineSpacingMultiplier = a.getFloat(R.styleable.WheelView_wv_lineSpacingMultiplier, lineSpacingMultiplier);
            itemsVisible = a.getInteger(R.styleable.WheelView_wv_itemsVisible, itemsVisible);
            a.recycle();//回收内存
        }
        
        mVisibles = new Object[itemsVisible];
        
        judgeLineSpace();
        
        initLoopView(context);
    }
    
    /**
     * 判断间距是否在[1.2 , +∞)间,可通过设置该数值增大Item的高度,默认为2倍文字高
     */
    private void judgeLineSpace() {
        if (lineSpacingMultiplier < 1.2f) {
            lineSpacingMultiplier = 1.2f;
        }
    }
    
    private void initLoopView(Context context) {
        handler = new MessageHandler(this);
        gestureDetector = new GestureDetector(context, new LoopViewGestureListener(this));
        gestureDetector.setIsLongpressEnabled(false);
        isLoop = true;
        
        totalScrollY = 0;
        initPosition = -1;
        initPaints();
    }
    
    private void initPaints() {
        paintOuterText = new TextPaint();
        paintOuterText.setColor(textColorOut);
        paintOuterText.setAntiAlias(true);
        paintOuterText.setTypeface(typeface);
        paintOuterText.setTextSize(textOutSize);
        
        paintCenterText = new TextPaint();
        paintCenterText.setColor(textColorCenter);
        paintCenterText.setAntiAlias(true);
        paintCenterText.setTextScaleX(1.1F);
        paintCenterText.setTypeface(typeface);
        paintCenterText.setTextSize(textSize);
        
        paintIndicator = new Paint();
        paintIndicator.setColor(dividerColor);
        paintIndicator.setAntiAlias(true);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }
    
    private void remeasure() {
        // 重新测量
        if (adapter == null) {
            return;
        }
        
        measureTextWidthHeight();
        
        // 半圆的周长 = item高度乘以item数目-1
        // 半圆周长
        int halfCircumference = (int) (itemHeight * (itemsVisible - 1));
        // 整个圆的周长除以PI得到直径，这个直径用作控件的总高度
        measuredHeight = (int) ((halfCircumference * 2) / Math.PI);
        // 求出半径
        radius = (int) (halfCircumference / Math.PI);
        // 控件宽度，这里支持weight
        measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        // 计算两条横线 和 选中项画笔的基线Y位置
        firstLineY = (measuredHeight - itemHeight) / 2.0F;
        secondLineY = (measuredHeight + itemHeight) / 2.0F;
        centerY = secondLineY - (itemHeight - maxTextHeight) / 2.0f - mCenterContentOffset;
        
        // 初始化显示的item的position
        if (initPosition == -1) {
            if (isLoop) {
                initPosition = (adapter.getItemsCount() + 1) / 2;
            } else {
                initPosition = 0;
            }
        }
        preCurrentIndex = initPosition;
    }
    
    /**
     * 计算最大length的Text的宽高度
     */
    private void measureTextWidthHeight() {
        Rect rect = new Rect();
        for (int i = 0; i < adapter.getItemsCount(); i++) {
            String s1 = getContentText(adapter.getItem(i));
            
            paintCenterText.getTextBounds(s1, 0, s1.length(), rect);
            int textWidth = rect.width();
            if (textWidth > maxTextWidth) {
                maxTextWidth = textWidth;
            }
            
            // 星期的字符编码（以它为标准高度）
            paintCenterText.getTextBounds("\u661F\u671F", 0, 2, rect);
            maxTextHeight = rect.height() + 2;
        }
        
        itemHeight = lineSpacingMultiplier * maxTextHeight + 10;
    }
    
    void smoothScroll(ACTION action) {
        // 平滑滚动的实现
        cancelFuture();
        if (action == ACTION.FLING || action == ACTION.DRAG) {
            mOffset = (int) ((totalScrollY % itemHeight + itemHeight) % itemHeight);
            if ((float) mOffset > itemHeight / 2.0F) {//如果超过Item高度的一半，滚动到下一个Item去
                mOffset = (int) (itemHeight - (float) mOffset);
            } else {
                mOffset = -mOffset;
            }
        }
        // 停止的时候，位置有偏移，不是全部都能正确停止到中间位置的，这里把文字位置挪回中间去
        mFuture = mExecutor.scheduleWithFixedDelay(new SmoothScrollTimerTask(this, mOffset), 0, 10, TimeUnit.MILLISECONDS);
    }
    
    protected final void scrollBy(float velocityY) {
        // 滚动惯性的实现
        cancelFuture();
        mFuture = mExecutor.scheduleWithFixedDelay(new InertiaTimerTask(this, velocityY), 0, VELOCITY_FLING, TimeUnit.MILLISECONDS);
    }
    
    public void cancelFuture() {
        if (mFuture != null && !mFuture.isCancelled()) {
            mFuture.cancel(true);
            mFuture = null;
        }
    }
    
    /**
     * 设置是否循环滚动
     *
     * @param cyclic 是否循环
     */
    public final void setCyclic(boolean cyclic) {
        isLoop = cyclic;
    }
    
    public final void setTypeface(Typeface font) {
        typeface = font;
        paintOuterText.setTypeface(typeface);
        paintCenterText.setTypeface(typeface);
    }
    
    public final void setTextSize(int size) {
        if (size > 0.0F) {
            textSize = size;
            paintCenterText.setTextSize(textSize);
        }
    }
    
    public final void setTextOutSize(int size) {
        if (size > 0.0F) {
            textOutSize = size;
            paintOuterText.setTextSize(textOutSize);
        }
    }
    
    public final void setCurrentItem(int currentItem) {
        // 不添加这句,当这个wheelView不可见时,默认都是0,会导致获取到的时间错误
        this.selectedItem = currentItem;
        this.initPosition = currentItem;
        // 回归顶部，不然重设setCurrentItem的话位置会偏移的，就会显示出不对位置的数据
        totalScrollY = 0;
        invalidate();
    }
    
    public final void setItemsVisible(int itemsVisible) {
        if (itemsVisible < 3) {
            itemsVisible = 3;
        }
        
        this.itemsVisible = itemsVisible;
        mVisibles = new Object[itemsVisible];
    }
    
    public final void setOnItemSelectedListener(OnItemSelectedListener OnItemSelectedListener) {
        this.onItemSelectedListener = OnItemSelectedListener;
    }
    
    public final void setAdapter(WheelAdapter<T> adapter) {
        this.adapter = adapter;
        remeasure();
        invalidate();
    }
    
    public final WheelAdapter<T> getAdapter() {
        return adapter;
    }
    
    public final int getCurrentItem() {
        return selectedItem;
    }
    
    protected final void onItemSelected() {
        if (onItemSelectedListener != null) {
            postDelayed(new OnItemSelectedRunnable(this), 200L);
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (adapter == null) {
            return;
        }
        
        // initPosition越界会造成preCurrentIndex的值不正确
        if (initPosition < 0) {
            initPosition = 0;
        }
        
        if (adapter.getItemsCount() > 0 && initPosition >= adapter.getItemsCount()) {
            initPosition = adapter.getItemsCount() - 1;
        }
        
        // 滚动的Y值高度除去每行Item的高度，得到滚动了多少个item，即change数
        int change = (int) (totalScrollY / itemHeight);
        
        try {
            // 滚动中实际的预选中的item(即经过了中间位置的item) ＝ 滑动前的位置 ＋ 滑动相对位置
            preCurrentIndex = initPosition + change % adapter.getItemsCount();
        } catch (ArithmeticException e) {
            if (BuildConfig.DEBUG) {
                Log.e("WheelView", "出错了！adapter.getItemsCount() == 0，联动数据不匹配");
            }
        }
        
        if (!isLoop) {
            // 不循环的情况
            if (preCurrentIndex < 0) {
                preCurrentIndex = 0;
            }
            
            if (adapter.getItemsCount() > 0 && preCurrentIndex > adapter.getItemsCount() - 1) {
                preCurrentIndex = adapter.getItemsCount() - 1;
            }
        } else {
            // 循环
            if (preCurrentIndex < 0) {
                // 举个例子：如果总数是5，preCurrentIndex ＝ -1，那么preCurrentIndex按循环来说，其实是0的上面，也就是4的位置
                preCurrentIndex = adapter.getItemsCount() + preCurrentIndex;
            }
            
            if (adapter.getItemsCount() > 0 && preCurrentIndex > adapter.getItemsCount() - 1) {
                // 同理上面,自己脑补一下
                preCurrentIndex = preCurrentIndex - adapter.getItemsCount();
            }
        }
        // 跟滚动流畅度有关，总滑动距离与每个item高度取余，即并不是一格格的滚动，每个item不一定滚到对应Rect里的，这个item对应格子的偏移值
        float itemHeightOffset = (totalScrollY % itemHeight);
        
        // 设置数组中每个元素的值
        int counter = 0;
        while (counter < itemsVisible) {
            // 索引值，即当前在控件中间的item看作数据源的中间，计算出相对源数据源的index值
            int index = preCurrentIndex - (itemsVisible / 2 - counter);
            // 判断是否循环，如果是循环数据源也使用相对循环的position获取对应的item值，如果不是循环则超出数据源范围使用""空白字符串填充，在界面上形成空白无数据的item项
            if (isLoop) {
                index = getLoopMappingIndex(index);
                mVisibles[counter] = adapter.getItem(index);
            } else if (index < 0) {
                mVisibles[counter] = null;
            } else if (index > adapter.getItemsCount() - 1) {
                mVisibles[counter] = null;
            } else {
                mVisibles[counter] = adapter.getItem(index);
            }
            
            counter++;
        }
        
        // 绘制中间两条横线
        if (dividerType == DividerType.WRAP) {//横线长度仅包裹内容
            float startX;
            float endX;
            
            if (TextUtils.isEmpty(label)) {
                // 隐藏Label的情况
                startX = (measuredWidth - maxTextWidth) / 2f - 12;
            } else {
                startX = (measuredWidth - maxTextWidth) / 4f - 12;
            }
            
            if (startX <= 0) {//如果超过了WheelView的边缘
                startX = 10;
            }
            
            endX = measuredWidth - startX;
            canvas.drawLine(startX, firstLineY, endX, firstLineY, paintIndicator);
            canvas.drawLine(startX, secondLineY, endX, secondLineY, paintIndicator);
        } else {
            canvas.drawLine(0.0F, firstLineY, measuredWidth, firstLineY, paintIndicator);
            canvas.drawLine(0.0F, secondLineY, measuredWidth, secondLineY, paintIndicator);
        }
        
        // 只显示选中项Label文字的模式，并且Label文字不为空，则进行绘制
        if (!TextUtils.isEmpty(label) && isCenterLabel) {
            // 绘制文字，靠右并留出空隙
            int drawRightContentStart = measuredWidth - getTextWidth(paintCenterText, label);
            canvas.drawText(label, drawRightContentStart - mCenterContentOffset, centerY, paintCenterText);
        }
        
        counter = 0;
        while (counter < itemsVisible) {
            canvas.save();
            // 弧长 L = itemHeight * counter - itemHeightOffset
            // 求弧度 α = L / r  (弧长/半径) [0,π]
            double radian = ((itemHeight * counter - itemHeightOffset)) / radius;
            // 弧度转换成角度(把半圆以Y轴为轴心向右转90度，使其处于第一象限及第四象限
            // angle [-90°,90°],item第一项,从90度开始，逐渐递减到 -90度
            float angle = (float) (90D - (radian / Math.PI) * 180D);
            
            // 计算取值可能有细微偏差，保证负90°到90°以外的不绘制
            if (angle >= 90F || angle <= -90F) {
                canvas.restore();
            } else {
                // 根据当前角度计算出偏差系数，用以在绘制时控制文字的 水平移动 透明度 倾斜程度
                float offsetCoefficient = (float) Math.pow(Math.abs(angle) / 90f, 2.2);
                // 获取内容文字
                String contentText;
                // 如果是label每项都显示的模式，并且item内容不为空、label 也不为空
                if (!isCenterLabel && !TextUtils.isEmpty(label) && !TextUtils.isEmpty(getContentText(mVisibles[counter]))) {
                    contentText = getContentText(mVisibles[counter]) + label;
                } else {
                    contentText = getContentText(mVisibles[counter]);
                }
                
                reMeasureTextSize(contentText);
                // 计算开始绘制的位置
                measuredCenterContentStart(contentText);
                measuredOutContentStart(contentText);
                float translateY = (float) (radius - Math.cos(radian) * radius - (Math.sin(radian) * maxTextHeight) / 2D);
                // 根据Math.sin(radian)来更改canvas坐标系原点，然后缩放画布，使得文字高度进行缩放，形成弧形3d视觉差
                canvas.translate(0.0F, translateY);
                // canvas.scale(1.0F, (float) Math.sin(radian));
                if (translateY <= firstLineY && maxTextHeight + translateY >= firstLineY) {
                    // 条目经过第一条线
                    canvas.save();
                    canvas.clipRect(0, 0, measuredWidth, firstLineY - translateY);
                    canvas.scale(1.0F, (float) Math.sin(radian) * SCALE_CONTENT);
                    canvas.drawText(contentText, drawOutContentStart, maxTextHeight, paintOuterText);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, firstLineY - translateY, measuredWidth, (int) (itemHeight));
                    canvas.scale(1.0F, (float) Math.sin(radian));
                    canvas.drawText(contentText, drawCenterContentStart, maxTextHeight - mCenterContentOffset, paintCenterText);
                    canvas.restore();
                } else if (translateY <= secondLineY && maxTextHeight + translateY >= secondLineY) {
                    // 条目经过第二条线
                    canvas.save();
                    canvas.clipRect(0, 0, measuredWidth, secondLineY - translateY);
                    canvas.scale(1.0F, (float) Math.sin(radian));
                    canvas.drawText(contentText, drawCenterContentStart, maxTextHeight - mCenterContentOffset, paintCenterText);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, secondLineY - translateY, measuredWidth, (int) (itemHeight));
                    canvas.scale(1.0F, (float) Math.sin(radian) * SCALE_CONTENT);
                    canvas.drawText(contentText, drawOutContentStart, maxTextHeight, paintOuterText);
                    canvas.restore();
                } else if (translateY >= firstLineY && maxTextHeight + translateY <= secondLineY) {
                    // 中间条目
                    // canvas.clipRect(0, 0, measuredWidth,   maxTextHeight);
                    // 让文字居中
                    float Y = maxTextHeight - mCenterContentOffset;//因为圆弧角换算的向下取值，导致角度稍微有点偏差，加上画笔的基线会偏上，因此需要偏移量修正一下
                    canvas.drawText(contentText, drawCenterContentStart, Y, paintCenterText);
                    Object visible = mVisibles[counter];
                    //noinspection unchecked
                    selectedItem = adapter.indexOf(visible == null ? null : (T) visible);
                } else {
                    // 其他条目
                    canvas.save();
                    canvas.clipRect(0, 0, measuredWidth, (int) (itemHeight));
                    canvas.scale(1.0F, (float) Math.sin(radian) * SCALE_CONTENT);
                    // 控制文字倾斜角度
                    paintOuterText.setTextSkewX((Integer.compare(textXOffset, 0)) * (angle > 0 ? -1 : 1) * DEFAULT_TEXT_TARGET_SKEWX * offsetCoefficient);
                    // 控制透明度
                    paintOuterText.setAlpha((int) ((1 - offsetCoefficient) * 255));
                    // 控制文字水平便宜距离
                    canvas.drawText(contentText, drawOutContentStart + textXOffset * offsetCoefficient, maxTextHeight, paintOuterText);
                    canvas.restore();
                }
                canvas.restore();
                paintCenterText.setTextSize(textSize);
            }
            counter++;
        }
    }
    
    /**
     * 根据文字的长度 重新设置文字的大小 让其能完全显示
     */
    private void reMeasureTextSize(String contentText) {
        Rect rect = new Rect();
        paintCenterText.getTextBounds(contentText, 0, contentText.length(), rect);
        int width = rect.width();
        int size = textSize;
        while (width > measuredWidth) {
            size--;
            // 设置2条横线中间的文字大小
            paintCenterText.setTextSize(size);
            paintCenterText.getTextBounds(contentText, 0, contentText.length(), rect);
            width = rect.width();
        }
        
        // 设置2条横线外面的文字大小
        paintOuterText.setTextSize(size * 1f / textSize * textOutSize);
    }
    
    
    // 递归计算出对应的index
    private int getLoopMappingIndex(int index) {
        if (index < 0) {
            index = index + adapter.getItemsCount();
            index = getLoopMappingIndex(index);
        } else if (index > adapter.getItemsCount() - 1) {
            index = index - adapter.getItemsCount();
            index = getLoopMappingIndex(index);
        }
        return index;
    }
    
    /**
     * 根据传进来的对象获取getPickerViewText()方法，来获取需要显示的值
     *
     * @param item 数据源的item
     * @return 对应显示的字符串
     */
    private String getContentText(Object item) {
        if (item == null) {
            return "";
        } else if (item instanceof IPickerViewData) {
            return ((IPickerViewData) item).getPickerViewText();
        } else if (item instanceof Integer) {
            // 如果为整形则最少保留两位数.
            return String.format(Locale.getDefault(), "%02d", (Integer) item);
        }
        return item.toString();
    }
    
    private void measuredCenterContentStart(String content) {
        Rect rect = new Rect();
        paintCenterText.getTextBounds(content, 0, content.length(), rect);
        switch (mGravity) {
            case Gravity.CENTER://显示内容居中
                if (isOptions || label == null || label.equals("") || !isCenterLabel) {
                    drawCenterContentStart = (int) ((measuredWidth - rect.width()) * 0.5);
                } else {//只显示中间label时，时间选择器内容偏左一点，留出空间绘制单位标签
                    drawCenterContentStart = (int) ((measuredWidth - rect.width()) * 0.25);
                }
                break;
            case Gravity.LEFT:
                drawCenterContentStart = 0;
                break;
            case Gravity.RIGHT://添加偏移量
                drawCenterContentStart = measuredWidth - rect.width() - (int) mCenterContentOffset;
                break;
        }
    }
    
    private void measuredOutContentStart(String content) {
        Rect rect = new Rect();
        paintOuterText.getTextBounds(content, 0, content.length(), rect);
        switch (mGravity) {
            case Gravity.CENTER:
                if (isOptions || label == null || label.equals("") || !isCenterLabel) {
                    drawOutContentStart = (int) ((measuredWidth - rect.width()) * 0.5);
                } else {//只显示中间label时，时间选择器内容偏左一点，留出空间绘制单位标签
                    drawOutContentStart = (int) ((measuredWidth - rect.width()) * 0.25);
                }
                break;
            case Gravity.LEFT:
                drawOutContentStart = 0;
                break;
            case Gravity.RIGHT:
                drawOutContentStart = measuredWidth - rect.width() - (int) mCenterContentOffset;
                break;
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.widthMeasureSpec = widthMeasureSpec;
        remeasure();
        setMeasuredDimension(measuredWidth, measuredHeight);
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean eventConsumed = gestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            // 按下
            case MotionEvent.ACTION_DOWN:
                startTime = System.currentTimeMillis();
                cancelFuture();
                previousY = event.getRawY();
                break;
            
            // 滑动中
            case MotionEvent.ACTION_MOVE:
                float dy = previousY - event.getRawY();
                previousY = event.getRawY();
                totalScrollY = totalScrollY + dy;
                
                // 边界处理。
                if (!isLoop) {
                    float top = -initPosition * itemHeight;
                    float bottom = (adapter.getItemsCount() - 1 - initPosition) * itemHeight;
                    
                    if (totalScrollY - itemHeight * 0.25 < top) {
                        top = totalScrollY - dy;
                    } else if (totalScrollY + itemHeight * 0.25 > bottom) {
                        bottom = totalScrollY - dy;
                    }
                    
                    if (totalScrollY < top) {
                        totalScrollY = (int) top;
                    } else if (totalScrollY > bottom) {
                        totalScrollY = (int) bottom;
                    }
                }
                break;
            
            // 完成滑动，手指离开屏幕
            case MotionEvent.ACTION_UP:
            default:
                if (!eventConsumed) {
                    // 未消费掉事件
                    
                    // 关于弧长的计算
                    // 弧长公式： L = α*R
                    // 反余弦公式：arccos(cosα) = α
                    // 由于之前是有顺时针偏移90度，
                    // 所以实际弧度范围α2的值 ：α2 = π/2-α    （α=[0,π] α2 = [-π/2,π/2]）
                    // 根据正弦余弦转换公式 cosα = sin(π/2-α)
                    // 代入，得： cosα = sin(π/2-α) = sinα2 = (R - y) / R
                    // 所以弧长 L = arccos(cosα)*R = arccos((R - y) / R)*R
                    
                    float y = event.getY();
                    double L = Math.acos((radius - y) / radius) * radius;
                    // item0 有一半是在不可见区域，所以需要加上 itemHeight / 2
                    int circlePosition = (int) ((L + itemHeight / 2) / itemHeight);
                    float extraOffset = (totalScrollY % itemHeight + itemHeight) % itemHeight;
                    // 已滑动的弧长值
                    mOffset = (int) ((circlePosition - itemsVisible / 2) * itemHeight - extraOffset);
                    
                    if ((System.currentTimeMillis() - startTime) > 120) {
                        // 处理拖拽事件
                        smoothScroll(ACTION.DRAG);
                    } else {
                        // 处理条目点击事件
                        smoothScroll(ACTION.CLICK);
                    }
                }
                break;
        }
        
        invalidate();
        return true;
    }
    
    /**
     * 获取Item个数
     *
     * @return item个数
     */
    public int getItemsCount() {
        return adapter != null ? adapter.getItemsCount() : 0;
    }
    
    /**
     * 附加在右边的单位字符串
     *
     * @param label 单位
     */
    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void isCenterLabel(boolean isCenterLabel) {
        this.isCenterLabel = isCenterLabel;
    }
    
    public void setGravity(int gravity) {
        this.mGravity = gravity;
    }
    
    public int getTextWidth(Paint paint, String str) {
        // 计算文字宽度
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }
    
    public void setIsOptions(boolean options) {
        isOptions = options;
    }
    
    public void setTextColorOut(int textColorOut) {
        if (textColorOut != 0) {
            this.textColorOut = textColorOut;
            paintOuterText.setColor(this.textColorOut);
        }
    }
    
    public void setTextColorCenter(int textColorCenter) {
        if (textColorCenter != 0) {
            
            this.textColorCenter = textColorCenter;
            paintCenterText.setColor(this.textColorCenter);
        }
    }
    
    public void setTextXOffset(int textXOffset) {
        this.textXOffset = textXOffset;
        if (textXOffset != 0) {
            paintCenterText.setTextScaleX(1.0f);
        }
    }
    
    public void setDividerColor(int dividerColor) {
        if (dividerColor != 0) {
            this.dividerColor = dividerColor;
            paintIndicator.setColor(this.dividerColor);
        }
    }
    
    public void setDividerType(DividerType dividerType) {
        this.dividerType = dividerType;
    }
    
    public void setLineSpacingMultiplier(float lineSpacingMultiplier) {
        if (lineSpacingMultiplier != 0) {
            this.lineSpacingMultiplier = lineSpacingMultiplier;
            judgeLineSpace();
        }
    }
}