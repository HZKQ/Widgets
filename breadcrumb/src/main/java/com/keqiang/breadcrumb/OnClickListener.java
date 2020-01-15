package com.keqiang.breadcrumb;

/**
 * 目录点击监听
 * @author Created by 汪高皖 on 2020/1/15 16:30
 */
public interface OnClickListener {
    /**
     * @param folderId   当前点击文件Id
     * @param folderName 当前点击文件夹名称
     */
    void onClick(String folderId, String folderName);
}
