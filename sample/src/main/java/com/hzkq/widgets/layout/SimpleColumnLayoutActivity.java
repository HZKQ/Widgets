package com.hzkq.widgets.layout;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;

/**
 * @author Created by wanggaowan on 2021/9/18 16:56
 */
public class SimpleColumnLayoutActivity extends BaseActivity {
    private TitleBar mTitleBar;
    private View mView1;
    private View mView2;
    private AdapterView mAdapterView;
    private View mView3;
    private AdapterView mAdapterView2;
    private LazyColumn mLazyColumn;
    private AdapterView mAdapterView3;
    private AdapterView mAdapterView4;
    private AdapterView mAdapterView5;
    private Button mButton;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_simple_column_layout;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mLazyColumn = findViewById(R.id.column);
        mView1 = mLazyColumn.findViewById2(R.id.view1);
        mView2 = mLazyColumn.findViewById2(R.id.view2);
        mAdapterView = mLazyColumn.findViewById2(R.id.lazyColumn);
        mButton = findViewById(R.id.btn);
        mView3 = mLazyColumn.findViewById2(R.id.view3);
        mAdapterView2 = mLazyColumn.findViewById2(R.id.lazyColumn2);
        mAdapterView3 = mLazyColumn.findViewById2(R.id.lazyColumn3);
        mAdapterView4 = mLazyColumn.findViewById2(R.id.lazyColumn4);
        mAdapterView5 = mLazyColumn.findViewById2(R.id.lazyColumn5);
    }
    
    @Override
    public void initData() {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("test str:" + i);
        }
        CustomerAdapter adapter = new CustomerAdapter(strings);
        mAdapterView.setAdapter(adapter);
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            // CustomerAdapter3 adapter2 = (CustomerAdapter3) mAdapterView2.getAdapter();
            // adapter2.getData().get(0).setName("change name");
            // adapter2.notifyItemChanged(0, "test change");
            // mLazyColumn.notifyItemChanged(8, "test");
            List<Person> personList = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                personList.add(new Person("name" + i, i));
            }
            CustomerAdapter3 adapter2 = new CustomerAdapter3(personList);
            mAdapterView2.setAdapter(adapter2);
        });
        
        strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("test str3:" + i);
        }
        CustomerAdapter adapter3 = new CustomerAdapter(strings);
        mAdapterView3.setAdapter(adapter3);
        
        strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("test str5:" + i);
        }
        CustomerAdapter adapter5 = new CustomerAdapter(strings);
        mAdapterView5.setAdapter(adapter5);
        
        List<Person> personList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            personList.add(new Person("name" + i, i));
        }
        CustomerAdapter3 adapter2 = new CustomerAdapter3(personList);
        mAdapterView2.setAdapter(adapter2);
        
        strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("test str4:" + i);
        }
        CustomerAdapter2 adapter4 = new CustomerAdapter2(strings);
        mAdapterView4.setAdapter(adapter4);
    }
    
    private GroupPlaceholder group = null;
    private boolean add = false;
    
    @Override
    public void initEvent() {
        // mButton.setVisibility(View.VISIBLE);
        mButton.setOnClickListener(v -> {
            // mLazyColumn.setPaddingRelative(30, mLazyColumn.getPaddingTop(), 30, mLazyColumn.getPaddingBottom());
            // mLazyColumn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            
            // TextView textView = new TextView(mContext);
            // LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
            // textView.setLayoutParams(params);
            // textView.setBackgroundResource(R.color.color888);
            // textView.setTextColor(getResources().getColor(R.color.colorWhite));
            // textView.setText("新增的view");
            // textView.setGravity(Gravity.CENTER);
            // textView.setTextSize(50);
            // mLazyColumn.addView(textView, 1);
            
            if (group == null) {
                group = (GroupPlaceholder) LayoutInflater.from(mContext).inflate(R.layout.view_simple_grou_placeholder, null);
                AdapterView adapterView = group.findViewById2(R.id.lazyColumn333);
                List<String> strings = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    strings.add("group placeholder itme:" + i);
                }
                CustomerAdapter adapter = new CustomerAdapter(strings);
                assert adapterView != null;
                adapterView.setAdapter(adapter);
                
                mLazyColumn.addView(group,0);
                mLazyColumn.scrollToPosition(0);
            } else {
                if (!add) {
                    TextView textView = new TextView(mContext);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
                    textView.setLayoutParams(params);
                    textView.setBackgroundResource(R.color.color888);
                    textView.setTextColor(getResources().getColor(R.color.colorWhite));
                    textView.setText("新增的view");
                    textView.setGravity(Gravity.CENTER);
                    textView.setTextSize(50);
                    group.addView(textView, 1);
                    add = true;
                } else {
                    group.removeViewAt(1);
                }
            }
            
            // mLazyColumn.removeViewAt(1);
            
            // AdapterView lazyView = new AdapterView(mContext);
            // LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // lazyView.setLayoutParams(params);
            // lazyView.setBackgroundResource(R.color.color888);
            // List<String> strings = new ArrayList<>();
            // for (int i = 0; i < 5; i++) {
            //     strings.add("test str2:" + i);
            // }
            // CustomerAdapter2 adapter2 = new CustomerAdapter2(strings);
            // lazyView.setAdapter(adapter2);
            //
            // mColumnLayout.addView(lazyView, 1);
            
            // mLazyColumn.scrollToPosition(5);
            // mAdapterView4.scrollToPositionWithOffset(0, SimpleUtil.getScaledValue(100));
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
    
    private static class CustomerAdapter3 extends RvQuickAdapter<Person, BaseViewHolder> {
        
        public CustomerAdapter3(@Nullable List<? extends Person> data) {
            super(R.layout.rv_item_layout2, data);
        }
        
        @Override
        protected void convert(@NonNull BaseViewHolder holder, Person item) {
            int position = holder.getBindingAdapterPosition();
            TextView itemView = (TextView) holder.itemView;
            itemView.setText(getData().get(position).getName() + ";" + "当前位置：" + position);
        }
        
        @Override
        protected void convert(@NonNull BaseViewHolder holder, Person item, @NonNull List<?> payloads) {
            int position = holder.getBindingAdapterPosition();
            TextView itemView = (TextView) holder.itemView;
            itemView.setText(getData().get(position).getName() + ";" + "当前位置：" + position);
        }
        
        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
        
        @Override
        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
        }
        
        @Nullable
        @Override
        public int[] getNestViewIds() {
            return null;
        }
    }
    
    public static class Person {
        private String name;
        private int age;
        
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getAge() {
            return age;
        }
        
        public void setAge(int age) {
            this.age = age;
        }
    }
}
