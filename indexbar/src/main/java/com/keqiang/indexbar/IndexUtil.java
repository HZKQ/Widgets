package com.keqiang.indexbar;

import android.util.SparseIntArray;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 建立列表索引工具，将内容按照{@link IndexModel#getFullName()}返回的值的首字母进行排序
 *
 * @author Created by 汪高皖 on 2018/6/7 0007 10:07
 */
public class IndexUtil {
    /**
     * 始终排在列表最顶端,可自己改为特点值
     */
    public static String ALWAYS_TOP_SYMBOL = "★";
    
    /**
     * 始终排在列表最底部,可自己改为特点值
     */
    public static String ALWAYS_BOTTOM_SYMBOL = "#";
    
    private static PinyinComparator<IndexModel> mPinyinComparator;
    
    private IndexUtil() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }
    
    
    /**
     * 给数据排序，默认根据{@link IndexModel#getSortLetter()}内容升序排列，其中有两个特殊字符{@link #ALWAYS_TOP_SYMBOL}
     * 和{@link #ALWAYS_BOTTOM_SYMBOL}。如果{@link IndexModel#getSortLetter()}获取内容为{@link #ALWAYS_TOP_SYMBOL},
     * 则始终排在最顶端，如果{@link IndexModel#getSortLetter()}获取内容为{@link #ALWAYS_BOTTOM_SYMBOL},则始终排在最低端。
     * 通常情况下都无需调用{@link IndexModel#setSortLetter(String)}，除非你想干预某个数据在列表的排序结果
     */
    @NonNull
    public static <T extends IndexModel> SectionIndexer sortData(List<T> tList) {
        return sortData(tList, true);
    }
    
    /**
     * 给数据排序，默认根据{@link IndexModel#getSortLetter()}内容升序排列，其中有两个特殊字符{@link #ALWAYS_TOP_SYMBOL}
     * 和{@link #ALWAYS_BOTTOM_SYMBOL}。如果{@link IndexModel#getSortLetter()}获取内容为{@link #ALWAYS_TOP_SYMBOL},
     * 则始终排在最顶端，如果{@link IndexModel#getSortLetter()}获取内容为{@link #ALWAYS_BOTTOM_SYMBOL},则始终排在最低端。
     * 通常情况下都无需调用{@link IndexModel#setSortLetter(String)}，除非你想干预某个数据在列表的排序结果
     *
     * @param isIndexNumber 是否索引数字，如果不索引，则全部放到"#"号分组
     *                      {@code true} 索引
     *                      {@code false} 不索引
     */
    @NonNull
    public static <T extends IndexModel> SectionIndexer sortData(List<T> tList, boolean isIndexNumber) {
        SectionIndexer sectionIndexer = new SectionIndexer();
        if (tList == null || tList.size() == 0) {
            return sectionIndexer;
        }
        
        if (tList.size() == 1) {
            T t = tList.get(0);
            if (t.getSortLetter() == null) {
                String selling = getSelling(t, isIndexNumber);
                t.setSortLetter(selling);
            } else if (t.getSortLetter().length() == 0) {
                t.setSortLetter(ALWAYS_BOTTOM_SYMBOL);
            }
            
            sectionIndexer.getLetters().add(t.getSortLetter());
            sectionIndexer.getPositionForSectionArray().put(t.getSortLetter().charAt(0), 0);
            sectionIndexer.getSectionForPositionArray().put(0, 0);
            return sectionIndexer;
        }
        
        try {
            if (mPinyinComparator == null) {
                mPinyinComparator = new PinyinComparator<>();
            }
            mPinyinComparator.setIndexNumber(isIndexNumber);
            Collections.sort(tList, mPinyinComparator);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        
        String letter = null;
        int firstPosition = -1;
        SparseIntArray sectionForPosition = sectionIndexer.getSectionForPositionArray();
        SparseIntArray positionForSection = sectionIndexer.getPositionForSectionArray();
        Set<String> letters = sectionIndexer.getLetters();
        for (int i = 0; i < tList.size(); i++) {
            IndexModel model = tList.get(i);
            if (letter == null || !letter.equals(model.getSortLetter())) {
                firstPosition = i;
                positionForSection.put(model.getSortLetter().charAt(0), i);
                letters.add(model.getSortLetter());
            }
            sectionForPosition.put(i, firstPosition);
            letter = model.getSortLetter();
        }
        return sectionIndexer;
    }
    
    private static <T extends IndexModel> String getSelling(T t, boolean isIndexNumber) {
        String letter = t.getFullName();
        if (letter == null || letter.length() == 0) {
            return ALWAYS_BOTTOM_SYMBOL;
        }
        
        // 汉字转换成拼音
        String pinyin = PinyinUtils.getPinyinFirstLetter(letter);
        if (pinyin.length() == 0) {
            return ALWAYS_BOTTOM_SYMBOL;
        }
        
        String sortString = pinyin.substring(0, 1).toUpperCase();
        // 正则表达式，判断首字母是否是英文字母
        if (sortString.matches("[A-Z]")) {
            return sortString.toUpperCase();
        } else if (isIndexNumber && sortString.matches("[0-9]")) {
            return sortString.toUpperCase();
        } else {
            return ALWAYS_BOTTOM_SYMBOL;
        }
    }
    
    private static class PinyinComparator<T extends IndexModel> implements Comparator<T> {
        private boolean isIndexNumber;
        
        private void setIndexNumber(boolean indexNumber) {
            isIndexNumber = indexNumber;
        }
        
        @Override
        public int compare(T lhs, T rhs) {
            if (lhs.getSortLetter() == null) {
                String selling = getSelling(lhs, isIndexNumber);
                lhs.setSortLetter(selling);
            }
            
            if (rhs.getSortLetter() == null) {
                String selling = getSelling(rhs, isIndexNumber);
                rhs.setSortLetter(selling);
            }
            
            String lFullName = lhs.getFullName() == null ? "" : lhs.getFullName();
            String rFullName = rhs.getFullName() == null ? "" : rhs.getFullName();
            
            if (ALWAYS_TOP_SYMBOL.equals(lhs.getSortLetter())) {
                if (ALWAYS_TOP_SYMBOL.equals(rhs.getSortLetter())) {
                    int c = lFullName.compareTo(rFullName);
                    return Integer.compare(c, 0);
                }
                return -1;
            } else if (ALWAYS_TOP_SYMBOL.equals(rhs.getSortLetter())) {
                return 1;
            } else if (ALWAYS_BOTTOM_SYMBOL.equals(lhs.getSortLetter())) {
                if (ALWAYS_BOTTOM_SYMBOL.equals(rhs.getSortLetter())) {
                    int c = lFullName.compareTo(rFullName);
                    return Integer.compare(c, 0);
                }
                
                return 1;
            } else if (ALWAYS_BOTTOM_SYMBOL.equals(rhs.getSortLetter())) {
                return -1;
            } else if (lhs.getSortLetter().equals(rhs.getSortLetter())) {
                int c = lFullName.compareTo(rFullName);
                return Integer.compare(c, 0);
            } else {
                int c = lhs.getSortLetter().compareTo(rhs.getSortLetter());
                return Integer.compare(c, 0);
            }
        }
    }
}
