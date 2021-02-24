package com.hzkq.widgets;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.keqiang.seekbar.OneWaySeekBar;
import com.keqiang.seekbar.TwoWaySeekBar;

import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;

/**
 * @author Created by 汪高皖 on 2020/1/8 16:45
 */
public class SeekBarActivity extends BaseActivity {
    
    private TitleBar mTitleBar;
    private TwoWaySeekBar mTwoWaySeekbar;
    private OneWaySeekBar mOneWaySeekbar;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_seek_bar;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mTitleBar = findViewById(R.id.title_bar);
        mTwoWaySeekbar = findViewById(R.id.two_way_seekbar);
        mOneWaySeekbar = findViewById(R.id.one_way_seekbar);
    }
    
    @Override
    public void initData() {
        mTwoWaySeekbar.setProgressLeft(50);
        mTwoWaySeekbar.setProgressRight(100);
        
        mOneWaySeekbar.setProgress(50);
    }
    
    @Override
    public void initEvent() {
        mTitleBar.getLlLeft().setOnClickListener(v -> closeAct());
    }
}
