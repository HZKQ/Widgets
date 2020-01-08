package com.hzkq.widgets.entity;

import com.keqiang.indexbar.IndexModel;

/**
 * @author Created by 汪高皖 on 2020/1/8 17:02
 */
public class User extends IndexModel {
    public String name;
    public String phone;
    
    public User(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
    
    @Override
    public String getFullName() {
        return name;
    }
}
