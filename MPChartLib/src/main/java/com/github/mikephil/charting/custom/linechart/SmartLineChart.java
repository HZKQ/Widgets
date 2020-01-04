package com.github.mikephil.charting.custom.linechart;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.R;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

/**
 * 折线图
 *
 * @author Created by 汪高皖 on 2018/4/11 0011 13:42
 */
public class SmartLineChart extends SmartBarLineChartBase<SmartLineData> implements SmartLineDataProvider {
    public SmartLineChart(Context context) {
        super(context);
        init2();
    }

    public SmartLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init2();
    }

    public SmartLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init2();
    }

    @Override
    protected void init() {
        super.init();
    
        mAxisLeft.setDrawGridLines(false);
        mAxisLeft.setSpaceTop(0);
        mAxisLeft.setSpaceBottom(0);
        mAxisLeft.setGranularity(1f);
        mAxisLeft.setGranularityEnabled(true);
        
        mAxisRight.setEnabled(false);
        mAxisRight.setSpaceTop(0);
        mAxisRight.setSpaceBottom(0);
        mAxisRight.setGranularity(1f);
        mAxisRight.setGranularityEnabled(true);
    
        mXAxis.setDrawGridLines(false);
        mXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //设置X轴粒度，防止放大或缩小后label标签重复
        mXAxis.setGranularity(1f);
        mXAxis.setGranularityEnabled(true);
    
        mRenderer = new SmartLineChartRenderer(this, mAnimator, mViewPortHandler);
    }

    @Override
    public SmartLineData getLineData() {
        return mData;
    }

    @Override
    public SmartTransformer getTransformer(YAxis.AxisDependency which) {
        if (which == YAxis.AxisDependency.LEFT)
            return mLeftAxisTransformer;
        else
            return mRightAxisTransformer;
    }
    
    private void init2(){
        // 此处部分赋值由于有全局初始化值，因此不能放在init()方法中，因为init()方法由最顶层父类调用，
        // 所以会比顶层父类的所有孩子类中全局变量初始化操作先执行，如果这些赋值放到init()中，随后就会被
        // 顶层父类的孩子类中的全局变量初始化值覆盖
        setNoDataText(getContext().getString(R.string.no_data_label));
        setDrawGridBackground(false);
        setDoubleTapToZoomEnabled(false);
        setScaleEnabled(false);
        setPinchZoom(false);
        setTouchEnabled(true);
        setDragEnabled(true);
        
        getDescription().setEnabled(false);
        getLegend().setEnabled(false);
    }
}
