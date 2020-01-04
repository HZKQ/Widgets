package com.github.mikephil.charting.custom.linechart;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * x轴边界Label内容缩进类型
 * @author Created by 汪高皖 on 2018/8/7 0007 14:51
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
@IntDef({XLabelRetractType.NONE, XLabelRetractType.LEFT, XLabelRetractType.RIGHT, XLabelRetractType.BOTH})
public @interface XLabelRetractType{
    /**
     * 都不缩进
     */
    int NONE = 0;
    /**
     * 只X轴左边缩进
     */
    int LEFT = 1;
    
    /**
     * 只X轴右边缩进
     */
    int RIGHT = 2;
    
    /**
     * X轴两边都缩进
     */
    int BOTH = 3;
}
