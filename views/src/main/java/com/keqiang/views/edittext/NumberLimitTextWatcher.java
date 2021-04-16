package com.keqiang.views.edittext;

import android.text.Editable;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.keqiang.views.ExtendEditText;
import com.keqiang.views.ExtendTextView;

import androidx.annotation.NonNull;

/**
 * 对于仅输入数字类型进行处理，其它输入类型不做处理。
 * 如果输入类型为浮点数，可限制小数位数，默认小数位数不做限制
 *
 * @author Created by wanggaowan on 1/29/21 10:05 AM
 */
public class NumberLimitTextWatcher extends SimpleTextWatcher {
    
    private int mDecimalLimit = Integer.MAX_VALUE;
    private int mIntegerLimit = Integer.MAX_VALUE;
    private boolean mAutoRemoveInValidZero = true;
    
    protected final EditText mEditText;
    private CharSequence mChangeBeforeText;
    
    public NumberLimitTextWatcher(@NonNull EditText editText) {
        super();
        mEditText = editText;
    }
    
    /**
     * @param forceCall 当使用{@link ExtendEditText#setTextNoListen(CharSequence)}或
     *                  {@link ExtendTextView#setTextNoListen(CharSequence)}相关方法设置文本时，
     *                  是否强制调用此监听，如果为{@code true}，则文本改变时此监听始终被调用
     */
    public NumberLimitTextWatcher(@NonNull EditText editText, boolean forceCall) {
        super(forceCall);
        mEditText = editText;
    }
    
    /**
     * @param decimalCount 如果editText为可输入浮点数，此值限制最大的小数位数,>=0
     */
    public NumberLimitTextWatcher(@NonNull EditText editText, int decimalCount) {
        this(editText, decimalCount, Integer.MAX_VALUE);
    }
    
    /**
     * @param decimalCount 如果editText为可输入浮点数，此值限制最长的小数位数,>=0
     * @param integerLimit 如果editText为数字类型，此值限制最大整数位数,>=1
     */
    public NumberLimitTextWatcher(@NonNull EditText editText, int decimalCount, int integerLimit) {
        this(editText, decimalCount, integerLimit, true);
    }
    
    /**
     * @param decimalCount          如果editText为可输入浮点数，此值限制最长的小数位数,>=0
     * @param integerLimit          如果editText为数字类型，此值限制最大整数位数,>=1
     * @param autoRemoveInValidZero 如果editText为数字类型,是否自动去除整数位无效0值
     */
    public NumberLimitTextWatcher(@NonNull EditText editText, int decimalCount, int integerLimit, boolean autoRemoveInValidZero) {
        this(editText, decimalCount, integerLimit, autoRemoveInValidZero, false);
    }
    
    /**
     * @param decimalCount          如果editText为可输入浮点数，此值限制最长的小数位数,>=0
     * @param integerLimit          如果editText为数字类型，此值限制最大整数位数,>=1
     * @param autoRemoveInValidZero 如果editText为数字类型,是否自动去除整数位无效0值
     * @param forceCall             当使用{@link ExtendEditText#setTextNoListen(CharSequence)}或
     *                              {@link ExtendTextView#setTextNoListen(CharSequence)}相关方法设置文本时，
     *                              是否强制调用此监听，如果为{@code true}，则文本改变时此监听始终被调用
     */
    public NumberLimitTextWatcher(@NonNull EditText editText, int decimalCount, int integerLimit,
                                  boolean autoRemoveInValidZero, boolean forceCall) {
        super(forceCall);
        mEditText = editText;
        mAutoRemoveInValidZero = autoRemoveInValidZero;
        
        if (decimalCount < 0) {
            decimalCount = 0;
        }
        
        if (integerLimit < 1) {
            integerLimit = 1;
        }
        
        mDecimalLimit = decimalCount;
        mIntegerLimit = integerLimit;
    }
    
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // start 开始的位置,count被改变的旧内容数, after改变后的内容数量.
        // 这里的s表示改变之前的内容，通常start和count组合，
        // 可以在s中读取本次改变字段中被改变的内容。
        // 而after表示改变后新的内容的数量。
        
