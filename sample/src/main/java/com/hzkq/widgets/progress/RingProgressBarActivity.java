package com.hzkq.widgets.progress;

import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.hzkq.widgets.R;
import com.keqiang.progressbar.RingProgress;

import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;

/**
 * 圆环进度条
 *
 * @author Created by 汪高皖 on 2020/1/17 16:20
 */
public class RingProgressBarActivity extends BaseActivity {
    
    private TitleBar mTitleBar;
    private RingProgress mRb1;
    private RingProgress mRb2;
    private TextView mTvStartAnim;
    private RingProgress mRb3;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_ring_progress_bar;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mRb1 = findViewById(R.id.rb1);
        mRb2 = findViewById(R.id.rb2);
        mTvStartAnim = findViewById(R.id.tv_start_anim);
        mRb3 = findViewById(R.id.rb3);
    }
    
    @Override
    public void initData() {
        mRb2.setBorderPathEffect(new DashPathEffect(new float[]{20, 10}, 0));
        
        mRb3.setRingColors2(ContextCompat.getColor(this, R.color.text_blue),
            ContextCompat.getColor(this, R.color.text_green),
            ContextCompat.getColor(this, R.color.text_yellow));
    }
    
    @Override
    public void initEvent() {
        mTvStartAnim.setOnClickListener(v -> mRb3.animate(3000, progress -> mRb3.setCenterText((int) progress + "%")));
    }
}
