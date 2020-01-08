package com.hzkq.widgets;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.keqiang.ratiocolorbar.RatioColorBar;
import com.keqiang.ratiocolorbar.ScaleRatioColorBar;
import com.keqiang.ratiocolorbar.entity.RatioBarData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;

/**
 * @author Created by 汪高皖 on 2020/1/8 11:41
 */
public class RatioColorBarActivity extends BaseActivity {
    private static final int[] COLORS = new int[]{/*黄颜色*/0xFFFFA60E,/*绿色*/0xFF0DDF66,/*红色*/0xFFFF5252,/*灰色*/0xFFCBCAD1};
    
    private TitleBar mTitleBar;
    private RatioColorBar mColorBar;
    private ScaleRatioColorBar mScaleColorBar;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_ratio_color_bar;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mColorBar = findViewById(R.id.color_bar);
        mScaleColorBar = findViewById(R.id.scale_color_bar);
    }
    
    @Override
    public void initData() {
        mColorBar.setContentView(R.layout.view_color_bar_content);
        
        mScaleColorBar.setContentView(R.layout.view_color_bar_content);
        
        List<RatioBarData> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            RatioBarData data = new RatioBarData();
            data.setColor(COLORS[Math.abs(random.nextInt()) % 4]);
            data.setValue(random.nextFloat() * 100);
            list.add(data);
        }
        mColorBar.setColorBars(list);
        
        List<RatioBarData> list2 = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            RatioBarData data = new RatioBarData();
            data.setColor(COLORS[Math.abs(random.nextInt()) % 4]);
            data.setValue(random.nextFloat() * 50);
            list2.add(data);
        }
        mScaleColorBar.setColorBars(0, 50, list2);
    }
    
    @Override
    public void initEvent() {
        mTitleBar.getLlLeft().setOnClickListener(v -> closeAct());
        
        mColorBar.setOnBarSelectedChangeListener((contentView, position, selected, ratioBarData) -> {
            if (contentView == null) {
                return;
            }
    
    
            ((TextView) contentView.findViewById(R.id.tv_title)).setText("位置" + position);
        });
    
        mScaleColorBar.setOnBarSelectedChangeListener((contentView, position, selected, ratioBarData) -> {
            if (contentView == null) {
                return;
            }
    
            ((TextView) contentView.findViewById(R.id.tv_title)).setText("位置" + position);
        });
    }
}
