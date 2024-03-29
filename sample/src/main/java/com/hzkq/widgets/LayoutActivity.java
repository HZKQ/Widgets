package com.hzkq.widgets;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;

/**
 * @author Created by 汪高皖 on 2020/1/17 17:26
 */
public class LayoutActivity extends BaseActivity {
    private TitleBar mTitleBar;
    
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
        mTitleBar = findViewById(R.id.title_bar);
    }
    
    @Override
    public void initData() {
    
    }
    
    @Override
    public void initEvent() {
        mTitleBar.getLlLeft().setOnClickListener(v -> closeAct());
    }
    
    public void onClickCombinationLayout(View view) {
        startActWithIntent(new Intent(this, CombinationLayoutActivity.class));
    }
}
