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
import com.keqiang.indexbar.IndexUtil;
import com.keqiang.indexbar.SectionIndexer;

import java.util.List;

import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * 选择通讯录
 */
public class UserAdapter extends BaseQuickAdapter<User, BaseViewHolder> {
    
    private SectionIndexer mSectionIndexer;
    private Handler mHandler;
    private IndexDoneListener mIndexDoneListener;
    
    public UserAdapter(@Nullable List<User> data) {
        super(R.layout.rv_item_user, data);
        mHandler = new Handler();
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
        if (position == mSectionIndexer.getSectionForPosition(position)) {
            helper.setGone(R.id.tv_index, true)
                .setText(R.id.tv_index, item.getSortLetter());
        } else {
            helper.setGone(R.id.tv_index, false);
        }
    }
    
    @Override
    public void setNewData(@Nullable List<User> data) {
        if (data == null || data.size() < 50) {
            mSectionIndexer = IndexUtil.sortData(data);
            super.setNewData(data);
            if (mIndexDoneListener != null) {
                mIndexDoneListener.onDone(mSectionIndexer);
            }
            return;
        }
        
        new Thread() {
            @Override
            public void run() {
                // 数据量较大，可放到线程中执行
                mSectionIndexer = IndexUtil.sortData(data);
                mHandler.post(() -> {
                    UserAdapter.super.setNewData(data);
                    if (mIndexDoneListener != null) {
                        mIndexDoneListener.onDone(mSectionIndexer);
                    }
                });
            }
        }.start();
    }
    
    public void setIndexDoneListener(IndexDoneListener indexDoneListener) {
        mIndexDoneListener = indexDoneListener;
    }
    
    public int getPositionForLetter(String letter) {
        if (TextUtils.isEmpty(letter) || mSectionIndexer == null) {
            return -1;
        }
        
        return mSectionIndexer.getPositionForSection(letter.charAt(0));
    }
    
    /**
     * 索引建立完成监听
     */
    public interface IndexDoneListener {
        /**
         * 索引建立完成回调
         */
        void onDone(@NonNull SectionIndexer indexer);
    }
}
