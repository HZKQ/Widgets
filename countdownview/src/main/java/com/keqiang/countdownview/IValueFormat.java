package com.keqiang.countdownview;

/**
 * 数据格式化
 *
 * @author Created by 汪高皖 on 2020/1/11 10:34
 */
public interface IValueFormat {
    /**
     * @param second 当前倒计时剩余秒数
     */
    String format(int second);
}
