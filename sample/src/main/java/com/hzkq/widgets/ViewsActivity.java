package com.hzkq.widgets;

import android.os.Bundle;

import com.keqiang.views.ExtendEditText;

import androidx.annotation.Nullable;

import me.zhouzhuo810.magpiex.ui.act.BaseActivity;

/**
 * @author Created by 汪高皖 on 2020/1/14 15:47
 */
public class ViewsActivity extends BaseActivity {
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_views;
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
        ExtendEditText editText = findViewById(R.id.et_test);
        findViewById(R.id.rl_test).setOnClickListener(v -> {
            editText.setEnabled(!editText.isEnabled());
        });
    }
}
