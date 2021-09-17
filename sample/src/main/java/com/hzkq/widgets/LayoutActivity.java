package com.hzkq.widgets;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chad.library.adapter.base.RvQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.keqiang.layout.combination.Column;
import com.keqiang.layout.combination.LazyColumn;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;

/**
 * @author Created by 汪高皖 on 2020/1/17 17:26
 */
public class LayoutActivity extends BaseActivity {
    private TitleBar mTitleBar;
    private View mView1;
    private View mView2;
    private LazyColumn mLazyColumn;
    private View mView3;
    private LazyColumn mLazyColumn2;
    private Column mColumn;
    private LazyColumn mLazyColumn3;
    private LazyColumn mLazyColumn4;
    private LazyColumn mLazyColumn5;
    
    private Button mButton;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_layout;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mColumn = findViewById(R.id.column);
        mView1 = mColumn.findViewById2(R.id.view1);
        mView2 = mColumn.findViewById2(R.id.view2);
        mLazyColumn = mColumn.findViewById2(R.id.lazyColumn);
        mView3 = mColumn.findViewById2(R.id.view3);
        mLazyColumn2 = mColumn.findViewById2(R.id.lazyColumn2);
        mButton = findViewById(R.id.btn);
        mLazyColumn3 = mColumn.findViewById2(R.id.lazyColumn3);
        mLazyColumn4 = mColumn.findViewById2(R.id.lazyColumn4);
        mLazyColumn5 = mColumn.findViewById2(R.id.lazyColumn5);
    }
    
    @Override
    public void initData() {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("test str:" + i);
        }
        CustomterAdapter adapter = new CustomterAdapter(strings);
        mLazyColumn.setAdapter(adapter);
    
        strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("test str3:" + i);
        }
        CustomterAdapter adapter3 = new CustomterAdapter(strings);
        mLazyColumn3.setAdapter(adapter3);
    
        strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("test str5:" + i);
        }
        CustomterAdapter adapter5 = new CustomterAdapter(strings);
        mLazyColumn5.setAdapter(adapter5);
        
        strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("test str2:" + i);
        }
        CustomterAdapter2 adapter2 = new CustomterAdapter2(strings);
        mLazyColumn2.setAdapter(adapter2);
    
        strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("test str4:" + i);
        }
        CustomterAdapter2 adapter4 = new CustomterAdapter2(strings);
        mLazyColumn4.setAdapter(adapter4);
    }
    
    @Override
    public void initEvent() {
        mButton.setOnClickListener(v -> {
            // mLazyColumn.setPaddingRelative(30, mLazyColumn.getPaddingTop(), 30, mLazyColumn.getPaddingBottom());
            mLazyColumn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        });
    }
    
    private class CustomterAdapter extends RvQuickAdapter<String, BaseViewHolder> {
        
        public CustomterAdapter(@Nullable List<? extends String> data) {
            super(R.layout.rv_item_layout, data);
        }
        
        @Override
        protected void convert(@NonNull BaseViewHolder holder, String item) {
            ((TextView) holder.itemView).setText(item);
        }
        
        @Nullable
        @Override
        public int[] getNestViewIds() {
            return null;
        }
    }
    
    private class CustomterAdapter2 extends RvQuickAdapter<String, BaseViewHolder> {
        
        public CustomterAdapter2(@Nullable List<? extends String> data) {
            super(R.layout.rv_item_layout2, data);
        }
        
        @Override
        protected void convert(@NonNull BaseViewHolder holder, String item) {
            TextView itemView = (TextView) holder.itemView;
            itemView.setText(item);
        }
        
        @Nullable
        @Override
        public int[] getNestViewIds() {
            return null;
        }
    }
}
