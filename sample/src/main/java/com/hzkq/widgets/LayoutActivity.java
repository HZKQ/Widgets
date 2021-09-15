package com.hzkq.widgets;

import android.os.Bundle;

import androidx.annotation.Nullable;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;

/**
 * @author Created by 汪高皖 on 2020/1/17 17:26
 */
public class LayoutActivity extends BaseActivity {
    @Override
    public int getLayoutId() {
        return R.layout.activity_layout;
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
}
