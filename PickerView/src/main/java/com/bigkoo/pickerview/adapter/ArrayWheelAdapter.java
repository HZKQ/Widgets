package com.bigkoo.pickerview.adapter;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * The simple Array wheel adapter
 *
 * @param <T> the element type
 */
public class ArrayWheelAdapter<T> implements WheelAdapter<T> {
    
    /**
     * The default items length
     */
    public static final int DEFAULT_LENGTH = 4;
    
    // items
    private final List<T> items;
    // length
    private int length;
    
    /**
     * Constructor
     *
     * @param items  the items
     * @param length the max items length
     */
    public ArrayWheelAdapter(List<T> items, int length) {
        this.items = items;
        this.length = length;
    }
    
    /**
     * Constructor
     *
     * @param items the items
     */
    public ArrayWheelAdapter(List<T> items) {
        this(items, DEFAULT_LENGTH);
    }
    
    @Nullable
    @Override
    public T getItem(int index) {
        if (getItemsCount() == 0) {
            return null;
        }
        
        if (index >= 0 && index < getItemsCount()) {
            return items.get(index);
        }
        
        return null;
    }
    
    @Override
    public int getItemsCount() {
        return items == null ? 0 : items.size();
    }
    
    @Override
    public int indexOf(@Nullable T o) {
        if (getItemsCount() == 0) {
            return -1;
        }
        
        return items.indexOf(o);
    }
    
    public List<T> getItems() {
        return items;
    }
}
