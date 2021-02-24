package com.hzkq.widgets.adapter;

import android.os.Handler;
import android.text.TextUtils;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hzkq.widgets.R;
import com.hzkq.widgets.entity.User;
import com.keqiang.indexbar.SectionIndexer;

import java.util.List;

import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * 选择通讯录
 */
public class UserAdapter extends BaseQuickAdapter<User, BaseViewHolder> {
    
    private SectionIndexer<User> mSectionIndexer;
    
    public UserAdapter(@Nullable List<User> data) {
        super(R.layout.rv_item_user, data);
    }
    
    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BaseViewHolder holder = super.onCreateViewHolder(parent, viewType);
        SimpleUtil.scaleView(holder.itemView);
        return holder;
    }
    
    @Override
    protected void convert(BaseViewHolder helper, User item) {
        helper.setText(R.id.tv_name, item.name)
            .setText(R.id.tv_phone, item.phone);
        
        int position = helper.getAdapterPosition() - getHeaderLayoutCount();
        if (position == 0) {
            helper.setGone(R.id.tv_index, true)
                .setText(R.id.tv_index, item.getSortLetter());
        } else {
            int sectionForPosition = mSectionIndexer.getSectionForPosition(position);
            int preSectionForPosition = mSectionIndexer.getSectionForPosition(position - 1);
            if (sectionForPosition != preSectionForPosition) {
                helper.setGone(R.id.tv_index, true)
                    .setText(R.id.tv_index, item.getSortLetter());
            } else {
                helper.setGone(R.id.tv_index, false);
            }
        }
    }
    
    public SectionIndexer<User> getSectionIndexer() {
        return mSectionIndexer;
    }
    
    public void setSectionIndexer(SectionIndexer<User> sectionIndexer) {
        mSectionIndexer = sectionIndexer;
    }
}
