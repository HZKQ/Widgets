package com.keqiang.huaweiscan;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * @author Created by wanggaowan on 2021/6/3 14:55
 */
public interface ScanCallBack {
    /**
     * 扫码回调，此回调在{@link Activity#onActivityResult(int, int, Intent)}中执行
     *
     * @param scanResult 扫码结果
     */
    void onCallBack(@NonNull ScanResultEntity scanResult);
}
