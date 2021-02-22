package com.keqiang.views.edittext;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;

/**
 * 对于仅输入数字类型进行处理，其它输入类型不做处理。
 * 如果输入类型为浮点数，可限制小数位数，默认小数位数不做限制
 *
 * @author Created by wanggaowan on 1/29/21 10:05 AM
 */
public class NumberLimitTextWatcher implements TextWatcher {
    private int mDecimalLimit = Integer.MAX_VALUE;
    private final EditText mEditText;
    
    public NumberLimitTextWatcher(@NonNull EditText editText) {
        mEditText = editText;
    }
    
    /**
     * @param decimalCount 如果editText为可输入浮点数，此值限制最长的小数位数
     */
    public NumberLimitTextWatcher(@NonNull EditText editText, int decimalCount) {
        mEditText = editText;
        if (decimalCount < 0) {
            decimalCount = 0;
        }
        mDecimalLimit = decimalCount;
    }
    
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // start 开始的位置,count被改变的旧内容数, after改变后的内容数量.
        // 这里的s表示改变之前的内容，通常start和count组合，
        // 可以在s中读取本次改变字段中被改变的内容。
        // 而after表示改变后新的内容的数量。
    }
    
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // start开始位置,before改变前的内容数量,count新增数.
        // 这里的s表示改变之后的内容，通常start和count组合，
        // 可以在s中读取本次改变字段中新的内容。
        // 而before表示被改变的内容的数量。
        if (!isNumber()) {
            return;
        }
        
        String str = s.toString();
        boolean change = false;
        if (isDecimal()) {
            if (str.contains(".")) {
                int indexOf = str.indexOf(".");
                if (s.length() - 1 - indexOf > mDecimalLimit) {
                    s = s.subSequence(0, indexOf + mDecimalLimit + 1);
                    change = true;
                }
            }
            
            if (s.toString().trim().equals(".")) {
                s = "0" + s;
                change = true;
            }
        }
        
        str = s.toString();
        if (str.startsWith("0") && str.trim().length() > 1
            && !str.startsWith(".", 1)) {
            s = s.subSequence(1, s.length());
            change = true;
            s = removePreZero(s);
        }
        
        if (change) {
            mEditText.setText(s);
            mEditText.setSelection(s.length());
        }
    }
    
    private CharSequence removePreZero(CharSequence s) {
        String str = s.toString();
        if (str.startsWith("0") && str.trim().length() > 1
            && !str.startsWith(".", 1)) {
            s = s.subSequence(1, s.length());
            return removePreZero(s);
        } else {
            return s;
        }
    }
    
    @Override
    public void afterTextChanged(Editable s) {
    
    }
    
    private boolean isNumber() {
        return (EditorInfo.TYPE_CLASS_NUMBER & (mEditText.getInputType() & EditorInfo.TYPE_MASK_CLASS))
            == EditorInfo.TYPE_CLASS_NUMBER;
    }
    
    private boolean isDecimal() {
        int decimal = EditorInfo.TYPE_NUMBER_FLAG_DECIMAL | EditorInfo.TYPE_CLASS_NUMBER;
        return (decimal & (mEditText.getInputType() & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_FLAGS)))
            == decimal;
    }
}
