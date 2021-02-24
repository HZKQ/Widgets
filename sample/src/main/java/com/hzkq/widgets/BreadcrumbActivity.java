package com.hzkq.widgets;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.keqiang.breadcrumb.Breadcrumb;

import me.zhouzhuo810.magpiex.ui.act.BaseActivity;

/**
 * 面包屑
 *
 * @author Created by 汪高皖 on 2020/1/15 17:23
 */
public class BreadcrumbActivity extends BaseActivity {
    
    private Breadcrumb mBreadcrumb;
    private TextView mTvAddFolder;
    private TextView mTvChangeClickColor;
    private TextView mTvChangeNormalColor;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_breadcurmb;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mBreadcrumb = findViewById(R.id.breadcrumb);
        mTvAddFolder = findViewById(R.id.tv_add_folder);
        mTvAddFolder = findViewById(R.id.tv_add_folder);
        mTvChangeClickColor = findViewById(R.id.tv_change_click_color);
        mTvChangeNormalColor = findViewById(R.id.tv_change_normal_color);
    }
    
    @Override
    public void initData() {
    
    }
    
    @Override
    public void initEvent() {
        mTvChangeClickColor.setOnClickListener(v -> mBreadcrumb.setClickableTextColor(Color.RED));
        
        mTvChangeNormalColor.setOnClickListener(v -> mBreadcrumb.setTextColor(Color.BLUE));
        
        mTvAddFolder.setOnClickListener(v -> {
            String value = String.valueOf((int) (Math.random() * 10000));
            mBreadcrumb.addFolderSpan(value, value);
        });
    }
}
