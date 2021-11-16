package com.hzkq.widgets.chart.simple;

import android.os.Bundle;

import com.hzkq.widgets.R;
import com.keqiang.chart.line.LineChart;

import androidx.annotation.Nullable;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;

/**
 * @author Created by 汪高皖 on 2020/1/4 10:26
 */
public class LineChartActivity extends BaseActivity {
    
    private TitleBar mTitleBar;
    private LineChart mLineChart;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_simple_line_chart;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mLineChart = findViewById(R.id.line_chart);
        initLineChart1();
    }
    
    @Override
    public void initData() {
        fillLineChart1Data();
    }
    
    @Override
    public void initEvent() {
        mTitleBar.getLlLeft().setOnClickListener(v -> closeAct());
        
    }
    
    private void initLineChart1() {
    
    }
    
    
    /**
     * 设置图表数据
     */
    private void fillLineChart1Data() {
    
    }
}
