package com.hzkq.widgets.layout;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.chad.library.adapter.base.RvQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hzkq.widgets.R;
import com.keqiang.layout.combination.AdapterView;
import com.keqiang.layout.combination.LazyColumn;
import com.keqiang.layout.combination.LazyRow;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;

/**
 * @author Created by wanggaowan on 2021/9/18 16:56
 */
public class ComplexColumnRowLayoutActivity extends BaseActivity {
    
    private TitleBar mTitleBar;
    private AdapterView mAdapterView;
    private AdapterView mRowAdapterView;
    private AdapterView mColumnAdapterView;
    private LazyRow mLazyRow;
    private AdapterView mAdapterView2;
    private LazyColumn mColumn;
    private LazyColumn mColumn2;
    private Button mBtn;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_complex_column_row_layout;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mBtn = findViewById(R.id.btn);
        
        mColumn = findViewById(R.id.column);
        mAdapterView = mColumn.findViewById2(R.id.lazyColumn);
        mAdapterView2 = mColumn.findViewById2(R.id.lazyColumn2);
        
        mLazyRow = mColumn.findViewById2(R.id.row_layout);
        assert mLazyRow != null;
        mRowAdapterView = mLazyRow.findViewById2(R.id.row_lazy_view);
        mColumnAdapterView = mLazyRow.findViewById2(R.id.adapter_view);
        mColumn2 = mLazyRow.findViewById2(R.id.column2);
    }
    
    @Override
    public void initData() {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("test str:" + i);
        }
        CustomerAdapter adapter = new CustomerAdapter(strings);
        mAdapterView.setAdapter(adapter);
        
        strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("test horizontal str:" + i);
        }
        CustomerHorizontalAdapter horizontalAdapter = new CustomerHorizontalAdapter(strings);
        mRowAdapterView.setAdapter(horizontalAdapter);
        
        strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("横向嵌套纵向:" + i);
        }
        CustomerAdapter adapter3 = new CustomerAdapter(strings);
        mColumnAdapterView.setAdapter(adapter3);
        
        strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("test str2:" + i);
        }
        CustomerAdapter2 adapter2 = new CustomerAdapter2(strings);
        mAdapterView2.setAdapter(adapter2);
    }
    
    @Override
    public void initEvent() {
        // mBtn.setVisibility(View.VISIBLE);
        mBtn.setOnClickListener(v -> {
            mColumn.scrollToPosition(4);
            mLazyRow.scrollToPosition(4);
            mColumn2.scrollToPosition(1);
        });
    }
    
    private static class CustomerAdapter extends RvQuickAdapter<String, BaseViewHolder> {
        
        public CustomerAdapter(@Nullable List<? extends String> data) {
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
    
    private static class CustomerAdapter2 extends RvQuickAdapter<String, BaseViewHolder> {
        
        public CustomerAdapter2(@Nullable List<? extends String> data) {
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
    
    private static class CustomerHorizontalAdapter extends RvQuickAdapter<String, BaseViewHolder> {
        
        public CustomerHorizontalAdapter(@Nullable List<? extends String> data) {
            super(R.layout.rv_item_layout_horizontal, data);
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
}
