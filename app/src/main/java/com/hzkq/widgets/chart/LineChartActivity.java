package com.hzkq.widgets.chart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.custom.linechart.ISmartLineDataSet;
import com.github.mikephil.charting.custom.linechart.SmartLineChart;
import com.github.mikephil.charting.custom.linechart.SmartLineData;
import com.github.mikephil.charting.custom.linechart.SmartLineDataSet;
import com.github.mikephil.charting.custom.linechart.SmartLineEntry;
import com.github.mikephil.charting.custom.linechart.SmartXAxis;
import com.github.mikephil.charting.custom.linechart.SmartYAxis;
import com.github.mikephil.charting.custom.linechart.XLabelRetractType;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.hzkq.widgets.R;

import java.util.ArrayList;
import java.util.List;

import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;
import me.zhouzhuo810.magpiex.utils.ScreenAdapterUtil;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * @author Created by 汪高皖 on 2020/1/4 10:26
 */
public class LineChartActivity extends BaseActivity {
    
    private TitleBar mTitleBar;
    private SmartLineChart mLineChart1;
    private SmartLineChart mLineChart2;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_linechart;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mLineChart1 = findViewById(R.id.line_chart1);
        mLineChart2 = findViewById(R.id.line_chart2);
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
        int color = 0x80333333;
        int lineColor = 0xFFE7EAEE;
        
        MyMarkView myMarkView = new MyMarkView(this);
        myMarkView.setChartView(mLineChart1);
        mLineChart1.setMarker(myMarkView);
        mLineChart1.setExtraBottomOffsetInPx(SimpleUtil.getScaledValue(40));
        
        SmartXAxis xAxis = mLineChart1.getXAxis();
        // x轴线条颜色
        xAxis.setAxisLineColor(lineColor);
        // x轴线条宽度
        xAxis.setAxisLineWidthInPx(2);
        // x轴文本颜色
        xAxis.setTextColor(color);
        // x轴字体大小
        xAxis.setTextSizeInPx(SimpleUtil.getScaledValue(20));
        // x轴是否绘制纵向指示线
        xAxis.setDrawGridLines(false);
        // x轴最小值
        xAxis.setAxisMinimum(0);
        // 是否自己计算Y轴每个标签的间隔值
        xAxis.setCustomCalculateXOffset(true);
        // 设置x轴边界Label内容缩进类型
        xAxis.setXLabelRetractType(XLabelRetractType.NONE);
        
