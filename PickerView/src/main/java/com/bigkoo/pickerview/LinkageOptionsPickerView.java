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
import androidx.annotation.Nullable;

/**
 * 联动数据条件选择器
 *
 * @author Created by Sai on 15/11/22.
 */
public class LinkageOptionsPickerView<T1, T2, T3> extends BasePickerView implements View.OnClickListener {
    
    private static final String TAG_SUBMIT = "submit";
    private static final String TAG_CANCEL = "cancel";
    
    private WheelOptions<T1, T2, T3> wheelOptions;
    private final int layoutRes;
    private final CustomLayoutCallback mCustomLayoutCallback;
    private TextView tvTitle;
    
    private final OnOptionsSelectListener optionsSelectListener;
    private final OnOptionsSelectListener2 optionsSelectListener2;
    
    private final String Str_Submit;// 确定按钮文字
    private final String Str_Cancel;// 取消按钮文字
    private final String Str_Title;// 标题文字
    
    private final int Color_Submit;// 确定按钮颜色
    private final int Color_Cancel;// 取消按钮颜色
    private final int Color_Title;// 标题颜色
    
    private final int Color_Background_Wheel;// 滚轮背景颜色
    private final int Color_Background_Title;// 标题背景颜色
    
    private final int Size_Submit_Cancel;// 确定取消按钮大小
    private final int Size_Title;// 标题文字大小
    private final int Size_Content;// 内容文字大小
    private final int Size_Out;// 未选中内容文字大小
    
    private final int textColorOut; // 分割线以外的文字颜色
    private final int textColorCenter; // 分割线之间的文字颜色
    private final int dividerColor; // 分割线的颜色
    private final int backgroundId; // 显示时的外部背景色颜色,默认是灰色
    // 条目间距倍数 默认1.6
    private final float lineSpacingMultiplier;
    private final int itemsVisible; // 可见条目数量
    private final boolean isDialog;// 是否是对话框模式
    
    private final boolean cancelable;// 是否能取消
    private final boolean linkage;// 是否联动
    
    private final boolean isCenterLabel;// 是否只显示中间的label
    
    // 单位
    private final String label1;
    private final String label2;
    private final String label3;
    
    // 是否循环
    private final boolean cyclic1;
    private final boolean cyclic2;
    private final boolean cyclic3;
    
    // 字体样式
    private final Typeface font;
    
    // 默认选中项
    private int option1;
    private int option2;
    private int option3;
    
    // x轴偏移量
    private final int xoffset_one;
    private final int xoffset_two;
    private final int xoffset_three;
    
    // 分隔线类型
    private final WheelView.DividerType dividerType;
    // 显示位置
    private final int gravity;
    
