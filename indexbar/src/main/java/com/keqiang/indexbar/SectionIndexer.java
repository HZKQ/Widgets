package com.keqiang.indexbar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 索引器，建立{@link #getData()} 与 {@link #getSections()} 之间的索引关系。
 *
 * @author Created by 汪高皖 on 2019/4/29 0029 10:38
 */
public class SectionIndexer<T> implements android.widget.SectionIndexer, Serializable {
    private static final long serialVersionUID = -4737333504664429520L;
    
    /**
     * 无效下标值
     */
    public static final int INVALID_INDEX = -1;
    
    /**
     * 索引对应列表数据的位置
     */
    private final Map<String, Integer> mPositionForSection;
    
    /**
     * 列表数据位置对应索引位置
     */
    private final Map<Integer, Integer> mSectionForPosition;
    
    /**
     * 索引字符
     */
    private final List<String> mLetters;
    
    /**
     * 索引对应的列表数据
     */
    private List<T> mData;
    
    SectionIndexer() {
        mPositionForSection = new HashMap<>();
        mSectionForPosition = new HashMap<>();
        mLetters = new ArrayList<>();
    }
    
    /**
     * 获取索引数据
     */
    @Override
    @NonNull
    public String[] getSections() {
        return mLetters.toArray(new String[0]);
    }
    
    /**
     * 获取索引数据
     */
    @NonNull
    public List<String> getLetters() {
        return mLetters;
    }
    
    /**
     * 获取索引对应的数据列表
     */
    @Nullable
    public List<T> getData() {
        return mData;
    }
    
    void setData(List<T> data) {
        mData = data;
    }
    
    /**
     * 根据索引在索引列表({@link #getSections()})中的位置获取索引对应的数据列表({@link #getData()})中索引的起始位置
     *
     * @param sectionIndex {@link IndexModel#getSortLetter()}中索引所处下标
     * @return 未找到返回 {@link #INVALID_INDEX}
     */
    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= mLetters.size()) {
            return INVALID_INDEX;
        }
        return getPositionForIndexLetter(mLetters.get(sectionIndex));
    }
    
    /**
     * 根据索引字符获取索引在数据列表({@link #getData()})中的起始位置
     *
     * @param letter {@link IndexModel#getSortLetter()}中的索引字符
     * @return 未找到返回 {@link #INVALID_INDEX}
     */
    public int getPositionForIndexLetter(String letter) {
        if (letter == null) {
            return -1;
        }
        Integer index = mPositionForSection.get(letter);
        return index == null ? INVALID_INDEX : index;
    }
    
    /**
     * 根据数据列表({@link #getData()})中的位置获取该数据对应的索引在索引列表({@link #getSections()})中的位置
     *
     * @param position 列表数据在列表的位置
     * @return 未找到返回 {@link #INVALID_INDEX}
     */
    @Override
    public int getSectionForPosition(int position) {
        Integer index = mSectionForPosition.get(position);
        return index == null ? INVALID_INDEX : index;
    }
    
    /**
     * 根据列表({@link #getData()})中的数据获取该数据对应的索引在索引列表({@link #getSections()})中的位置
     *
     * @param t 列表数据
     * @return 未找到返回 {@link #INVALID_INDEX}
     */
    public int getSectionForData(T t) {
        if (t == null || mData == null) {
            return -1;
        }
        
        return getSectionForPosition(mData.indexOf(t));
    }
    
    Map<String, Integer> getPositionForSectionArray() {
        return mPositionForSection;
    }
    
    Map<Integer, Integer> getSectionForPositionArray() {
        return mSectionForPosition;
    }
}
