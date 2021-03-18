package com.keqiang.breadcrumb;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

import androidx.annotation.RequiresApi;

/**
 * 自定义横向ScrollView，增加滑动到起始和结束位置功能
 *
 * @author Created by 汪高皖 on 2018/11/12 0012 10:57
 */
public class CustomHorizontalScrollView extends HorizontalScrollView {
    public CustomHorizontalScrollView(Context context) {
        super(context);
    }
    
    public CustomHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public CustomHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    
    /**
     * 滑动到最右边
     */
    public void scrollToEnd() {
        post(() -> scrollTo(computeHorizontalScrollRange(), 0));
    }
    
    /**
     * 滑动到最左边
     */
    public void scrollToStart() {
        scrollTo(0, 0);
    }
}
