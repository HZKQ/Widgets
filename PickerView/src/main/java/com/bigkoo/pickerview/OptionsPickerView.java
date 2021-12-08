package com.bigkoo.pickerview;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.lib.WheelView;
import com.bigkoo.pickerview.listener.CustomLayoutCallback;
import com.bigkoo.pickerview.view.BasePickerView;
import com.bigkoo.pickerview.view.WheelOptions;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * 条件选择器
 * Created by Sai on 15/11/22.
 */
public class OptionsPickerView<T> extends BasePickerView implements View.OnClickListener {
    
    private static final String TAG_SUBMIT = "submit";
    private static final String TAG_CANCEL = "cancel";
    
    private WheelOptions<T, T, T> wheelOptions;
    private final int layoutRes;
    private final CustomLayoutCallback mCustomLayoutCallback;
    private TextView tvTitle;
    
    private final OnOptionsSelectListener optionsSelectListener;
    private final OnOptionsSelectListener2 optionsSelectListener2;
    // 确定按钮文字
    private final String mStrSubmit;
    // 取消按钮文字
    private final String mStrCancel;
    // 标题文字
    private final String mStrTitle;
    // 确定按钮颜色
    private final int mColorSubmit;
    // 取消按钮颜色
    private final int mColorCancel;
    // 标题颜色
    private final int mColorTitle;
    // 滚轮背景颜色
    private final int mColorBackgroundWheel;
    // 标题背景颜色
    private final int mColorBackgroundTitle;
    // 确定取消按钮大小
    private final int mSizeSubmitCancel;
    // 标题文字大小
    private final int mSizeTitle;
    // 内容文字大小
    private final int mSizeContent;
    // 未选中内容文字大小
    private final int mSizeOut;
    // 分割线以外的文字颜色
    private final int textColorOut;
    // 分割线之间的文字颜色
    private final int textColorCenter;
    // 分割线的颜色
    private final int dividerColor;
    // 显示时的外部背景色颜色,默认是灰色
    private final int backgroundId;
    // 条目间距倍数 默认1.6
    private final float lineSpacingMultiplier;
    // 可见条目数量
    private final int itemsVisible;
    // 是否是对话框模式
    private final boolean isDialog;
    // 是否能取消
    private final boolean cancelable;
    // 是否只显示中间的label
    private final boolean isCenterLabel;
    // 单位
    private final String label;
    // 是否循环
    private final boolean cycle;
    // 字体样式
    private final Typeface font;
    // 默认选中项
    private int option;
    // x轴偏移量
    private final int xOffset;
    // 分隔线类型
    private final WheelView.DividerType dividerType;
    // 显示位置
    private final int gravity;
    
    // 构造方法
    public OptionsPickerView(Builder builder) {
        super(builder.context);
        this.optionsSelectListener = builder.optionsSelectListener;
        this.optionsSelectListener2 = builder.optionsSelectListener2;
        this.mStrSubmit = builder.Str_Submit;
        this.mStrCancel = builder.Str_Cancel;
        this.mStrTitle = builder.Str_Title;
        
        this.mColorSubmit = builder.Color_Submit;
        this.mColorCancel = builder.Color_Cancel;
        this.mColorTitle = builder.Color_Title;
        this.mColorBackgroundWheel = builder.Color_Background_Wheel;
        this.mColorBackgroundTitle = builder.Color_Background_Title;
        
        this.mSizeSubmitCancel = builder.Size_Submit_Cancel;
        this.mSizeTitle = builder.Size_Title;
        this.mSizeContent = builder.Size_Content;
        this.mSizeOut = builder.Size_Out;
        
        this.cycle = builder.cycle;
        this.cancelable = builder.cancelable;
        this.isCenterLabel = builder.isCenterLabel;
        this.label = builder.label;
        this.font = builder.font;
        this.option = builder.option;
        this.xOffset = builder.xOffset;
        
        this.textColorCenter = builder.textColorCenter;
        this.textColorOut = builder.textColorOut;
        this.dividerColor = builder.dividerColor;
        this.lineSpacingMultiplier = builder.lineSpacingMultiplier;
        this.itemsVisible = builder.itemsVisible;
        this.mCustomLayoutCallback = builder.mCustomLayoutCallback;
        this.layoutRes = builder.layoutRes;
        this.isDialog = builder.isDialog;
        this.dividerType = builder.dividerType;
        this.backgroundId = builder.backgroundId;
        this.setDecorView(builder.decorView);
        this.gravity = builder.gravity;
        initView(builder.context);
    }
    
