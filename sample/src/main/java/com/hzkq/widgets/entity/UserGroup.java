package com.hzkq.widgets.entity;

import com.chad.library.adapter.base.entity.node.BaseExpandNode;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Created by 汪高皖 on 2020/1/8 17:02
 */
public class UserGroup extends BaseExpandNode<User> {
    public String title;
    public List<User> users;
    
    public UserGroup(String title) {
        this.title = title;
    }
    
    @Nullable
    @Override
    public List<User> getChildNode() {
        return users;
    }
}