        // 克隆一份数据，否则改值会随着onTextChanged内容的改变而改变
        mChangeBeforeText = s.subSequence(0, s.length());
    }
    
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // start开始位置,before表示被移除数据长度，count表示新增数据长度
        if (s.equals(mChangeBeforeText) || !isNumber(mEditText)) {
            return;
        }
        
        Editable editable = mEditText.getText();
        String str = editable.toString();
        
        if (str.equals(".")) {
            if (mDecimalLimit > 0) {
                editable.replace(0, editable.length(), "0.");
            } else {
                editable.delete(0, 1);
                lengthOverLimit(mEditText, true, false);
            }
            return;
        }
        
        String integerStr;
        String decimalStr;
        int dotIndexOf = -1;
        if (str.contains(".")) {
            dotIndexOf = str.indexOf(".");
            integerStr = str.substring(0, dotIndexOf);
            decimalStr = str.substring(dotIndexOf + 1);
        } else {
            integerStr = str;
            decimalStr = "";
        }
        
        if (mAutoRemoveInValidZero && integerStr.startsWith("0") && integerStr.length() > 1) {
            int lastInValidZeroIndex = getLastInValidZeroIndex(integerStr, 1);
            editable.delete(0, lastInValidZeroIndex);
            return;
        }
        
        if (count == 0) {
            return;
        }
        
        boolean isDecimalOver = mDecimalLimit != Integer.MAX_VALUE
            && (decimalStr.length() > mDecimalLimit || (dotIndexOf != -1 && decimalStr.isEmpty() && mDecimalLimit == 0));
        boolean isIntegerOver = mIntegerLimit != Integer.MAX_VALUE && integerStr.length() > mIntegerLimit;
        boolean lengthOverLimit = isDecimalOver || isIntegerOver;
        
        if (before > 0 && lengthOverLimit) {
            editable.replace(0, editable.length(), mChangeBeforeText);
            lengthOverLimit(mEditText, isDecimalOver, isIntegerOver);
            return;
        }
        
        if (start + count > integerStr.length() && (isDecimalOver || isIntegerOver)) {
            // 跨越了整数和小数
            editable.replace(0, editable.length(), mChangeBeforeText);
            lengthOverLimit(mEditText, isDecimalOver, isIntegerOver);
            return;
        }
        
        if (dotIndexOf > -1 && start > dotIndexOf && isDecimalOver) {
            if (mDecimalLimit == 0) {
                // 删除小数点
                editable.delete(start - 1, start + count);
            } else {
                editable.delete(start, start + count);
            }
            
            lengthOverLimit(mEditText, true, false);
            return;
        }
        
        if (isIntegerOver) {
            editable.delete(start, start + count);
            lengthOverLimit(mEditText, false, true);
        }
    }
    
    private int getLastInValidZeroIndex(String str, int offset) {
        boolean startsWith = str.startsWith("0", offset);
        if (!startsWith || offset >= str.length() - 1) {
            return offset;
        }
        
        return getLastInValidZeroIndex(str, offset + 1);
    }
    
    @Override
    public void afterTextChanged(Editable s) {
    
    }
    
    /**
     * 输入数值超过设定范围
     *
     * @param isDecimalOver {@code true}:小数位超过限制
     * @param isIntegerOver {@code true}:整数位超过限制
     */
    public void lengthOverLimit(EditText editText, boolean isDecimalOver, boolean isIntegerOver) {
    
    }
    
    public void setDecimalLimit(int decimalLimit) {
        mDecimalLimit = decimalLimit;
    }
    
    public int getDecimalLimit() {
        return mDecimalLimit;
    }
    
    public void setIntegerLimit(int integerLimit) {
        mIntegerLimit = integerLimit;
    }
    
    public int getIntegerLimit() {
        return mIntegerLimit;
    }
    
    public void setAutoRemoveInValidZero(boolean autoRemoveInValidZero) {
        mAutoRemoveInValidZero = autoRemoveInValidZero;
    }
    
    public boolean isAutoRemoveInValidZero() {
        return mAutoRemoveInValidZero;
    }
    
    /**
     * {@link EditText}输入类型是否是Number类型
     */
    public static boolean isNumber(EditText editText) {
        if ((EditorInfo.TYPE_CLASS_NUMBER & (editText.getInputType() & EditorInfo.TYPE_MASK_CLASS)) == EditorInfo.TYPE_CLASS_NUMBER) {
            return (EditorInfo.TYPE_CLASS_PHONE & (editText.getInputType() & EditorInfo.TYPE_MASK_CLASS)) != EditorInfo.TYPE_CLASS_PHONE;
        }
        
        return false;
    }
}
