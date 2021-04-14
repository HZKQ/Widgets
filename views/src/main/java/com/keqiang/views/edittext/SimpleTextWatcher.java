package com.keqiang.views.edittext;

import android.text.Editable;
import android.text.TextWatcher;

import com.keqiang.views.ExtendEditText;
import com.keqiang.views.ExtendTextView;

/**
 * TextWatcher简单实现，结合{@link ExtendEditText}、{@link ExtendTextView}使用
 *
 * @author Created by wanggaowan on 2021/4/14 11:29
 */
public class SimpleTextWatcher implements TextWatcher {
    private boolean forceCall = false;
    
    public SimpleTextWatcher() {
    
    }
    
    /**
     * @param forceCall 当使用{@link ExtendEditText#setTextNoListen(CharSequence)}或
     *                  {@link ExtendTextView#setTextNoListen(CharSequence)}相关方法设置文本时，
     *                  是否强制调用此监听，如果为{@code true}，则文本改变时此监听始终被调用
     */
    public SimpleTextWatcher(boolean forceCall) {
        this.forceCall = forceCall;
    }
    
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    
    }
    
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    
    }
    
    @Override
    public void afterTextChanged(Editable s) {
    
    }
    
    /**
     * 当使用{@link ExtendEditText#setTextNoListen(CharSequence)}或
     * {@link ExtendTextView#setTextNoListen(CharSequence)}相关方法设置文本时，
     * 是否强制调用此监听
     */
    public boolean isForceCall() {
        return forceCall;
    }
}
