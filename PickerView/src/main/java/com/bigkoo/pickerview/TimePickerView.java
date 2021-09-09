package com.bigkoo.pickerview;

import android.content.Context;
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
import com.bigkoo.pickerview.view.WheelTime;

import java.util.Calendar;
import java.util.Date;

/**
 * 时间选择器
 * Created by Sai on 15/11/22.
 * Updated by XiaoSong on 2017-2-22.
 */
public class TimePickerView extends BasePickerView implements View.OnClickListener {
    private final int layoutRes;
    private final CustomLayoutCallback mCustomLayoutCallback;
    
    // 自定义控件
    private WheelTime wheelTime;
    // 回调接口
    private final OnTimeSelectListener timeSelectListener;
    // 内容显示位置 默认居中
    private final int gravity;
    // 显示类型
    private final boolean[] type;
    
    // 确定按钮字符串
    private final String mStrSubmit;
    // 取消按钮字符串
    private final String mStrCancel;
    // 标题字符串
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
    // 标题字体大小
    private final int mSizeTitle;
    // 内容字体大小
    private final int mSizeContent;
    // 未选中内容字体大小
    private final int mSizeOut;
    
    // 当前选中时间
    private Calendar date;
    // 开始时间
    private final Calendar startDate;
    // 终止时间
    private final Calendar endDate;
    // 开始年份
    private final int startYear;
    // 结尾年份
    private final int endYear;
    
    // 是否循环
    private final boolean cyclic;
    // 是否能取消
    private final boolean cancelable;
    // 是否只显示中间的label
    private final boolean isCenterLabel;
    // 是否显示农历
    private final boolean isLunarCalendar;
    
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
    // 是否是对话框模式
    private final boolean isDialog;
    private final String mLabelYear, mLabelMonth, mLabelDay, mLabelHours, mLabelMins, mLabelSeconds;
    private final int mXOffsetYear, mXOffsetMonth, mXOffsetDay, mXOffsetHours, mXOffsetMins, mXOffsetSeconds;
    // 分隔线类型
    private final WheelView.DividerType dividerType;
    
    private static final String TAG_SUBMIT = "submit";
    private static final String TAG_CANCEL = "cancel";
    
    // 构造方法
    public TimePickerView(Builder builder) {
        super(builder.context);
        this.timeSelectListener = builder.timeSelectListener;
        this.gravity = builder.gravity;
        this.type = builder.type;
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
        this.startYear = builder.startYear;
        this.endYear = builder.endYear;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.date = builder.date;
        this.cyclic = builder.cyclic;
        this.isCenterLabel = builder.isCenterLabel;
        this.isLunarCalendar = builder.isLunarCalendar;
        this.cancelable = builder.cancelable;
        this.mLabelYear = builder.label_year;
        this.mLabelMonth = builder.label_month;
        this.mLabelDay = builder.label_day;
        this.mLabelHours = builder.label_hours;
        this.mLabelMins = builder.label_mins;
        this.mLabelSeconds = builder.label_seconds;
        this.mXOffsetYear = builder.xoffset_year;
        this.mXOffsetMonth = builder.xoffset_month;
        this.mXOffsetDay = builder.xoffset_day;
        this.mXOffsetHours = builder.xoffset_hours;
        this.mXOffsetMins = builder.xoffset_mins;
        this.mXOffsetSeconds = builder.xoffset_seconds;
        this.textColorCenter = builder.textColorCenter;
        this.textColorOut = builder.textColorOut;
        this.dividerColor = builder.dividerColor;
        this.mCustomLayoutCallback = builder.mCustomLayoutCallback;
        this.layoutRes = builder.layoutRes;
        this.lineSpacingMultiplier = builder.lineSpacingMultiplier;
        this.isDialog = builder.isDialog;
        this.dividerType = builder.dividerType;
        this.backgroundId = builder.backgroundId;
        this.decorView = builder.decorView;
        initView(builder.context);
    }
    
