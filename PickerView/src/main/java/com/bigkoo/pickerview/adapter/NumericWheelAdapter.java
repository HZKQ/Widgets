package com.bigkoo.pickerview.adapter;


import androidx.annotation.Nullable;

/**
 * Numeric Wheel adapter.
 */
public class NumericWheelAdapter implements WheelAdapter<Integer> {
    
    /**
     * The default min value
     */
    public static final int DEFAULT_MAX_VALUE = 9;
    
    /**
     * The default max value
     */
    private static final int DEFAULT_MIN_VALUE = 0;
    
    // Values
    private final int minValue;
    private final int maxValue;
    
    /**
     * Default constructor
     */
    public NumericWheelAdapter() {
        this(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }
    
    /**
     * Constructor
     *
     * @param minValue the wheel min value
     * @param maxValue the wheel max value
     */
    public NumericWheelAdapter(int minValue, int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    @Nullable
    @Override
    public Integer getItem(int index) {
        if (index >= 0 && index < getItemsCount()) {
            return minValue + index;
        }
        return 0;
    }
    
    @Override
    public int getItemsCount() {
        return maxValue - minValue + 1;
    }
    
    @Override
    public int indexOf(@Nullable Integer o) {
        if (o == null) {
            return -1;
        }
        
        return o - minValue;
    }
}
