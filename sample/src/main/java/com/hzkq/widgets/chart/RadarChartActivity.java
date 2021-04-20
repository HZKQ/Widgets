package com.hzkq.widgets.chart;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.custom.radarchart.DrawBgMode;
import com.github.mikephil.charting.custom.radarchart.ISmartRadarDataSet;
import com.github.mikephil.charting.custom.radarchart.SmartRadarChart;
import com.github.mikephil.charting.custom.radarchart.SmartRadarData;
import com.github.mikephil.charting.custom.radarchart.SmartRadarDataSet;
import com.github.mikephil.charting.custom.radarchart.SmartRadarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.hzkq.widgets.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;

/**
 * 雷达图
 *
 * @author Created by 汪高皖 on 2020/1/4 10:26
 */
public class RadarChartActivity extends BaseActivity {
    
    private TitleBar mTitleBar;
    private SmartRadarChart mRadarChart;
    private SmartRadarChart mRadarChart2;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_radar_chart;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mRadarChart = findViewById(R.id.radar_chart1);
        mRadarChart2 = findViewById(R.id.radar_chart2);
        initLineChart1();
        initLineChart2();
    }
    
    @Override
    public void initData() {
        fillLineChart1Data();
        fillLineChart2Data();
    }
    
    @Override
    public void initEvent() {
        mTitleBar.getLlLeft().setOnClickListener(v -> closeAct());
        
    }
    
    private void initLineChart1() {
        mRadarChart.setBackgroundColor(Color.rgb(0, 15, 67));
        
        mRadarChart.getDescription().setEnabled(false);
        
        mRadarChart.setExtraTopOffsetInPx(60);
        mRadarChart.setWebLineWidth(1f);
        mRadarChart.setWebColor(Color.LTGRAY);
        mRadarChart.setWebLineWidthInner(1f);
        mRadarChart.setWebColorInner(Color.LTGRAY);
        mRadarChart.setWebAlpha(100);
        
        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MarkerView mv = new RadarMarkerView(this, R.layout.radar_markerview);
        mv.setChartView(mRadarChart); // For bounds control
        mRadarChart.setMarker(mv); // Set the marker to the chart
        
        XAxis xAxis = mRadarChart.getXAxis();
        xAxis.setTextSize(9f);
        xAxis.setYOffset(0);
        xAxis.setXOffset(3f);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] mActivities = new String[]{"Burger", "Steak", "Salad", "Pasta", "Pizza"};
            
            @Override
            public String getFormattedValue(float value) {
                return mActivities[(int) value % mActivities.length];
            }
        });
        xAxis.setTextColor(Color.WHITE);
        
        YAxis yAxis = mRadarChart.getYAxis();
        yAxis.setLabelCount(5, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(80f);
        yAxis.setDrawLabels(false);
        yAxis.setDrawAxisLine(false);
        
        Legend l = mRadarChart.getLegend();
        l.setEnabled(false);
    }
    
    private void initLineChart2() {
        mRadarChart2.setBackgroundColor(Color.rgb(0, 15, 67));
        mRadarChart2.getDescription().setEnabled(false);
        
        mRadarChart2.setExtraTopOffsetInPx(60);
        mRadarChart2.setWebLineWidth(1f);
        mRadarChart2.setWebColor(Color.LTGRAY);
        mRadarChart2.setWebLineWidthInner(1f);
        mRadarChart2.setWebColorInner(Color.LTGRAY);
        mRadarChart2.setWebAlpha(100);
        
        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MarkerView mv = new RadarMarkerView(this, R.layout.radar_markerview);
        mv.setChartView(mRadarChart2); // For bounds control
        mRadarChart2.setMarker(mv); // Set the marker to the chart
        
        XAxis xAxis = mRadarChart2.getXAxis();
        xAxis.setTextSize(9f);
        xAxis.setYOffset(0f);
        xAxis.setXOffset(3f);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] mActivities = new String[]{"Burger", "Steak", "Salad", "Pasta", "Pizza"};
            
            @Override
            public String getFormattedValue(float value) {
                return mActivities[(int) value % mActivities.length];
            }
        });
        xAxis.setTextColor(Color.WHITE);
        
        YAxis yAxis = mRadarChart2.getYAxis();
        yAxis.setLabelCount(5, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(80f);
        yAxis.setDrawLabels(false);
        yAxis.setDrawAxisLine(false);
        
        Legend l = mRadarChart2.getLegend();
        l.setEnabled(false);
    }
    
    /**
     * 设置图表数据
     */
    private void fillLineChart1Data() {
        float mul = 80;
        float min = 20;
        int cnt = 5;
        
        ArrayList<SmartRadarEntry> entries1 = new ArrayList<>();
        
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < cnt; i++) {
            float val1 = (float) (Math.random() * mul) + min;
            entries1.add(new SmartRadarEntry(val1));
        }
        
        SmartRadarDataSet set1 = new SmartRadarDataSet(entries1, "Last Week");
        set1.setColor(0xff498FF5);
        set1.setFillColor(0xff498FF5);
        set1.setDrawFilled(true);
        set1.setFillAlpha(180);
        set1.setLineWidth(2f);
        set1.setDrawHighlightCircleEnabled(true);
        set1.setDrawHighlightIndicators(false);
        
        ArrayList<ISmartRadarDataSet> sets = new ArrayList<>();
        sets.add(set1);
        
        SmartRadarData data = new SmartRadarData(sets);
        data.setValueTextSize(8f);
        data.setDrawValues(false);
        data.setValueTextColor(Color.WHITE);
        data.setDrawBgMode(DrawBgMode.POLYGON);
        data.setDrawBg(true);
        data.setBgDrawable(getResources().getDrawable(R.drawable.bg_circle_blue_to_light_blue));
        
        mRadarChart.setData(data);
        mRadarChart.invalidate();
        mRadarChart.animateXY(1400, 1400, Easing.EaseInOutQuad);
    }
    
    /**
     * 设置图表数据
     */
    private void fillLineChart2Data() {
        float mul = 80;
        float min = 20;
        int cnt = 5;
        
        ArrayList<SmartRadarEntry> entries1 = new ArrayList<>();
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < cnt; i++) {
            float val1 = (float) (Math.random() * mul) + min;
            entries1.add(new SmartRadarEntry(val1));
        }
        
        SmartRadarDataSet set1 = new SmartRadarDataSet(entries1, "Last Week");
        set1.setColor(0xff498FF5);
        set1.setFillColor(0xff498FF5);
        set1.setDrawFilled(true);
        set1.setFillAlpha(180);
        set1.setLineWidth(2f);
        set1.setDrawHighlightCircleEnabled(true);
        set1.setDrawHighlightIndicators(false);
        
        ArrayList<ISmartRadarDataSet> sets = new ArrayList<>();
        sets.add(set1);
        
        SmartRadarData data = new SmartRadarData(sets);
        data.setValueTextSize(8f);
        data.setDrawValues(false);
        data.setValueTextColor(Color.WHITE);
        data.setDrawBgMode(DrawBgMode.CIRCULAR);
        data.setDrawBgRadiusLine(false);
        data.setDrawBg(true);
        data.setBgDrawable(getResources().getDrawable(R.drawable.bg_circle_blue_to_light_blue));
        
        mRadarChart2.setData(data);
        mRadarChart2.invalidate();
        mRadarChart2.animateXY(1400, 1400, Easing.EaseInOutQuad);
    }
    
    /**
     * 用于在滑动报表时显示机器利用率
     */
    public static class RadarMarkerView extends MarkerView {
        
        private final TextView tvContent;
        private final DecimalFormat format = new DecimalFormat("##0");
        
        public RadarMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            
            tvContent = findViewById(R.id.tvContent);
        }
        
        // runs every time the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            tvContent.setText(String.format("%s %%", format.format(e.getY())));
            
            super.refreshContent(e, highlight);
        }
        
        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2f), -getHeight() - 10);
        }
    }
}
