package com.keqiang.indexbar;

import java.util.List;

import io.reactivex.annotations.Nullable;

/**
 * 索引数据
 */
public abstract class IndexModel {
    /**
     * 用于排序的字符（大写），通常是拼音首字母
     */
    private String sortLetter;
    
    public String getSortLetter() {
        return sortLetter;
    }
    
    /**
     * 通常情况无需用户手动调用此方法，除非需要干预使用{@link IndexUtil#sortData(List)}中的排序结果
     */
    public void setSortLetter(String sortLetter) {
        this.sortLetter = sortLetter;
    }
    
    /**
     * 排序数据的全称，当{@link #getSortLetter()}为null时，用于获取首字母并调用{@link #setSortLetter(String)}赋值，
     * 其次，当两个数据{@link #getSortLetter()}相同时，用于组内排序
     *
     * @return 当返回null或空时{@link #setSortLetter(String)}将赋值
     */
    @Nullable
    public abstract String getFullName();
}