    // 建造器
    public static class Builder {
        private int layoutRes = R.layout.pickerview_time;
        private CustomLayoutCallback mCustomLayoutCallback;
        private final Context context;
        private final OnTimeSelectListener timeSelectListener;
        // 显示类型 默认全部显示
        private boolean[] type = new boolean[]{true, true, true, true, true, true};
        // 内容显示位置 默认居中
        private int gravity = Gravity.CENTER;
        
        private String Str_Submit;// 确定按钮文字
        private String Str_Cancel;// 取消按钮文字
        private String Str_Title;// 标题文字
        
        private int Color_Submit;// 确定按钮颜色
        private int Color_Cancel;// 取消按钮颜色
        private int Color_Title;// 标题颜色
        
        private int Color_Background_Wheel;// 滚轮背景颜色
        private int Color_Background_Title;// 标题背景颜色
        
        private int Size_Submit_Cancel = 17;// 确定取消按钮大小
        private int Size_Title = 18;// 标题字体大小
        private int Size_Content = 12;// 内容字体大小
        private int Size_Out = 12;// 未选中内容字体大小
        private Calendar date;// 当前选中时间
        private Calendar startDate;// 开始时间
        private Calendar endDate;// 终止时间
        private int startYear;// 开始年份
        private int endYear;// 结尾年份
        
        private boolean cyclic = true;// 是否循环
        private boolean cancelable = true;// 是否能取消
        
        private boolean isCenterLabel = true;// 是否只显示中间的label
        private boolean isLunarCalendar = false;// 是否显示农历
        public ViewGroup decorView;// 显示pickerview的根View,默认是activity的根view
        
        private int textColorOut; // 分割线以外的文字颜色
        private int textColorCenter; // 分割线之间的文字颜色
        private int dividerColor; // 分割线的颜色
        private int backgroundId; // 显示时的外部背景色颜色,默认是灰色
        private WheelView.DividerType dividerType;//分隔线类型
        // 条目间距倍数 默认1.6
        private float lineSpacingMultiplier = 1.6F;
        
        private boolean isDialog;// 是否是对话框模式
        
        private String label_year, label_month, label_day, label_hours, label_mins, label_seconds;//单位
        private int xoffset_year, xoffset_month, xoffset_day, xoffset_hours, xoffset_mins, xoffset_seconds;//单位
        
        // Required
        public Builder(Context context, OnTimeSelectListener listener) {
            this.context = context;
            this.timeSelectListener = listener;
        }
        
        // Option
        public Builder setType(boolean[] type) {
            this.type = type;
            return this;
        }
        
        public Builder gravity(int gravity) {
            this.gravity = gravity;
            return this;
        }
        
        public Builder setSubmitText(String Str_Submit) {
            this.Str_Submit = Str_Submit;
            return this;
        }
        
