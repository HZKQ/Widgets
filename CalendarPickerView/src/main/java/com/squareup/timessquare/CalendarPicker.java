package com.squareup.timessquare;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * 使用此类
 */
public class CalendarPicker {
    
    private Context mContext;
    private PopupWindow mCalendarPop = null;
    private CalendarPickerView mCalendarPicker;
    
    public interface OnDatePickerListener {
        boolean onDateSelected(List<Date> selectedDates, Date startDate, Date endDate);
    }
    
    public CalendarPicker(Context context) {
        this.mContext = context;
    }
    
    public void showCalendar(View viewMask, View viewAnchor, Date mStartTime, Date mEndTime, OnDatePickerListener onDatePickerListener) {
        if (mCalendarPop == null) {
            final View contentView = LayoutInflater.from(mContext).inflate(R.layout.pop_calendar, null);
            SimpleUtil.scaleView(contentView);
            
            mCalendarPicker = contentView.findViewById(R.id.calendar_view);
            Calendar nextDay = Calendar.getInstance();
            nextDay.add(Calendar.DAY_OF_YEAR, 1);
            Calendar lastYear = Calendar.getInstance();
            lastYear.add(Calendar.YEAR, -1);
            mCalendarPicker.init(lastYear.getTime(), nextDay.getTime()).inMode(CalendarPickerView.SelectionMode.RANGE);
            
            contentView.findViewById(R.id.btn_ok).setOnClickListener(v -> {
                List<Date> selectedDates = mCalendarPicker.getSelectedDates();
                Date startDate = null;
                Date endDate = null;
                if (selectedDates != null && selectedDates.size() > 0) {
                    startDate = selectedDates.get(0);
                    endDate = selectedDates.get(selectedDates.size() - 1);
                }
                if (onDatePickerListener != null) {
                    boolean notClose = onDatePickerListener.onDateSelected(selectedDates, startDate, endDate);
                    if (!notClose) {
                        closeCalendar();
                    }
                } else {
                    closeCalendar();
                }
            });
            
            mCalendarPop = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mCalendarPop.setContentView(contentView);
            // 设置弹出窗体可点击
            mCalendarPop.setFocusable(true);
            mCalendarPop.setOnDismissListener(() -> {
                if (viewMask != null) {
                    viewMask.setVisibility(View.GONE);
                }
            });
            // 设置弹出窗体的背景
            mCalendarPop.setBackgroundDrawable(new ColorDrawable());
        }
        
        if (viewMask != null) {
            viewMask.setVisibility(View.VISIBLE);
        }
        mCalendarPicker.selectRangeDate(mStartTime, mEndTime);
        if (mCalendarPop != null) {
            mCalendarPop.showAsDropDown(viewAnchor);
        }
    }
    
    public CalendarPickerView getCalendarPicker() {
        return mCalendarPicker;
    }
    
    public PopupWindow getCalendarPop() {
        return mCalendarPop;
    }
    
    public void closeCalendar() {
        if (mCalendarPop != null && mCalendarPop.isShowing()) {
            mCalendarPop.dismiss();
        }
    }
}