    // 构造方法
    public LinkageOptionsPickerView(Builder builder) {
        super(builder.context);
        this.optionsSelectListener = builder.optionsSelectListener;
        this.optionsSelectListener2 = builder.optionsSelectListener2;
        this.Str_Submit = builder.Str_Submit;
        this.Str_Cancel = builder.Str_Cancel;
        this.Str_Title = builder.Str_Title;
        
        this.Color_Submit = builder.Color_Submit;
        this.Color_Cancel = builder.Color_Cancel;
        this.Color_Title = builder.Color_Title;
        this.Color_Background_Wheel = builder.Color_Background_Wheel;
        this.Color_Background_Title = builder.Color_Background_Title;
        
        this.Size_Submit_Cancel = builder.Size_Submit_Cancel;
        this.Size_Title = builder.Size_Title;
        this.Size_Content = builder.Size_Content;
        this.Size_Out = builder.Size_Out;
        
        this.cyclic1 = builder.cyclic1;
        this.cyclic2 = builder.cyclic2;
        this.cyclic3 = builder.cyclic3;
        
        this.cancelable = builder.cancelable;
        this.linkage = builder.linkage;
        this.isCenterLabel = builder.isCenterLabel;
        
        this.label1 = builder.label1;
        this.label2 = builder.label2;
        this.label3 = builder.label3;
        
        this.font = builder.font;
        
        this.option1 = builder.option1;
        this.option2 = builder.option2;
        this.option3 = builder.option3;
        this.xoffset_one = builder.xoffset_one;
        this.xoffset_two = builder.xoffset_two;
        this.xoffset_three = builder.xoffset_three;
        
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
    
    //建造器
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
        private boolean linkage = true;// 是否联动
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
        
        private String label1;
        private String label2;
        private String label3;
        // 是否循环，默认否
        private boolean cyclic1 = false;
        private boolean cyclic2 = false;
        private boolean cyclic3 = false;
        
        private Typeface font;
        // 默认选中项
        private int option1;
        private int option2;
        private int option3;
        // x轴偏移量
        private int xoffset_one;
        private int xoffset_two;
        private int xoffset_three;
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
            this.optionsSelectListener2 = listener;
            this.optionsSelectListener = null;
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
         * 必须是viewgroup
         * 设置要将pickerview显示到的容器
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
        
        /**
         * 此方法已废弃
         * 不联动的情况下，请调用 setNPicker 方法。
         */
        @Deprecated
        public Builder setLinkage(boolean linkage) {
            this.linkage = linkage;
            return this;
        }
        
        public Builder setLabels(String label1, String label2, String label3) {
            this.label1 = label1;
            this.label2 = label2;
            this.label3 = label3;
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
        
        public Builder setCyclic(boolean cyclic1, boolean cyclic2, boolean cyclic3) {
            this.cyclic1 = cyclic1;
            this.cyclic2 = cyclic2;
            this.cyclic3 = cyclic3;
            return this;
        }
        
        public Builder setSelectOptions(int option1) {
            this.option1 = option1;
            return this;
        }
        
        public Builder setSelectOptions(int option1, int option2) {
            this.option1 = option1;
            this.option2 = option2;
            return this;
        }
        
        public Builder setSelectOptions(int option1, int option2, int option3) {
            this.option1 = option1;
            this.option2 = option2;
            this.option3 = option3;
            return this;
        }
        
        public Builder setTextXOffset(int xoffset_one, int xoffset_two, int xoffset_three) {
            this.xoffset_one = xoffset_one;
            this.xoffset_two = xoffset_two;
            this.xoffset_three = xoffset_three;
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
        
        public <T1, T2, T3> LinkageOptionsPickerView<T1, T2, T3> build() {
            return new LinkageOptionsPickerView<>(this);
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
            btnSubmit.setText(TextUtils.isEmpty(Str_Submit) ? context.getResources().getString(R.string.ok_text) : Str_Submit);
            btnCancel.setText(TextUtils.isEmpty(Str_Cancel) ? context.getResources().getString(R.string.cancel_text) : Str_Cancel);
            tvTitle.setText(TextUtils.isEmpty(Str_Title) ? "" : Str_Title);// 默认为空
            
            // 设置color
            btnSubmit.setTextColor(Color_Submit == 0 ? pickerview_timebtn_nor : Color_Submit);
            btnCancel.setTextColor(Color_Cancel == 0 ? pickerview_timebtn_nor : Color_Cancel);
            tvTitle.setTextColor(Color_Title == 0 ? pickerview_topbar_title : Color_Title);
            rvTopBar.setBackgroundColor(Color_Background_Title == 0 ? pickerview_bg_topbar : Color_Background_Title);
            
            // 设置文字大小
            btnSubmit.setTextSize(Size_Submit_Cancel);
            btnCancel.setTextSize(Size_Submit_Cancel);
            tvTitle.setTextSize(Size_Title);
            tvTitle.setText(Str_Title);
        } else {
            mCustomLayoutCallback.initLayout(this, LayoutInflater.from(context).inflate(layoutRes, contentContainer));
        }
        
        // ----滚轮布局
        final LinearLayout optionsPicker = (LinearLayout) findViewById(R.id.optionspicker);
        optionsPicker.setBackgroundColor(Color_Background_Wheel == 0 ? bgColor_default : Color_Background_Wheel);
        
        wheelOptions = new WheelOptions<>(optionsPicker, linkage);
        wheelOptions.setTextContentSize(Size_Content);
        wheelOptions.setTextOutSize(Size_Out);
        wheelOptions.setLabels(label1, label2, label3);
        wheelOptions.setTextXOffset(xoffset_one, xoffset_two, xoffset_three);
        
        wheelOptions.setCyclic(cyclic1, cyclic2, cyclic3);
        wheelOptions.setTypeface(font);
        
        setOutSideCancelable(cancelable);
        
        if (tvTitle != null) {
            tvTitle.setText(Str_Title);
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
    public void setSelectOptions(int option1) {
        this.option1 = option1;
        setCurrentItems();
    }
    
    public void setSelectOptions(int option1, int option2) {
        this.option1 = option1;
        this.option2 = option2;
        setCurrentItems();
    }
    
    public void setSelectOptions(int option1, int option2, int option3) {
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        setCurrentItems();
    }
    
    private void setCurrentItems() {
        if (wheelOptions != null) {
            wheelOptions.setCurrentItems(option1, option2, option3);
        }
    }
    
    public void setPicker(List<T1> optionsItems) {
        this.setPicker(optionsItems, null, null);
    }
    
    public void setPicker(List<T1> options1Items, List<List<T2>> options2Items) {
        this.setPicker(options1Items, options2Items, null);
    }
    
    public void setPicker(List<T1> options1Items,
                          List<List<T2>> options2Items,
                          List<List<List<T3>>> options3Items) {
        wheelOptions.setPicker(options1Items, options2Items, options3Items);
        setCurrentItems();
    }
    
    /**
     * 不联动情况下调用
     */
    public void setNPicker(List<T1> options1Items,
                           List<T2> options2Items,
                           List<T3> options3Items) {
        
        wheelOptions.setNPicker(options1Items, options2Items, options3Items);
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
            optionsSelectListener.onOptionsSelect(this, optionsCurrentItems[0], optionsCurrentItems[1], optionsCurrentItems[2]);
        }
        
        if (optionsSelectListener2 != null) {
            int[] optionsCurrentItems = wheelOptions.getCurrentItems();
            optionsSelectListener2.onOptionsSelect(this, null, optionsCurrentItems[0], optionsCurrentItems[1], optionsCurrentItems[2]);
        }
    }
    
    /**
     * 用户选中数据后调用回调
     */
    @Override
    public void returnData(View view) {
        if (optionsSelectListener != null) {
            int[] optionsCurrentItems = wheelOptions.getCurrentItems();
            optionsSelectListener.onOptionsSelect(this, optionsCurrentItems[0], optionsCurrentItems[1], optionsCurrentItems[2]);
        }
        
        if (optionsSelectListener2 != null) {
            int[] optionsCurrentItems = wheelOptions.getCurrentItems();
            optionsSelectListener2.onOptionsSelect(this, view, optionsCurrentItems[0], optionsCurrentItems[1], optionsCurrentItems[2]);
        }
    }
    
    @Override
    public boolean isDialog() {
        return isDialog;
    }
    
    /**
     * 获取数据
     *
     * @param index 数据在选择器位置
     */
    public <T> List<T> getData(int index) {
        return wheelOptions.getData(index);
    }
    
    /**
     * 选中监听
     */
    public interface OnOptionsSelectListener {
        @SuppressWarnings("rawtypes")
        void onOptionsSelect(@NonNull LinkageOptionsPickerView pickerView, int options1, int options2, int options3);
    }
    
    /**
     * 选中监听
     */
    public interface OnOptionsSelectListener2 {
        /**
         * @param view 用户使用{@link BasePickerView#returnData(View)}时传递的参数
         */
        @SuppressWarnings("rawtypes")
        void onOptionsSelect(@NonNull LinkageOptionsPickerView pickerView, @Nullable View view, int options1, int options2, int options3);
    }
}
