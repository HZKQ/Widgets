package com.hzkq.widgets.layout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chad.library.adapter.base.RvQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hzkq.widgets.R;
import com.keqiang.layout.combination.AdapterView;
import com.keqiang.layout.combination.GroupPlaceholder;
import com.keqiang.layout.combination.LazyColumn;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;

/**
 * @author Created by wanggaowan on 2021/9/18 16:56
 */
public class TestActivity extends BaseActivity {
    private TitleBar mTitleBar;
    private LazyColumn mLazyColumn;
    private AdapterView mAdapterView;
    private GroupPlaceholder mGroupRoot;
    private Button mButton;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_test;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mLazyColumn = findViewById(R.id.column);
        mAdapterView = findViewById(R.id.adapter_view);
        mGroupRoot = findViewById(R.id.group_root);
        mButton = findViewById(R.id.btn);
    }
    
    @Override
    public void initData() {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            strings.add("test str:" + i);
        }
        CustomerAdapter adapter = new CustomerAdapter(strings);
        mAdapterView.setAdapter(adapter);
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            List<String> data = ((CustomerAdapter) adapter1).getData();
            String s = data.get(position);
        });
        
    }
    
    @Override
    public void initEvent() {
        mButton.setVisibility(View.VISIBLE);
        mButton.setOnClickListener(v -> {
            if (mGroupRoot.getChildCount() > 1) {
                findViewById(R.id.tv_test).setVisibility(View.GONE);
                // mGroupRoot.getChildAt(0).setVisibility(View.GONE);
                return;
            }
            
            GroupPlaceholder view = (GroupPlaceholder) LayoutInflater.from(mContext).inflate(R.layout.activity_test_group_item, mGroupRoot, false);
            AdapterView adapterView = view.findViewById(R.id.adapter_view);
            List<String> strings = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                strings.add("test str2:" + i);
            }
            CustomerAdapter adapter = new CustomerAdapter(strings);
            adapterView.setAdapter(adapter);
            adapter.setOnItemChildClickListener((adapter1, view2, position) -> {
                List<String> data = ((CustomerAdapter) adapter1).getData();
                String s = data.get(position);
            });
            
            mGroupRoot.addView(view);
        });
    }
    
    private static class CustomerAdapter extends RvQuickAdapter<String, BaseViewHolder> {
        
        public CustomerAdapter(@Nullable List<? extends String> data) {
            super(R.layout.rv_item_layout, data);
        }
        
        @Override
        protected void convert(@NonNull BaseViewHolder holder, String item) {
            int bindingAdapterPosition = holder.getBindingAdapterPosition();
            int layoutPosition = holder.getLayoutPosition();
            int oldPosition = holder.getOldPosition();
            int absoluteAdapterPosition = holder.getAbsoluteAdapterPosition();
            Log.e("xxx", "bindingAdapterPosition:" + bindingAdapterPosition + " layoutPosition:" + layoutPosition
                + " oldPosition:" + oldPosition + "absoluteAdapterPosition:" + absoluteAdapterPosition);
            ((TextView) holder.itemView).setText(item);
        }
        
        @Nullable
        @Override
        public int[] getNestViewIds() {
            return new int[]{R.id.tv_test};
        }
    }
}