        SmartYAxis yAxis = mLineChart1.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setTextColor(color);
        yAxis.setTextSizeInPx(SimpleUtil.getScaledValue(20));
        yAxis.setAxisLineColor(lineColor);
        yAxis.setAxisLineWidthInPx(2);
        yAxis.setGridColor(lineColor);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(100);
        yAxis.setLabelCount(5, true);
    }
    
    private void initLineChart2() {
        int color = 0x80333333;
        int lineColor = 0xFFE7EAEE;
        
        mLineChart2.setTouchEnabled(false);
        mLineChart2.setDragEnabled(false);
        mLineChart2.setExtraBottomOffsetInPx(SimpleUtil.getScaledValue(40));
        
        SmartXAxis xAxis = mLineChart2.getXAxis();
        xAxis.setAxisLineColor(lineColor);
        xAxis.setAxisLineWidth(1);
        xAxis.setTextColor(color);
        xAxis.setTextSize(12);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(0);
        xAxis.setCustomCalculateXOffset(true);
        xAxis.setXLabelRetractType(XLabelRetractType.NONE);
        
        SmartYAxis yAxis = mLineChart2.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setTextColor(color);
        yAxis.setTextSize(12);
        yAxis.setAxisLineColor(lineColor);
        yAxis.setAxisLineWidth(1);
        yAxis.setGridColor(lineColor);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(100);
        yAxis.setLabelCount(5, true);
    }
    
    /**
     * 设置图表数据
     */
    private void fillLineChart1Data() {
        List<SmartLineEntry> values = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            values.add(new SmartLineEntry(i, (int) (Math.random() * 100)));
        }
        
        int size = values.size();
        mLineChart1.getXAxis().setAxisMaximum(size);
        mLineChart1.getXAxis().setLabelCount(size);
    
        mLineChart1.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= values.size()) {
                    return "";
                }
            
                return (index + 1) + "月";
            }
        });
        
        SmartLineDataSet set;
        if (mLineChart1.getData() != null &&
            mLineChart1.getData().getDataSetCount() > 0) {
            set = (SmartLineDataSet) mLineChart1.getData().getDataSetByIndex(0);
            set.setValues(values);
        } else {
            int blue = 0xff3180F6;
            set = new SmartLineDataSet(values, "利用率");
            set.setDrawIcons(false);
            set.setDrawCircles(true);
            set.setMode(LineDataSet.Mode.LINEAR);
            set.setColor(blue);
            set.setLineWidthInPx(SimpleUtil.getScaledValue(6));
            set.setCircleRadiusInPx(SimpleUtil.getScaledValue(8));
            set.setCircleColor(blue);
            set.setValueTextSizeInPx(SimpleUtil.getScaledValue(20));
            set.setDrawFilled(true);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                set.setFillDrawable(getDrawable(R.drawable.bg_line_chart));
            } else {
                set.setFillColor(0xffDDEAFD);
            }
            
            set.setDrawValues(false);
            // 设置选中高亮线条颜色
            set.setHighLightColor(blue);
            // 当选中某个节点值时，是否绘制横向指示线
            set.setDrawHorizontalHighlightIndicator(false);
            // 设置纵向指示线是否是间断的
            set.enableDashedHighlightLine(ScreenAdapterUtil.getInstance().getScaledValue(10),
                ScreenAdapterUtil.getInstance().getScaledValue(5), 0);
            // 设置纵向指示线是否超出所点击节点的最高点
            set.setOverVerticalHighlightIndicatorEnd(false);
        }
        
        if (mLineChart1.getData() != null && mLineChart1.getData().getDataSetCount() > 0) {
            mLineChart1.getData().notifyDataChanged();
            mLineChart1.notifyDataSetChanged();
        } else {
            ArrayList<ISmartLineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set);
            SmartLineData lineData = new SmartLineData(dataSets);
            mLineChart1.setData(lineData);
        }
        mLineChart1.invalidate();
    }
    
    /**
     * 设置图表数据
     */
    private void fillLineChart2Data() {
        List<SmartLineEntry> values = new ArrayList<>();
        SmartLineEntry entry = new SmartLineEntry(0, 30);
        values.add(entry);
        
        entry = new SmartLineEntry(1, 50);
        values.add(entry);
        
        entry = new SmartLineEntry(2, 40);
        entry.setDraw(false);
        values.add(entry);
        
        entry = new SmartLineEntry(3, 60);
        entry.setDraw(false);
        values.add(entry);
        
        entry = new SmartLineEntry(4, 55);
        values.add(entry);
        
        entry = new SmartLineEntry(5, 20);
        values.add(entry);
        
        entry = new SmartLineEntry(6, 45);
        values.add(entry);
        
        
        int size = values.size();
        mLineChart2.getXAxis().setAxisMaximum(size);
        mLineChart2.getXAxis().setLabelCount(size);
    
        mLineChart2.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= values.size()) {
                    return "";
                }
                
                return (index + 1) + "月";
            }
        });
        
        SmartLineDataSet set;
        if (mLineChart2.getData() != null &&
            mLineChart2.getData().getDataSetCount() > 0) {
            set = (SmartLineDataSet) mLineChart2.getData().getDataSetByIndex(0);
            set.setValues(values);
        } else {
            int blue = 0xff3180F6;
            set = new SmartLineDataSet(values, "利用率");
            set.setDrawIcons(false);
            set.setDrawCircles(true);
            set.setMode(LineDataSet.Mode.LINEAR);
            set.setColor(blue);
            set.setLineWidth(2f);
            set.setCircleRadius(3f);
            set.setCircleColor(blue);
            set.setValueTextSize(9f);
            set.setDrawFilled(true);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                set.setFillDrawable(getDrawable(R.drawable.bg_line_chart));
            } else {
                set.setFillColor(0xffDDEAFD);
            }
            
            set.setDrawValues(true);
        }
        
        if (mLineChart2.getData() != null && mLineChart2.getData().getDataSetCount() > 0) {
            mLineChart2.getData().notifyDataChanged();
            mLineChart2.notifyDataSetChanged();
        } else {
            ArrayList<ISmartLineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set);
            SmartLineData lineData = new SmartLineData(dataSets);
            mLineChart2.setData(lineData);
        }
        mLineChart2.invalidate();
    }
    
    /**
     * 用于在滑动报表时显示机器利用率
     */
    private static class MyMarkView extends MarkerView {
        private TextView value;
        private MPPointF offset;
        
        public MyMarkView(Context context) {
            super(context, R.layout.mark_view_device_use_rate);
            ScreenAdapterUtil.getInstance().loadView(getRootView());
            value = getRootView().findViewById(R.id.tv_use_rate);
            offset = new MPPointF();
            offset.x = -ScreenAdapterUtil.getInstance().getScaledValue(60);
            offset.y = -ScreenAdapterUtil.getInstance().getScaledValue(60);
        }
        
        @SuppressLint("SetTextI18n")
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            value.setText(e.getY() + "%");
            super.refreshContent(e, highlight);
        }
        
        @Override
        public MPPointF getOffset() {
            return offset;
        }
    }
    
    public static class UtilizationSevenDayEntity {
        /**
         * dayDate : 日期（格式为M/d）
         * utilizationRatio : 利用率
         */
        
        private String dayDate;
        private float utilizationRatio;
        
        public String getDayDate() { return dayDate;}
        
        public void setDayDate(String dayDate) { this.dayDate = dayDate;}
        
        public float getUtilizationRatio() { return utilizationRatio;}
        
        public void setUtilizationRatio(float utilizationRatio) { this.utilizationRatio = utilizationRatio;}
    }
}
