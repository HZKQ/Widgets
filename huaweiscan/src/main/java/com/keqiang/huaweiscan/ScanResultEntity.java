package com.keqiang.huaweiscan;

import com.huawei.hms.ml.scan.HmsScan;

import androidx.annotation.Nullable;

/**
 * 扫码回调
 *
 * @author Created by wanggaowan on 2021/10/27 08:56
 */
public class ScanResultEntity {
    /**
     * 扫码是否取消
     */
    private boolean isCancel;
    
    /**
     * 是否是识别图片
     */
    private boolean isScanImg;
    
    /**
     * 扫码结果
     */
    private HmsScan data;
    
    public boolean isCancel() {
        return isCancel;
    }
    
    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }
    
    public boolean isScanImg() {
        return isScanImg;
    }
    
    public void setScanImg(boolean scanImg) {
        isScanImg = scanImg;
    }
    
    /**
     * 获取扫码结果
     */
    @Nullable
    public HmsScan getData() {
        return data;
    }
    
    public void setData(HmsScan data) {
        this.data = data;
    }
    
    /**
     * 获取扫码文本内容
     */
    @Nullable
    public String getContents() {
        if (data == null) {
            return null;
        }
        
        return data.getOriginalValue();
    }
}
