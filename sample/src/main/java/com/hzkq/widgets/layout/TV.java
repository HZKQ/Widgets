package com.hzkq.widgets.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * @author Created by wanggaowan on 2021/10/5 08:50
 */
public class TV extends AppCompatTextView {
    public TV(@NonNull Context context) {
        super(context);
    }
    
    public TV(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    
    public TV(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.e("xxx","onAttachedToWindow");
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.e("xxx","onDetachedFromWindow");
    }
}
