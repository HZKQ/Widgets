package com.hzkq.widgets;

import android.os.Bundle;
import android.widget.TextView;

import com.keqiang.rainbowbar.RainbowBar;
import com.keqiang.rainbowbar.ScaleRainbowBar;
import com.keqiang.rainbowbar.entity.RainbowBarData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.Nullable;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;

/**
 * @author Created by 汪高皖 on 2020/1/8 11:41
 */
public class RatioColorBarActivity extends BaseActivity {
    private static final int[] COLORS = new int[]{/*黄颜色*/0xFFFFA60E,/*绿色*/0xFF0DDF66,/*红色*/0xFFFF5252,/*灰色*/0xFFCBCAD1};
    
    private TitleBar mTitleBar;
    private RainbowBar mColorBar;
    private ScaleRainbowBar mScaleColorBar;
    
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
        
        List<RainbowBarData> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            RainbowBarData data = new RainbowBarData();
            data.setColor(COLORS[Math.abs(random.nextInt()) % 4]);
            data.setValue(random.nextFloat() * 100);
            list.add(data);
        }
        mColorBar.setColorBars(list);
        
        List<RainbowBarData> list2 = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            RainbowBarData data = new RainbowBarData();
            data.setColor(COLORS[Math.abs(random.nextInt()) % 4]);
            data.setValue(random.nextFloat() * 50);
            list2.add(data);
        }
        mScaleColorBar.setColorBars(0, 50, list2);
    }
    
    @Override
    public void initEvent() {
        mTitleBar.getLlLeft().setOnClickListener(v -> closeAct());
        
        mColorBar.setOnBarSelectedChangeListener((contentView, position, selected, rainbowBarData) -> {
            if (contentView == null) {
                return;
            }
    
    
            ((TextView) contentView.findViewById(R.id.tv_title)).setText("位置" + position);
        });
    
        mScaleColorBar.setOnBarSelectedChangeListener((contentView, position, selected, rainbowBarData) -> {
            if (contentView == null) {
                return;
            }
    
            ((TextView) contentView.findViewById(R.id.tv_title)).setText("位置" + position);
        });
    }
}
