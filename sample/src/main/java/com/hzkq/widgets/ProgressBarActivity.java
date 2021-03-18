package com.hzkq.widgets;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hzkq.widgets.progress.HProgressBarActivity;
import com.hzkq.widgets.progress.RingProgressBarActivity;

import androidx.annotation.Nullable;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;

/**
 * @author Created by 汪高皖 on 2020/1/17 17:26
 */
public class ProgressBarActivity extends BaseActivity {
    @Override
    public int getLayoutId() {
        return R.layout.activity_progress_bar;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
    
    }
    
    @Override
    public void initData() {
    
    }
    
    @Override
    public void initEvent() {
    
    }
    
    public void onHProgressBarClick(View view) {
        startActWithIntent(new Intent(this, HProgressBarActivity.class));
    }
    
    public void onRingProgressBarClick(View view) {
        startActWithIntent(new Intent(this, RingProgressBarActivity.class));
    }
}
