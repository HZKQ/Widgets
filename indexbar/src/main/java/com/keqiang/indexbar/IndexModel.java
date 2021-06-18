package com.keqiang.indexbar;

import java.io.Serializable;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * 索引数据
 *
 * @author Created by 汪高皖 on 2019/4/29 11:38
 */
public abstract class IndexModel implements Serializable {
    private static final long serialVersionUID = -5793476112693059104L;
    
    /**
     * 用于排序的字符（大写），通常是拼音首字母
     */
    private String sortLetter;
    /**
     * {@link #getFullName()}全拼首字母,小写
     */
    private String fullLetters;
    
    public String getSortLetter() {
        return sortLetter;
    }
    
    /**
     * 通常情况无需用户手动调用此方法，除非需要干预使用{@link IndexUtil#sortData(List)}中的排序结果
     */
    public void setSortLetter(String sortLetter) {
        this.sortLetter = sortLetter;
    }
    
    public String getFullLetters() {
        return fullLetters;
    }
    
    /**
     * 通常情况无需用户手动调用此方法，除非需要干预使用{@link IndexUtil#sortData(List)}中组内({@link #getSortLetter()}相同)的排序结果
     */
    public void setFullLetters(String fullLetters) {
        this.fullLetters = fullLetters;
    }
    
    /**
     * 排序数据的全称，当{@link #getSortLetter()}为null时，用于获取首字母并调用{@link #setSortLetter(String)}赋值，
     * 其次，当两个数据{@link #getSortLetter()}相同时，用于获取全拼，根据{@link #getFullLetters()}组内排序
     *
     * @return 当返回null或空时{@link #setSortLetter(String)}将赋值{@link IndexUtil#ALWAYS_BOTTOM_SYMBOL}
     */
    @Nullable
    public abstract String getFullName();
}
