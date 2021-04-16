package com.keqiang.views.edittext;

import com.keqiang.views.ExtendEditText;

/**
 * {@link ExtendEditText}数值超过限制位数监听
 *
 * @author Created by wanggaowan on 2021/4/15 17:11
 */
public interface NumberOverLimitListener {
    /**
     * 输入数值超过设定范围
     *
     * @param isDecimalOver {@code true}:小数位超过限制
     * @param isIntegerOver {@code true}:整数位超过限制
     */
    void onOver(ExtendEditText editText, boolean isDecimalOver, boolean isIntegerOver);
}
