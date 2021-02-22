package com.hzkq.widgets.chart;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.custom.piechart.LabelGravity;
import com.github.mikephil.charting.custom.piechart.SmartPieChart;
import com.github.mikephil.charting.custom.piechart.SmartPieData;
import com.github.mikephil.charting.custom.piechart.SmartPieDataSet;
import com.github.mikephil.charting.custom.piechart.SmartPieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.hzkq.widgets.R;

import java.util.ArrayList;

import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;
import me.zhouzhuo810.magpiex.utils.ScreenAdapterUtil;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * @author Created by 汪高皖 on 2019/11/13 16:49
 */
public class PieChartActivity extends BaseActivity {
    protected final String[] parties = new String[]{
        "Party A", "Party B", "Party C", "Party D", "Party E", "Party F", "Party G", "Party H",
        "Party I", "Party J", "Party K", "Party L", "Party M", "Party N", "Party O", "Party P",
        "Party Q", "Party R", "Party S", "Party T", "Party U", "Party V", "Party W", "Party X",
        "Party Y", "Party Z"
    };
    
    private TitleBar mTitleBar;
    private SmartPieChart mPieChart;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_piechart;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mPieChart = findViewById(R.id.pie_chart);
        initPieChart();
    }
    
    @Override
    public void initData() {
        setData(4, 100);
    }
    
    @Override
    public void initEvent() {
        mTitleBar.getLlLeft().setOnClickListener(v -> closeAct());
    }
    
    private void initPieChart() {
        // 空数据提示
        mPieChart.setNoDataText("暂无数据");
        // 饼状图描述信息
        Description description = mPieChart.getDescription();
        // 设置饼状图描述信息是否展示
        description.setEnabled(true);
        description.setText("这是饼状图的描述");
        // 设置描述相当于饼状图的位置
        description.setPosition(0, 0);
        // 设置饼状图描述字体颜色
        description.setTextColor(ContextCompat.getColor(this, R.color.colorBlack70));
        // 设置饼状图描述字体大小
        description.setTextSizeInPx(SimpleUtil.getScaledValue(28));
        
        // 饼状图的位置偏移
        int offset = SimpleUtil.getScaledValue(30);
        mPieChart.setExtraOffsetsWithPixel(offset, offset, offset, offset);
        // 是否画中间的文字
        mPieChart.setDrawCenterText(true);
        // 是否画中间的洞
        mPieChart.setDrawHoleEnabled(true);
        // 中间洞的颜色
        mPieChart.setHoleColor(Color.TRANSPARENT);
        // 中间洞外边一小圈的颜色
        mPieChart.setTransparentCircleColor(Color.TRANSPARENT);
        // 中间洞外边一小圈的透明度
        mPieChart.setTransparentCircleAlpha(110);
        // 中间洞的半径
        mPieChart.setHoleRadius(70);
        // 洞外小圈的半径
        mPieChart.setTransparentCircleRadius(70);
        // 是否格式化成百分比值
        mPieChart.setUsePercentValues(true);
        // 中间绘制的文字大小
        mPieChart.setCenterTextSizePixels(SimpleUtil.getScaledValue(62));
        // 中间绘制的文字颜色
        mPieChart.setCenterTextColor(ContextCompat.getColor(this, R.color.colorBlack50));
        // 设置中间字体文字类型
        // mPieChart.setCenterTextTypeface(FontUtils.getDINBoldFont());
        // 是否绘制标签内容
        mPieChart.setDrawEntryLabels(true);
        
        // 饼状图是否绘制边框
        mPieChart.setDrawBorder(true);
        // 饼状图边框颜色
        mPieChart.setBorderColor(ContextCompat.getColor(this, R.color.colorWhite70));
        // 饼状图边框宽度
        mPieChart.setBorderWidth(SimpleUtil.getScaledValue(5));
        // 饼状图边框距离饼状图的偏移值
        mPieChart.setBorderOffset(SimpleUtil.getScaledValue(5));
        // 饼状图边框绘制路径样式，此处为虚实线
        mPieChart.setBorderPathEffect(new DashPathEffect(new float[]{SimpleUtil.getScaledValue(10), SimpleUtil.getScaledValue(10)}, 0));
        
        // 是否可以拖动旋转
        mPieChart.setRotationEnabled(true);
        // 是否可以触摸，置未false，则拖拽和点击则不可用
        mPieChart.setTouchEnabled(true);
        // 是否可用，置未false，只是禁止掉用户与饼状图的所有交互，饼状图依然会绘制
        mPieChart.setEnabled(true);

        // 是否绘制标签
        mPieChart.setDrawEntryLabels(true);
    
        // 饼状图各模块之间的描述
        Legend legend = mPieChart.getLegend();
        // 描述是否可用
        legend.setEnabled(true);
        legend.setDrawInside(false);
        // 设置各描述文本之间的横向间隔,当描述为横向排列时
        legend.setXEntrySpace(7);
        // 设置各描述文本之间的纵向间隔,当描述为纵向排列时
        legend.setYEntrySpace(0f);
        legend.setYOffsetInPx(0f);
        // 设置各描述之间怎么排列，横向和纵向
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
    
        // 横向和纵向起始位置共同决定描述在饼状图中的位置
        // 设置描述横向起始位置
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        // 设置描述纵向起始位置
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
    }
    
    private void setData(int count, float range) {
        ArrayList<SmartPieEntry> entries = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            SmartPieEntry smartPieEntry = new SmartPieEntry((float) ((Math.random() * range) + range / 5), parties[i % parties.length]);
            smartPieEntry.setYSecondValue(i);
            entries.add(smartPieEntry);
        }
        
        SmartPieDataSet dataSet = new SmartPieDataSet(entries, "测试数据");
        // 每个扇形图像之间的间隔
        dataSet.setSliceSpace(SimpleUtil.getScaledValue(5));
        // 设置扇形图点击选中后放大比原来扇形图宽度增加值
        dataSet.setSelectionShiftWithPixcel(SimpleUtil.getScaledValue(5));
        
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);
        
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        
        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);
        
        //是否画值
        dataSet.setDrawValues(true);
        // 是否绘制选中扇形图的y轴值
        dataSet.setDrawHighlightYValue(true);
        // 设置绘制值的字体大小
        dataSet.setValueTextSizeInPx(ScreenAdapterUtil.getInstance().getScaledValue(26));
        // 设置绘制值的颜色值
        dataSet.setValueTextColors(colors);
        // 设置绘制值的位置，顶部，底部或尾部
        dataSet.setYGravity(LabelGravity.TOP);
        // 设置是否绘制第二个数值
        dataSet.setDrawYSecondValue(true);
        // 设置绘制第二个数值颜色
        dataSet.setYSecondValueTextColors(colors);
        // 设置绘制第二个数值字体大小
        dataSet.setYSecondValueTextSize(ScreenAdapterUtil.getInstance().getScaledValue(26));
        
        
        // 设置绘制值引线颜色
        dataSet.setValueLineColors(colors);
        // 设置绘制值引线第一部分长度，基于饼状图半径
        dataSet.setValueLinePart1Length(0.3f);
        // 设置绘制值引线第二部分长度，基于饼状图半径
        dataSet.setValueLinePart2Length(1.1f);
        // 设置绘制值引线第二部分长度是否跟随文本宽度，如果为true，则dataSet.setValueLinePart2Length无效
        dataSet.setValueLinePart2LengthFollowTextWidth(true);
        // 设置y轴值绘制的位置，饼状图内部和外部，内部则不绘制引线
        dataSet.setYValuePosition(SmartPieDataSet.ValuePosition.OUTSIDE_SLICE);
        // 设置绘制值引线第一部分距离饼状图的偏移值，100f则是饼状图的最外面，<100则往里，>100往外
        dataSet.setValueLinePart1OffsetPercentage(100f);
        
        SmartPieData data = new SmartPieData(dataSet);
        data.setHighlightEnabled(true);
        mPieChart.setData(data);
        mPieChart.invalidate();
    }
}
