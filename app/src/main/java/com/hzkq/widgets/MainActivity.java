package com.hzkq.widgets;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.timessquare.CalendarPicker;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.utils.DateUtil;
import me.zhouzhuo810.magpiex.utils.ToastUtil;

public class MainActivity extends BaseActivity {
    
    private CalendarPicker mCalendarPicker;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
    
    }
    
    @Override
    public void initData() {
    
    }
    
    @Override
    public void initEvent() {
    
    }
    
    /**
     * 二维码扫描
     */
    public void onZxingClick(View v) {
        new IntentIntegrator(this)
            .setCustomTitle("二维码扫描") //标题栏文字
            .setCustomTitleBg(R.color.colorPrimary)
            .setCustomTitleTextColor(R.color.colorWhite)
            .setPrompt("将扫码框对准二维码开始扫描")  //扫描框底部文字
            .setCameraId(0)  // 后置摄像头
            .setBeepEnabled(true) //是否有提示音
            .setBarcodeImageEnabled(false)
            .setAlbumScanEnabled(false) //是否启用相册扫码
            .initiateScan();
    }
    
    /**
     * 日历选择
     */
    public void onCalendarClick(View v) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        Date mStartTime = c.getTime();
        Date mEndTime = new Date();
        if (mCalendarPicker == null) {
            mCalendarPicker = new CalendarPicker(this);
        }
        mCalendarPicker.showCalendar(null, findViewById(R.id.tv_calendar_picker), mStartTime, mEndTime, new CalendarPicker.OnDatePickerListener() {
            @Override
            public boolean onDateSelected(List<Date> selectedDates, Date startDate, Date endDate) {
                ToastUtil.showToast(DateUtil.get_yMd(startDate) + " ~ " + DateUtil.get_yMd(endDate));
                return false;
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result == null || result.getContents() == null) {
            return;
        }
        String contents = result.getContents();
        ToastUtil.showToast(contents);
        
    }
    
    public void onMPChartClick(View view) {
        startActWithIntent(new Intent(this, MPChartActivity.class));
    }
    
    public void onRatioColorBarClick(View view) {
        startActWithIntent(new Intent(this, RatioColorBarActivity.class));
    }
    
    public void onIndexBarClick(View view) {
        startActWithIntent(new Intent(this, IndexBarActivity.class));
    }
    
    public void onSeekBarClick(View view) {
        startActWithIntent(new Intent(this, SeekBarActivity.class));
    }
    
    public void onPickerViewClick(View view) {
        startActWithIntent(new Intent(this, PickerViewActivity.class));
    }
    
    public void onCountdownViewClick(View view) {
        startActWithIntent(new Intent(this, CountdownActivity.class));
    }
    
    public void onViewsClick(View view) {
        startActWithIntent(new Intent(this, ViewsActivity.class));
    }
    
    public void onBreadcrumbClick(View view) {
        startActWithIntent(new Intent(this, BreadcrumbActivity.class));
    }
    
    public void onProgressBarClick(View view) {
        startActWithIntent(new Intent(this, ProgressBarActivity.class));
    }
}
