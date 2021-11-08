package com.keqiang.huaweiscan;

import android.content.Context;
import android.graphics.Bitmap;

import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.hmsscankit.WriterException;
import com.huawei.hms.ml.scan.HmsBuildBitmapOption;
import com.huawei.hms.ml.scan.HmsScan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 一维码/二维码扫码/生成工具
 *
 * @author Created by wanggaowan on 2021/6/3 14:01
 */
public class CodeUtils {
    
    public static void scan(@NonNull Context context, ScanCallBack callBack) {
        ScanCodeActivity.scan(context, ScanConfig.copy(), callBack);
    }
    
    public static void scan(@NonNull Context context, @NonNull ScanConfig config, ScanCallBack callBack) {
        ScanCodeActivity.scan(context, config, callBack);
    }
    
    /**
     * 生成二维码
     */
    @Nullable
    public static Bitmap createQrCode(String content, int width, int height) {
        return createQrCode(content, width, height, null);
    }
    
    /**
     * 生成二维码
     */
    @Nullable
    public static Bitmap createQrCode(String content, int width, int height, @Nullable HmsBuildBitmapOption option) {
        try {
            // 如果未设置HmsBuildBitmapOption对象，生成二维码参数options置null。
            return ScanUtil.buildBitmap(content, HmsScan.QRCODE_SCAN_TYPE, width, height, option);
        } catch (WriterException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return null;
        }
    }
    
    /**
     * 生成条形码
     */
    @Nullable
    public static Bitmap createBarCode(String content, int width, int height) {
        return createBarCode(content, width, height, null);
    }
    
    /**
     * 生成条形码
     */
    @Nullable
    public static Bitmap createBarCode(String content, int width, int height, @Nullable HmsBuildBitmapOption option) {
        try {
            return ScanUtil.buildBitmap(content, HmsScan.CODE128_SCAN_TYPE, width, height, option);
        } catch (WriterException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
