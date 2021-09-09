package com.bigkoo.pickerview.listener;

import android.view.View;

import com.bigkoo.pickerview.view.BasePickerView;

/**
 * 自定义布局回调
 */
public interface CustomLayoutCallback {
    /**
     * 初始化自定义布局
     *
     * @param rootView 自定义View根布局
     */
    void initLayout(BasePickerView pickerView, View rootView);
}
