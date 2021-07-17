package com.hzkq.widgets;

import android.os.Bundle;
import android.text.Editable;
import android.util.Log;

import com.keqiang.views.ChooseItemView;
import com.keqiang.views.DropItemView;
import com.keqiang.views.EditItemView;
import com.keqiang.views.ExtendEditText;
import com.keqiang.views.edittext.SimpleTextWatcher;

import androidx.annotation.Nullable;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.utils.ToastUtil;

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
        ExtendEditText etTest2 = findViewById(R.id.et_test2);
        etTest2.setNumberOverLimitListener((editText1, isDecimalOver, isIntegerOver) -> {
            if (isDecimalOver && isIntegerOver) {
                ToastUtil.showToast("整数位和小数位长度均超过限制");
            } else if (isDecimalOver) {
                ToastUtil.showToast("小数位超过" + etTest2.getDecimalLimit() + "位长度限制");
            } else if (isIntegerOver) {
                ToastUtil.showToast("整数位超过" + etTest2.getIntegerLimit() + "位长度限制");
            }
        });
        
        etTest2.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                Log.e("xxx", "2:" + s.toString());
                Log.e("xxx", "3:" + etTest2.getText().toString());
            }
        });
        
        findViewById(R.id.rl_test).setOnClickListener(v -> {
            editText.setEnabled(!editText.isEnabled());
            etTest2.setText("0001234567.987654");
        });
        
        ChooseItemView civItem = findViewById(R.id.civ_item);
        EditItemView eivItem = findViewById(R.id.eiv_item);
        civItem.setOnClickListener(v -> {
            civItem.getTvContent().setText("当前为阅读模式，不可编辑");
            civItem.setShowStyle(ChooseItemView.SHOW_STYLE_READ);
            eivItem.setShowStyle(EditItemView.SHOW_STYLE_READ);
        });
        
        
        eivItem.setOnClickListener(v -> {
            if (eivItem.getShowStyle() == EditItemView.SHOW_STYLE_READ) {
                civItem.getTvContent().setText("点击切换不同的显示模式");
                civItem.setShowStyle(ChooseItemView.SHOW_STYLE_EDIT);
                eivItem.setShowStyle(EditItemView.SHOW_STYLE_EDIT);
            }
        });
        
        DropItemView divDevice = findViewById(R.id.div_device);
        divDevice.setOnDropStatusChangeListener((div, isExpand) -> {
            ToastUtil.showToast(isExpand ? "展开" : "收起");
        });
    }
}
