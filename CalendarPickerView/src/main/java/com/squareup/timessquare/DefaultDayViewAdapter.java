package com.squareup.timessquare;

import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.widget.TextView;

import me.zhouzhuo810.magpiex.utils.SimpleUtil;


public class DefaultDayViewAdapter implements DayViewAdapter {
    @Override
    public void makeCellView(CalendarCellView parent) {
        TextView textView = new TextView(
            new ContextThemeWrapper(parent.getContext(), R.style.CalendarCell_CalendarDate));
        textView.setDuplicateParentStateEnabled(true);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, SimpleUtil.getScaledValue(36, true));
        parent.addView(textView);
        parent.setDayOfMonthTextView(textView);
    }
}
