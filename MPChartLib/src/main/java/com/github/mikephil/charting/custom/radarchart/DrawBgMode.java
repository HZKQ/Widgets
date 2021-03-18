package com.github.mikephil.charting.custom.radarchart;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;

/**
 * 雷达图绘制模式
 *
 * @author Created by wanggaowan on 3/18/21 3:06 PM
 */
@IntDef({DrawBgMode.POLYGON, DrawBgMode.CIRCULAR})
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface DrawBgMode {
    /**
     * 绘制正多边形
     */
    int POLYGON = 0;
    
    /**
     * 绘制圆形
     */
    int CIRCULAR = 1;
}