    // 建造器
    public static class Builder {
        private int layoutRes = R.layout.pickerview_options;
        private CustomLayoutCallback mCustomLayoutCallback;
        private final Context context;
        private final OnOptionsSelectListener optionsSelectListener;
        private final OnOptionsSelectListener2 optionsSelectListener2;
        
        private String Str_Submit;// 确定按钮文字
        private String Str_Cancel;// 取消按钮文字
        private String Str_Title;// 标题文字
        
        private int Color_Submit;// 确定按钮颜色
        private int Color_Cancel;// 取消按钮颜色
        private int Color_Title;// 标题颜色
        
        private int Color_Background_Wheel;// 滚轮背景颜色
        private int Color_Background_Title;// 标题背景颜色
        
        private int Size_Submit_Cancel = 17;// 确定取消按钮大小
        private int Size_Title = 18;// 标题文字大小
        private int Size_Content = 18;// 内容文字大小
        private int Size_Out = 18;// 未选中内容文字大小
        
        private boolean cancelable = true;// 是否能取消
        private boolean isCenterLabel = true;// 是否只显示中间的label
        
        private int textColorOut; // 分割线以外的文字颜色
        private int textColorCenter; // 分割线之间的文字颜色
        private int dividerColor; // 分割线的颜色
        private int backgroundId; // 显示时的外部背景色颜色,默认是灰色
        private ViewGroup decorView;// 显示pickerview的根View,默认是activity的根view
        // 条目间距倍数 默认1.6
        private float lineSpacingMultiplier = 1.6F;
        private int itemsVisible = 7; // 可见条目数量
        private boolean isDialog;// 是否是对话框模式
        
        private String label;
        // 是否循环，默认否
        private boolean cycle = false;
        
        private Typeface font;
        // 默认选中项
        private int option;
        // x轴偏移量
        private int xOffset;
        // 分隔线类型
        private WheelView.DividerType dividerType;
        // 默认显示位置
        private int gravity = Gravity.CENTER;
        
        // Required
        public Builder(Context context, OnOptionsSelectListener listener) {
            this.context = context;
            this.optionsSelectListener = listener;
            this.optionsSelectListener2 = null;
        }
        
        public Builder(Context context, OnOptionsSelectListener2 listener) {
            this.context = context;
            this.optionsSelectListener = null;
            this.optionsSelectListener2 = listener;
        }
        
        // Option
        public Builder setSubmitText(String Str_Cancel) {
            this.Str_Submit = Str_Cancel;
            return this;
        }
        
        public Builder setCancelText(String Str_Cancel) {
            this.Str_Cancel = Str_Cancel;
            return this;
        }
        
        public Builder setTitleText(String Str_Title) {
            this.Str_Title = Str_Title;
            return this;
        }
        
        public Builder isDialog(boolean isDialog) {
            this.isDialog = isDialog;
            return this;
        }
        
        public Builder setSubmitColor(int Color_Submit) {
            this.Color_Submit = Color_Submit;
            return this;
        }
        
        public Builder setCancelColor(int Color_Cancel) {
            this.Color_Cancel = Color_Cancel;
            return this;
        }
        
        /**
         * 显示时的外部背景色颜色,默认是灰色
         */
        public Builder setBackgroundId(int backgroundId) {
            this.backgroundId = backgroundId;
            return this;
        }
        
        /**
         * 必须是{@link ViewGroup}
         * 设置要将PickerView显示到的容器
         */
        public Builder setDecorView(ViewGroup decorView) {
            this.decorView = decorView;
            return this;
        }
        
        public Builder setLayoutRes(int res) {
            return setLayoutRes(res, null);
        }
        
        public Builder setLayoutRes(int res, CustomLayoutCallback listener) {
            this.layoutRes = res;
            this.mCustomLayoutCallback = listener;
            return this;
        }
        
        public Builder setBgColor(int Color_Background_Wheel) {
            this.Color_Background_Wheel = Color_Background_Wheel;
            return this;
        }
        
        public Builder setTitleBgColor(int Color_Background_Title) {
            this.Color_Background_Title = Color_Background_Title;
            return this;
        }
        
        public Builder setTitleColor(int Color_Title) {
            this.Color_Title = Color_Title;
            return this;
        }
        
