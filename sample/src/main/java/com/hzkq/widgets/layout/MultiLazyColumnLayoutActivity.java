package com.hzkq.widgets.layout;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.chad.library.adapter.base.RvQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hzkq.widgets.R;
import com.keqiang.layout.combination.AdapterView;
import com.keqiang.layout.combination.LazyColumn;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;

/**
 * @author Created by wanggaowan on 2021/9/18 16:56
 */
public class MultiLazyColumnLayoutActivity extends BaseActivity {
    private AdapterView mAdapterView;
    private AdapterView mAdapterView2;
    private LazyColumn mLazyColumn;
    private AdapterView mAdapterView3;
    private Button mButton;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_multi_lazy_column_layout;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mLazyColumn = findViewById(R.id.column);
        mAdapterView = mLazyColumn.findViewById2(R.id.lazyColumn);
        mButton = findViewById(R.id.btn);
        mAdapterView2 = mLazyColumn.findViewById2(R.id.lazyColumn2);
        mAdapterView3 = mLazyColumn.findViewById2(R.id.lazyColumn3);
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
            strings.add("test str2:" + i);
        }
        CustomerAdapter2 adapter2 = new CustomerAdapter2(strings);
        mAdapterView2.setAdapter(adapter2);
        
        strings = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            strings.add("test str3:" + i);
        }
        CustomerAdapter adapter3 = new CustomerAdapter(strings);
        mAdapterView3.setAdapter(adapter3);
    }
    
    @Override
    public void initEvent() {
        mButton.setOnClickListener(v -> {
        
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
            return null;
        }
        
        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
        
        @Override
        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
        }
        
        @Override
        public int findRelativeAdapterPositionIn(@NonNull Adapter<? extends ViewHolder> adapter, @NonNull ViewHolder viewHolder, int localPosition) {
            return super.findRelativeAdapterPositionIn(adapter, viewHolder, localPosition);
        }
    }
    
    private static class CustomerAdapter2 extends RvQuickAdapter<String, BaseViewHolder> {
        
        public CustomerAdapter2(@Nullable List<? extends String> data) {
            super(R.layout.rv_item_layout2, data);
        }
        
        @Override
        protected void convert(@NonNull BaseViewHolder holder, String item) {
            int position = holder.getBindingAdapterPosition();
            TextView itemView = (TextView) holder.itemView;
            itemView.setText(getData().get(position) + ";" + "当前位置：" + position);
        }
        
        @Override
        protected void convert(@NonNull BaseViewHolder holder, String item, @NonNull List<?> payloads) {
            int position = holder.getBindingAdapterPosition();
            TextView itemView = (TextView) holder.itemView;
            itemView.setText(getData().get(position) + ";" + "当前位置：" + position);
            
        }
        
        @Nullable
        @Override
        public int[] getNestViewIds() {
            return null;
        }
    }
}
