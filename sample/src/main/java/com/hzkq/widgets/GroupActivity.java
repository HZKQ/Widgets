package com.hzkq.widgets;

import android.os.Bundle;

import com.hzkq.widgets.adapter.UserGroupAdapter;
import com.hzkq.widgets.entity.User;
import com.hzkq.widgets.entity.UserGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;

/**
 * 分组
 *
 * @author Created by wanggaowan on 2021/7/26 16:03
 */
public class GroupActivity extends BaseActivity {
    
    private TitleBar mTitleBar;
    private RecyclerView mRv;
    private UserGroupAdapter mAdapter;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_group;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new LinearLayoutManager(this));
    }
    
    @Override
    public void initData() {
        List<UserGroup> groups = new ArrayList<>();
    
        UserGroup group = new UserGroup("张");
        groups.add(group);
        List<User> list = new ArrayList<>();
        User user = new User("张三", "17681110000");
        list.add(user);
        
        user = new User("张军", "17681110009");
        list.add(user);
        
        user = new User("张淑韵", "17681110001");
        list.add(user);
        group.users = list;
        
        group = new UserGroup("@");
        groups.add(group);
        list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            user = new User("@" + i, "1868111001" + i);
            list.add(user);
        }
        group.users = list;
        
        group = new UserGroup("李");
        groups.add(group);
        list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            user = new User("李" + i, "1358111001" + i);
            list.add(user);
        }
        group.users = list;
    
        group = new UserGroup("胡");
        groups.add(group);
        list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            user = new User("胡" + i, "1958111001" + i);
            list.add(user);
        }
        group.users = list;
    
        group = new UserGroup("刘");
        groups.add(group);
        list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            user = new User("刘" + i, "1238111001" + i);
            list.add(user);
        }
        group.users = list;
    
        mAdapter = new UserGroupAdapter(groups);
        mRv.setAdapter(mAdapter);
    }
    
    @Override
    public void initEvent() {
        mTitleBar.getLlLeft().setOnClickListener(v -> closeAct());
        
        findViewById(R.id.tv_expand_all).setOnClickListener(v -> mAdapter.expandAll());
    
        findViewById(R.id.tv_collapse_all).setOnClickListener(v -> mAdapter.collapseAll());
        
        // mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
        //     BaseNode baseNode = mAdapter.getData().get(position);
        //     if (baseNode instanceof BaseExpandNode) {
        //         if (((BaseExpandNode<?>) baseNode).isExpanded()) {
        //             mAdapter.collapse(position);
        //         } else {
        //             mAdapter.expand(position);
        //         }
        //     }
        // });
    }
}
