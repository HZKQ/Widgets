package com.hzkq.widgets;

import android.os.Bundle;

import com.keqiang.seekbar.OneWaySeekBar;
import com.keqiang.seekbar.TwoWaySeekBar;
import com.keqiang.seekbar.TwoWaySeekBar.OnSeekBarMultiChangeListener;

import androidx.annotation.Nullable;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;
import me.zhouzhuo810.magpiex.utils.ToastUtil;

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
        mTwoWaySeekbar.setProgressStart(50);
        mTwoWaySeekbar.setProgressEnd(100);
        
        mOneWaySeekbar.setProgress(50);
    }
    
    @Override
    public void initEvent() {
        mTitleBar.getLlLeft().setOnClickListener(v -> closeAct());
        
        mTwoWaySeekbar.setOnSeekBarMultiChangeListener(new OnSeekBarMultiChangeListener() {
            @Override
            public void onProgressChanged(TwoWaySeekBar seekBar, int progressStart, int progressEnd, boolean fromUser) {
            
            }
            
            @Override
            public void onStartTrackingTouch(TwoWaySeekBar seekBar, boolean isStartThumb) {
                ToastUtil.showToast(isStartThumb ? "滑动左滑块" : "滑动右滑块");
            }
            
            @Override
            public void onStopTrackingTouch(TwoWaySeekBar seekBar) {
                ToastUtil.showToast("停止滑动");
            }
        });
        
        mOneWaySeekbar.setOnSeekBarMultiChangeListener(new OneWaySeekBar.OnSeekBarMultiChangeListener() {
            @Override
            public void onProgressChanged(OneWaySeekBar seekBar, int progress, boolean fromUser) {
            
            }
            
            @Override
            public void onStartTrackingTouch(OneWaySeekBar seekBar) {
                ToastUtil.showToast("滑动滑块");
            }
            
            @Override
            public void onStopTrackingTouch(OneWaySeekBar seekBar) {
                ToastUtil.showToast("停止滑动");
            }
        });
    }
}