        public Builder setSubCalSize(int Size_Submit_Cancel) {
            this.Size_Submit_Cancel = Size_Submit_Cancel;
            return this;
        }
        
        public Builder setTitleSize(int Size_Title) {
            this.Size_Title = Size_Title;
            return this;
        }
        
        public Builder setContentTextSize(int Size_Content) {
            this.Size_Content = Size_Content;
            return this;
        }
        
        public Builder setOutTextSize(int Size_Out) {
            this.Size_Out = Size_Out;
            return this;
        }
        
        public Builder setOutSideCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }
        
        public Builder setLabel(String label) {
            this.label = label;
            return this;
        }
        
        /**
         * 设置间距倍数,但是只能在1.2-2.0f之间
         */
        public Builder setLineSpacingMultiplier(float lineSpacingMultiplier) {
            this.lineSpacingMultiplier = lineSpacingMultiplier;
            return this;
        }
        
        /**
         * 设置分割线的颜色
         */
        public Builder setDividerColor(int dividerColor) {
            this.dividerColor = dividerColor;
            return this;
        }
        
        /**
         * 设置分割线的类型
         */
        public Builder setDividerType(WheelView.DividerType dividerType) {
            this.dividerType = dividerType;
            return this;
        }
        
        /**
         * 设置分割线之间的文字的颜色
         */
        public Builder setTextColorCenter(int textColorCenter) {
            this.textColorCenter = textColorCenter;
            return this;
        }
        
        /**
         * 设置分割线以外文字的颜色
         */
        public Builder setTextColorOut(int textColorOut) {
            this.textColorOut = textColorOut;
            return this;
        }
        
        public Builder setTypeface(Typeface font) {
            this.font = font;
            return this;
        }
        
        public Builder setCycle(boolean cycle) {
            this.cycle = cycle;
            return this;
        }
        
        public Builder setSelectOption(int option) {
            this.option = option;
            return this;
        }
        
        
        public Builder setTextXOffset(int xOffset) {
            this.xOffset = xOffset;
            return this;
        }
        
        public Builder isCenterLabel(boolean isCenterLabel) {
            this.isCenterLabel = isCenterLabel;
            return this;
        }
        
        /**
         * 设置控件显示位置，默认显示在屏幕中间
         *
         * @param gravity 只支持{@link Gravity#CENTER}和{@link Gravity#BOTTOM},如果设置其它值，则使用默认值
         */
        public Builder setGravity(int gravity) {
            if (gravity == Gravity.CENTER || gravity == Gravity.BOTTOM) {
                this.gravity = gravity;
            } else {
                this.gravity = Gravity.CENTER;
            }
            return this;
        }
        
        public Builder setItemsVisible(int itemsVisible) {
            this.itemsVisible = itemsVisible;
            return this;
        }
        
