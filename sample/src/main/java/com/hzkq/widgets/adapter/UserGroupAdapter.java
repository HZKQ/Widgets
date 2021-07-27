package com.hzkq.widgets.adapter;

import android.view.View;

import com.chad.library.adapter.base.BaseNodeAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hzkq.widgets.R;
import com.hzkq.widgets.entity.User;
import com.hzkq.widgets.entity.UserGroup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import me.zhouzhuo810.magpiex.utils.ToastUtil;

/**
 * 选择通讯录
 */
public class UserGroupAdapter extends BaseNodeAdapter<BaseViewHolder> {
    
    public UserGroupAdapter(@Nullable List<? extends BaseNode> nodeList) {
        super(nodeList);
        addFullSpanNodeProvider(new BaseNodeProvider<UserGroup, BaseViewHolder>() {
            @Override
            public int getItemViewType() {
                return 0;
            }
            
            @Override
            public int getLayoutId() {
                return R.layout.rv_item_user_group;
            }
            
            @Override
            public void convert(@NotNull BaseViewHolder helper, UserGroup item) {
                helper.setText(R.id.tv_index, item.title + "test");
            }
            
        });
        
        addFullSpanNodeProvider(new BaseNodeProvider<User, BaseViewHolder>() {
            @Override
            public int getItemViewType() {
                return 1;
            }
            
            @Override
            public int getLayoutId() {
                return R.layout.rv_item_user;
            }
            
            @Override
            public void convert(@NotNull BaseViewHolder helper, User item) {
                helper.setGone(R.id.tv_index, false)
                    .setText(R.id.tv_name, item.name)
                    .setText(R.id.tv_phone, item.phone);
            }
            
            @Override
            public boolean getItemCouldClick() {
                return true;
            }
            
            @Override
            public void onClick(@NotNull BaseViewHolder helper, @NotNull View view, User data, int position) {
                super.onClick(helper, view, data, position);
                String name = data.name;
                ToastUtil.showToast("provider 实现的点击：" + name);
            }
        });
        
        addChildClickViewIds(R.id.tv_index);
    }
    
    @Override
    protected int getItemType(@NotNull List<? extends BaseNode> data, int position) {
        BaseNode baseNode = data.get(position);
        if (baseNode instanceof UserGroup) {
            return 0;
        }
        return 1;
    }
}
