package com.keqiang.indexbar;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * 建立列表索引工具，将内容按照{@link IndexModel#getFullName()}返回的值的首字母进行排序
 *
 * @author Created by 汪高皖 on 2018/6/7 0007 10:07
 */
public class IndexUtil {
    /**
     * 始终排在列表最顶端,可自己改为特定值
     */
    public static String ALWAYS_TOP_SYMBOL = "★";
    
    /**
     * 始终排在列表最底部,可自己改为特定值
     */
    public static String ALWAYS_BOTTOM_SYMBOL = "#";
    
    private IndexUtil() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }
    
    
    /**
     * 给数据排序，默认根据{@link IndexModel#getSortLetter()}内容升序排列，其中有两个特殊字符{@link #ALWAYS_TOP_SYMBOL}
     * 和{@link #ALWAYS_BOTTOM_SYMBOL}。如果{@link IndexModel#getSortLetter()}获取内容为{@link #ALWAYS_TOP_SYMBOL},
     * 则始终排在最顶端，如果{@link IndexModel#getSortLetter()}获取内容为{@link #ALWAYS_BOTTOM_SYMBOL},则始终排在最底端。
     * 通常情况下都无需调用{@link IndexModel#setSortLetter(String)}，除非你想干预某个数据在列表的排序结果
     */
    @NonNull
    public static <T extends IndexModel> SectionIndexer<T> sortData(List<T> tList) {
        return sortData(tList, true);
    }
    
    /**
     * 给数据排序，默认根据{@link IndexModel#getSortLetter()}内容升序排列，其中有两个特殊字符{@link #ALWAYS_TOP_SYMBOL}
     * 和{@link #ALWAYS_BOTTOM_SYMBOL}。如果{@link IndexModel#getSortLetter()}获取内容为{@link #ALWAYS_TOP_SYMBOL},
     * 则始终排在最顶端，如果{@link IndexModel#getSortLetter()}获取内容为{@link #ALWAYS_BOTTOM_SYMBOL},则始终排在最底端。
     * 通常情况下都无需调用{@link IndexModel#setSortLetter(String)}，除非你想干预某个数据在列表的排序结果
     *
     * @param isIndexNumber 是否索引数字，如果不索引，则全部放到{@linkplain #ALWAYS_BOTTOM_SYMBOL #}号分组
     *                      {@code true} 索引
     *                      {@code false} 不索引
     */
    @NonNull
    public static <T extends IndexModel> SectionIndexer<T> sortData(List<T> tList, boolean isIndexNumber) {
        return sortData(tList, isIndexNumber, false);
    }
    
    /**
     * 给数据排序，默认根据{@link IndexModel#getSortLetter()}内容升序排列，其中有两个特殊字符{@link #ALWAYS_TOP_SYMBOL}
     * 和{@link #ALWAYS_BOTTOM_SYMBOL}。如果{@link IndexModel#getSortLetter()}获取内容为{@link #ALWAYS_TOP_SYMBOL},
     * 则始终排在最顶端，如果{@link IndexModel#getSortLetter()}获取内容为{@link #ALWAYS_BOTTOM_SYMBOL},则始终排在最低端。
     * 通常情况下都无需调用{@link IndexModel#setSortLetter(String)}，除非你想干预某个数据在列表的排序结果
     *
     * @param isIndexNumber 是否索引数字，如果不索引，则全部放到{@linkplain #ALWAYS_BOTTOM_SYMBOL #}号分组
     *                      {@code true} 索引
     *                      {@code false} 不索引
     * @param isUserName    是否是人名，{@code true} 处理多音字作为姓氏时的正确读音
     */
    @NonNull
    public static <T extends IndexModel> SectionIndexer<T> sortData(List<T> tList, boolean isIndexNumber, boolean isUserName) {
        SectionIndexer<T> sectionIndexer = new SectionIndexer<>();
        if (tList == null || tList.size() == 0) {
            return sectionIndexer;
        }
        
        if (tList.size() == 1) {
            T t = tList.get(0);
            swap(t, isIndexNumber, isUserName);
            sectionIndexer.setData(tList);
            sectionIndexer.getLetters().add(t.getSortLetter());
            sectionIndexer.getPositionForSectionArray().put(t.getSortLetter(), 0);
            sectionIndexer.getSectionForPositionArray().put(0, 0);
            return sectionIndexer;
        }
        
        try {
            Collections.sort(tList, new PinyinComparator<>(isIndexNumber, isUserName));
        } catch (IllegalArgumentException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        sectionIndexer.setData(tList);
        
        String letter = null;
        int letterPosition = -1;
        Map<Integer, Integer> sectionForPosition = sectionIndexer.getSectionForPositionArray();
        Map<String, Integer> positionForSection = sectionIndexer.getPositionForSectionArray();
        List<String> letters = sectionIndexer.getLetters();
        for (int i = 0; i < tList.size(); i++) {
            IndexModel model = tList.get(i);
            if (letter == null || !letter.equals(model.getSortLetter())) {
                letterPosition++;
                positionForSection.put(model.getSortLetter(), i);
                letters.add(model.getSortLetter());
            }
            sectionForPosition.put(i, letterPosition);
            letter = model.getSortLetter();
        }
        return sectionIndexer;
    }
    
    /**
     * 数据转化
     */
    private static <T extends IndexModel> void swap(T t, boolean isIndexNumber, boolean isUserName) {
        if (t.getFullLetters() == null) {
            String selling = getSelling(t, isUserName);
            t.setFullLetters(selling);
        }
        
        if (t.getSortLetter() == null) {
            String firstLetter = getFirstLetter(t.getFullLetters(), isIndexNumber);
            t.setSortLetter(firstLetter);
        } else if (t.getSortLetter().length() == 0) {
            t.setSortLetter(ALWAYS_BOTTOM_SYMBOL);
        }
    }
    
    /**
     * 获取汉字全拼首字母
     */
    private static <T extends IndexModel> String getSelling(T t, boolean isUserName) {
        String letter = t.getFullName();
        if (letter == null || letter.length() == 0) {
            return null;
        }
        
        // 获取所有汉字首字母
        if (isUserName) {
            return PinyinUtils.getFullNameFirstLetters(letter);
        } else {
            return PinyinUtils.getPinyinFirstLetters(letter);
        }
    }
    
    /**
     * 根据全拼获取首字母
     */
    private static String getFirstLetter(String pinyin, boolean isIndexNumber) {
        if (pinyin == null || pinyin.length() == 0) {
            return ALWAYS_BOTTOM_SYMBOL;
        }
        
        String sortString = pinyin.substring(0, 1);
        // 正则表达式，判断首字母是否是英文字母
        if (sortString.matches("[A-Za-z]")) {
            return sortString.toUpperCase();
        } else if (isIndexNumber && sortString.matches("[0-9]")) {
            return sortString;
        } else {
            return ALWAYS_BOTTOM_SYMBOL;
        }
    }
    
    private static class PinyinComparator<T extends IndexModel> implements Comparator<T> {
        private final boolean isIndexNumber;
        private final boolean isUserName;
        
        public PinyinComparator(boolean isIndexNumber, boolean isUserName) {
            this.isIndexNumber = isIndexNumber;
            this.isUserName = isUserName;
        }
        
        @Override
        public int compare(T lhs, T rhs) {
            swap(lhs, isIndexNumber, isUserName);
            swap(rhs, isIndexNumber, isUserName);
            
            String lFullPinyin = lhs.getFullLetters() == null ? "" : lhs.getFullLetters();
            String rFullPinyin = rhs.getFullLetters() == null ? "" : rhs.getFullLetters();
            
            if (ALWAYS_TOP_SYMBOL.equals(lhs.getSortLetter())) {
                if (ALWAYS_TOP_SYMBOL.equals(rhs.getSortLetter())) {
                    int c = lFullPinyin.compareTo(rFullPinyin);
                    return Integer.compare(c, 0);
                }
                return -1;
            } else if (ALWAYS_TOP_SYMBOL.equals(rhs.getSortLetter())) {
                return 1;
            } else if (ALWAYS_BOTTOM_SYMBOL.equals(lhs.getSortLetter())) {
                if (ALWAYS_BOTTOM_SYMBOL.equals(rhs.getSortLetter())) {
                    int c = lFullPinyin.compareTo(rFullPinyin);
                    return Integer.compare(c, 0);
                }
                
                return 1;
            } else if (ALWAYS_BOTTOM_SYMBOL.equals(rhs.getSortLetter())) {
                return -1;
            } else if (lhs.getSortLetter().equals(rhs.getSortLetter())) {
                int c = lFullPinyin.compareTo(rFullPinyin);
                return Integer.compare(c, 0);
            } else {
                int c = lhs.getSortLetter().compareTo(rhs.getSortLetter());
                return Integer.compare(c, 0);
            }
        }
    }
}