        public <T> OptionsPickerView<T> build() {
            return new OptionsPickerView<>(this);
        }
    }
    
    private void initView(Context context) {
        setDialogOutSideCancelable(cancelable);
        setGravity(this.gravity);
        initViews(backgroundId);
        init();
        initEvents();
        if (mCustomLayoutCallback == null) {
            LayoutInflater.from(context).inflate(layoutRes, contentContainer);
            
            // 顶部标题
            tvTitle = (TextView) findViewById(R.id.tvTitle);
            RelativeLayout rvTopBar = (RelativeLayout) findViewById(R.id.rv_topbar);
            
            // 确定和取消按钮
            Button btnSubmit = (Button) findViewById(R.id.btnSubmit);
            // 确定、取消按钮
            Button btnCancel = (Button) findViewById(R.id.btnCancel);
            
            btnSubmit.setTag(TAG_SUBMIT);
            btnCancel.setTag(TAG_CANCEL);
            btnSubmit.setOnClickListener(this);
            btnCancel.setOnClickListener(this);
            
            // 设置文字
            btnSubmit.setText(TextUtils.isEmpty(mStrSubmit) ? context.getResources().getString(R.string.ok_text) : mStrSubmit);
            btnCancel.setText(TextUtils.isEmpty(mStrCancel) ? context.getResources().getString(R.string.cancel_text) : mStrCancel);
            tvTitle.setText(TextUtils.isEmpty(mStrTitle) ? "" : mStrTitle);// 默认为空
            
            // 设置color
            btnSubmit.setTextColor(mColorSubmit == 0 ? pickerview_timebtn_nor : mColorSubmit);
            btnCancel.setTextColor(mColorCancel == 0 ? pickerview_timebtn_nor : mColorCancel);
            tvTitle.setTextColor(mColorTitle == 0 ? pickerview_topbar_title : mColorTitle);
            rvTopBar.setBackgroundColor(mColorBackgroundTitle == 0 ? pickerview_bg_topbar : mColorBackgroundTitle);
            
            // 设置文字大小
            btnSubmit.setTextSize(mSizeSubmitCancel);
            btnCancel.setTextSize(mSizeSubmitCancel);
            tvTitle.setTextSize(mSizeTitle);
            tvTitle.setText(mStrTitle);
        } else {
            mCustomLayoutCallback.initLayout(this, LayoutInflater.from(context).inflate(layoutRes, contentContainer));
        }
        
        // ----滚轮布局
        final LinearLayout optionsPicker = (LinearLayout) findViewById(R.id.optionspicker);
        optionsPicker.setBackgroundColor(mColorBackgroundWheel == 0 ? bgColor_default : mColorBackgroundWheel);
        
        wheelOptions = new WheelOptions<>(optionsPicker, false);
        wheelOptions.setTextContentSize(mSizeContent);
        wheelOptions.setTextOutSize(mSizeOut);
        wheelOptions.setLabels(label, null, null);
        wheelOptions.setTextXOffset(xOffset, 0, 0);
        
        wheelOptions.setCyclic(cycle, false, false);
        wheelOptions.setTypeface(font);
        
        setOutSideCancelable(cancelable);
        
        if (tvTitle != null) {
            tvTitle.setText(mStrTitle);
        }
        
        wheelOptions.setDividerColor(dividerColor);
        wheelOptions.setDividerType(dividerType);
        wheelOptions.setLineSpacingMultiplier(lineSpacingMultiplier);
        wheelOptions.setTextColorOut(textColorOut);
        wheelOptions.setTextColorCenter(textColorCenter);
        wheelOptions.isCenterLabel(isCenterLabel);
        wheelOptions.setItemsVisible(itemsVisible);
    }
    
    /**
     * 设置默认选中项
     */
    public void setSelectOption(int option) {
        this.option = option;
        setCurrentItems();
    }
    
    private void setCurrentItems() {
        if (wheelOptions != null) {
            wheelOptions.setCurrentItems(option, 0, 0);
        }
    }
    
    public void setPicker(List<T> options1Items) {
        wheelOptions.setPicker(options1Items, null, null);
        setCurrentItems();
    }
    
    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag();
        if (tag.equals(TAG_SUBMIT)) {
            returnData(v);
        }
        dismiss();
    }
    
    /**
     * 用户选中数据后调用回调
     */
    @Override
    public void returnData() {
        if (optionsSelectListener != null) {
            int[] optionsCurrentItems = wheelOptions.getCurrentItems();
            optionsSelectListener.onOptionsSelect(this, optionsCurrentItems[0]);
        }
        
        if (optionsSelectListener2 != null) {
            int[] optionsCurrentItems = wheelOptions.getCurrentItems();
            optionsSelectListener2.onOptionsSelect(this, null, optionsCurrentItems[0]);
        }
    }
    
    @Override
    public void returnData(View view) {
        if (optionsSelectListener != null) {
            int[] optionsCurrentItems = wheelOptions.getCurrentItems();
            optionsSelectListener.onOptionsSelect(this, optionsCurrentItems[0]);
        }
        
        if (optionsSelectListener2 != null) {
            int[] optionsCurrentItems = wheelOptions.getCurrentItems();
            optionsSelectListener2.onOptionsSelect(this, view, optionsCurrentItems[0]);
        }
    }
    
    @Override
    public boolean isDialog() {
        return isDialog;
    }
    
    public List<T> getData() {
        return wheelOptions.getData(0);
    }
    
    /**
     * 选中监听
     */
    public interface OnOptionsSelectListener {
        @SuppressWarnings("rawtypes")
        void onOptionsSelect(@NonNull OptionsPickerView pickerView, int option);
    }
    
    /**
     * 选中监听
     */
    public interface OnOptionsSelectListener2 {
        /**
         * @param view 用户使用{@link BasePickerView#returnData(View)}时传递的参数
         */
        @SuppressWarnings("rawtypes")
        void onOptionsSelect(@NonNull OptionsPickerView pickerView, View view, int option);
    }
}
