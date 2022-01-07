package com.keqiang.indexbar;

import java.util.List;

/**
 * 索引数据
 */
public abstract class AbsIndexModel implements IndexModel{
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
}
