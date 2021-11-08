package com.hzkq.widgets;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hzkq.widgets.layout.ComplexColumnRowLayoutActivity;
import com.hzkq.widgets.layout.MultiLazyColumnLayoutActivity;
import com.hzkq.widgets.layout.SimpleColumnLayoutActivity;
import com.hzkq.widgets.layout.TestActivity;

import androidx.annotation.Nullable;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;

/**
 * @author Created by 汪高皖 on 2020/1/17 17:26
 */
public class CombinationLayoutActivity extends BaseActivity {
    private TitleBar mTitleBar;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_combination_layout;
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
        View view = findViewById(R.id.tv_test);
        // view.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void initEvent() {
        mTitleBar.getLlLeft().setOnClickListener(v -> closeAct());
    }
    
    public void onClickColumnLayout(View view) {
        startActWithIntent(new Intent(this, SimpleColumnLayoutActivity.class));
    }
    
    public void onClickColumnRowLayout(View view) {
        startActWithIntent(new Intent(this, ComplexColumnRowLayoutActivity.class));
    }
    
    public void onClickMultiColumnLayout(View view) {
        startActWithIntent(new Intent(this, MultiLazyColumnLayoutActivity.class));
    }
    
    public void onClickTest(View view) {
        startActWithIntent(new Intent(this, TestActivity.class));
    }
}
