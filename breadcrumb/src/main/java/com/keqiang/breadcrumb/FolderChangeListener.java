package com.keqiang.breadcrumb;

/**
 * 当前目录发生变化监听
 *
 * @author Created by 汪高皖 on 2020/1/15 16:31
 */
public interface FolderChangeListener {
    /**
     * 当点击目录后,如果点击目录与当前目录不一致时调用
     *
     * @param folderId   新文件夹Id
     * @param folderName 新文件夹名称
     */
    void onChange(String folderId, String folderName);
}