        public Builder isDialog(boolean isDialog) {
            this.isDialog = isDialog;
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
        
        public Builder setSubmitColor(int Color_Submit) {
            this.Color_Submit = Color_Submit;
            return this;
        }
        
        public Builder setCancelColor(int Color_Cancel) {
            this.Color_Cancel = Color_Cancel;
            return this;
        }
        
        /**
         * 必须是{@link ViewGroup}
         * 设置要将PickerView显示到的容器id
         */
        public Builder setDecorView(ViewGroup decorView) {
            this.decorView = decorView;
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
        
        public Builder setContentSize(int Size_Content) {
            this.Size_Content = Size_Content;
            return this;
        }
        
        public Builder setOutSize(int size_Out) {
            this.Size_Out = size_Out;
            return this;
        }
        
        /**
         * 因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
         */
        public Builder setDate(Calendar date) {
            this.date = date;
            return this;
        }
        
        public Builder setLayoutRes(int res, CustomLayoutCallback customLayoutCallback) {
            this.layoutRes = res;
            this.mCustomLayoutCallback = customLayoutCallback;
            return this;
        }
        
        public Builder setRange(int startYear, int endYear) {
            this.startYear = startYear;
            this.endYear = endYear;
            return this;
        }
        
        /**
         * 设置起始时间
         * 因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
         */
        public Builder setRangDate(Calendar startDate, Calendar endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
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
         * //显示时的外部背景色颜色,默认是灰色
         */
        
        public Builder setBackgroundId(int backgroundId) {
            this.backgroundId = backgroundId;
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
        
        public Builder isCyclic(boolean cyclic) {
            this.cyclic = cyclic;
            return this;
        }
        
        public Builder setOutSideCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }
        
        public Builder setLunarCalendar(boolean lunarCalendar) {
            isLunarCalendar = lunarCalendar;
            return this;
        }
        
        public Builder setLabel(String label_year, String label_month, String label_day, String label_hours, String label_mins, String label_seconds) {
            this.label_year = label_year;
            this.label_month = label_month;
            this.label_day = label_day;
            this.label_hours = label_hours;
            this.label_mins = label_mins;
            this.label_seconds = label_seconds;
            return this;
        }
        
        /**
         * 设置X轴倾斜角度[ -90 , 90°]
         *
         * @param xoffset_year    年
         * @param xoffset_month   月
         * @param xoffset_day     日
         * @param xoffset_hours   时
         * @param xoffset_mins    分
         * @param xoffset_seconds 秒
         */
        public Builder setTextXOffset(int xoffset_year, int xoffset_month, int xoffset_day, int xoffset_hours, int xoffset_mins, int xoffset_seconds) {
            this.xoffset_year = xoffset_year;
            this.xoffset_month = xoffset_month;
            this.xoffset_day = xoffset_day;
            this.xoffset_hours = xoffset_hours;
            this.xoffset_mins = xoffset_mins;
            this.xoffset_seconds = xoffset_seconds;
            return this;
        }
        
        public Builder isCenterLabel(boolean isCenterLabel) {
            this.isCenterLabel = isCenterLabel;
            return this;
        }
        
        public TimePickerView build() {
            return new TimePickerView(this);
        }
    }
    
    
    private void initView(Context context) {
        setDialogOutSideCancelable(cancelable);
        initViews(backgroundId);
        init();
        initEvents();
        if (mCustomLayoutCallback == null) {
            LayoutInflater.from(context).inflate(R.layout.pickerview_time, contentContainer);
            // 标题
            TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
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
            tvTitle.setText(TextUtils.isEmpty(mStrTitle) ? "" : mStrTitle);//默认为空
            
            // 设置文字颜色
            btnSubmit.setTextColor(mColorSubmit == 0 ? pickerview_timebtn_nor : mColorSubmit);
            btnCancel.setTextColor(mColorCancel == 0 ? pickerview_timebtn_nor : mColorCancel);
            tvTitle.setTextColor(mColorTitle == 0 ? pickerview_topbar_title : mColorTitle);
            
            // 设置文字大小
            btnSubmit.setTextSize(mSizeSubmitCancel);
            btnCancel.setTextSize(mSizeSubmitCancel);
            tvTitle.setTextSize(mSizeTitle);
            RelativeLayout rv_top_bar = (RelativeLayout) findViewById(R.id.rv_topbar);
            rv_top_bar.setBackgroundColor(mColorBackgroundTitle == 0 ? pickerview_bg_topbar : mColorBackgroundTitle);
        } else {
            mCustomLayoutCallback.initLayout(this, LayoutInflater.from(context).inflate(layoutRes, contentContainer));
        }
        
        // 时间转轮 自定义控件
        LinearLayout timePickerView = (LinearLayout) findViewById(R.id.timepicker);
        
        timePickerView.setBackgroundColor(mColorBackgroundWheel == 0 ? bgColor_default : mColorBackgroundWheel);
        
        wheelTime = new WheelTime(timePickerView, type, gravity, mSizeContent, mSizeOut);
        wheelTime.setLunarCalendar(isLunarCalendar);
        
        if (startYear != 0 && endYear != 0 && startYear <= endYear) {
            setRange();
        }
        
        if (startDate != null && endDate != null) {
            if (startDate.getTimeInMillis() <= endDate.getTimeInMillis()) {
                setRangDate();
            }
        } else if (startDate != null) {
            setRangDate();
        } else if (endDate != null) {
            setRangDate();
        }
        
        setTime();
        wheelTime.setLabels(mLabelYear, mLabelMonth, mLabelDay, mLabelHours, mLabelMins, mLabelSeconds);
        wheelTime.setTextXOffset(mXOffsetYear, mXOffsetMonth, mXOffsetDay, mXOffsetHours, mXOffsetMins, mXOffsetSeconds);
        
        setOutSideCancelable(cancelable);
        wheelTime.setCyclic(cyclic);
        wheelTime.setDividerColor(dividerColor);
        wheelTime.setDividerType(dividerType);
        wheelTime.setLineSpacingMultiplier(lineSpacingMultiplier);
        wheelTime.setTextColorOut(textColorOut);
        wheelTime.setTextColorCenter(textColorCenter);
        wheelTime.isCenterLabel(isCenterLabel);
    }
    
    
    /**
     * 设置默认时间
     */
    public void setDate(Calendar date) {
        this.date = date;
        setTime();
    }
    
    /**
     * 设置可以选择的时间范围, 要在setTime之前调用才有效果
     */
    private void setRange() {
        wheelTime.setStartYear(startYear);
        wheelTime.setEndYear(endYear);
        
    }
    
    /**
     * 设置可以选择的时间范围, 要在setTime之前调用才有效果
     */
    private void setRangDate() {
        wheelTime.setRangDate(startDate, endDate);
        //如果设置了时间范围
        if (startDate != null && endDate != null) {
            //判断一下默认时间是否设置了，或者是否在起始终止时间范围内
            if (date == null || date.getTimeInMillis() < startDate.getTimeInMillis()
                || date.getTimeInMillis() > endDate.getTimeInMillis()) {
                date = startDate;
            }
        } else if (startDate != null) {
            //没有设置默认选中时间,那就拿开始时间当默认时间
            date = startDate;
        } else if (endDate != null) {
            date = endDate;
        }
    }
    
    /**
     * 设置选中时间,默认选中当前时间
     */
    private void setTime() {
        int year, month, day, hours, minute, seconds;
        
        Calendar calendar = Calendar.getInstance();
        
        if (date == null) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            hours = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
            seconds = calendar.get(Calendar.SECOND);
        } else {
            year = date.get(Calendar.YEAR);
            month = date.get(Calendar.MONTH);
            day = date.get(Calendar.DAY_OF_MONTH);
            hours = date.get(Calendar.HOUR_OF_DAY);
            minute = date.get(Calendar.MINUTE);
            seconds = date.get(Calendar.SECOND);
        }
        
        wheelTime.setPicker(year, month, day, hours, minute, seconds);
    }
    
    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag();
        if (tag.equals(TAG_SUBMIT)) {
            returnData();
        }
        dismiss();
    }
    
    /**
     * 用户选中数据后调用回调
     */
    @Override
    public void returnData() {
        Date date = wheelTime.getDate();
        if (date != null && timeSelectListener != null) {
            timeSelectListener.onTimeSelect(this, date);
        }
    }
    
    public void setLunarCalendar(boolean lunar) {
        int year, month, day, hours, minute, seconds;
        Calendar calendar = Calendar.getInstance();
        Date date = wheelTime.getDate();
        if (date != null) {
            calendar.setTime(date);
        }
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hours = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        seconds = calendar.get(Calendar.SECOND);
        
        wheelTime.setLunarCalendar(lunar);
        wheelTime.setLabels(mLabelYear, mLabelMonth, mLabelDay, mLabelHours, mLabelMins, mLabelSeconds);
        wheelTime.setPicker(year, month, day, hours, minute, seconds);
    }
    
    public boolean isLunarCalendar() {
        return wheelTime.isLunarCalendar();
    }
    
    @Override
    public boolean isDialog() {
        return isDialog;
    }
    
    public interface OnTimeSelectListener {
        /**
         * 日期被选中时调用，当选中日期为null时此方法不会被调用
         *
         * @param date 选中的日期
         */
        void onTimeSelect(TimePickerView pickerView, Date date);
    }
}
