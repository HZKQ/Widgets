package com.hzkq.widgets;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.hzkq.widgets.chart.BarChartActivity;
import com.hzkq.widgets.chart.LineChartActivity;
import com.hzkq.widgets.chart.PieChartActivity;
import com.hzkq.widgets.chart.RadarChartActivity;

import androidx.annotation.Nullable;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;

/**
 * 图表
 *
 * @author Created by 汪高皖 on 2019/11/13 16:34
 */
public class MPChartActivity extends BaseActivity {
    @Override
    public int getLayoutId() {
        return R.layout.activity_mpchart;
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
    
    public void onPieChartClick(View view) {
        startActWithIntent(new Intent(this, PieChartActivity.class));
    }
    
    public void onLineChartClick(View view) {
        startActWithIntent(new Intent(this, LineChartActivity.class));
    }
    
    public void onBarChartClick(View view) {
        startActWithIntent(new Intent(this, BarChartActivity.class));
    }
    
    public void onRadarChartClick(View view) {
        startActWithIntent(new Intent(this, RadarChartActivity.class));
    }
    
    public void onMoreChartClick(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://github.com/PhilJay/MPAndroidChart"));
        startActivity(i);
    }
}
