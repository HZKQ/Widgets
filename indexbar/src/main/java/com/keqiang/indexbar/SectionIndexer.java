package com.keqiang.indexbar;

import android.util.SparseIntArray;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 建立索引后的结果
 *
 * @author Created by 汪高皖 on 2019/4/29 0029 10:38
 */
public class SectionIndexer implements android.widget.SectionIndexer {
    private SparseIntArray mPositionForSection;
    private SparseIntArray mSectionForPosition;
    private Set<String> mLetters;
    
    public SectionIndexer() {
        mPositionForSection = new SparseIntArray();
        mSectionForPosition = new SparseIntArray();
        mLetters = new LinkedHashSet<>();
    }
    
    /**
     * 获取数据中包含的索引字符，已去重
     */
    @Override
    public String[] getSections() {
        return mLetters.toArray(new String[0]);
    }
    
    /**
     * 根据索引字符获取索引第一次在列表出现的位置
     *
     * @param sectionIndex {@link IndexModel#getSortLetter()}.chartAt(0)
     * @return 未找到返回-1
     */
    @Override
    public int getPositionForSection(int sectionIndex) {
        return mPositionForSection.get(sectionIndex, -1);
    }
    
    /**
     * 根据列表中数据的位置获取该数据对应索引第一次在列表出现的位置
     *
     * @param position 列表数据在列表的位置
     * @return 未找到返回-1
     */
    @Override
    public int getSectionForPosition(int position) {
        return mSectionForPosition.get(position, -1);
    }
    
    SparseIntArray getPositionForSectionArray() {
        return mPositionForSection;
    }
    
    SparseIntArray getSectionForPositionArray() {
        return mSectionForPosition;
    }
    
    Set<String> getLetters() {
        return mLetters;
    }
}
