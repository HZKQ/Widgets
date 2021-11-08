package com.keqiang.huaweiscan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.huawei.hms.ml.scan.HmsScan;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;

/**
 * 一维码/二维码扫码接收回调界面
 *
 * @author Created by wanggaowan on 2021/6/3 14:02
 */
public class ScanCodeActivity extends BaseActivity {
    
    private static final int REQUEST_CODE_GO_SCAN = 0x2;
    
    /**
     * 扫码配置信息
     */
    private static final String CONFIG = "config";
    /**
     * 当前Activity的唯一标识
     */
    private static final String UID = "uuid";
    
    /**
     * 将扫码回调与Activity绑定
     */
    private static final Map<String, ScanCallBack> map = new HashMap<>();
    
    public static void scan(@NonNull Context context, @NonNull ScanConfig config, ScanCallBack callBack) {
        final String uuid = UUID.randomUUID().toString();
        map.put(uuid, callBack);
        
        if (context instanceof FragmentActivity) {
            ((FragmentActivity) context).getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    map.remove(uuid);
                }
            });
        }
        
        Intent intent = new Intent(context, ScanCodeActivity.class);
        intent.putExtra(CONFIG, config);
        intent.putExtra(UID, uuid);
        context.startActivity(intent);
        
    }
    
    private String mUUID;
    private ScanConfig mConfig;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfig = getIntent().getParcelableExtra(CONFIG);
        if (mConfig == null) {
            // 目前不清楚什么情况下会得到null，但是正常调用时，传参不会置为null
            closeAct();
            return;
        }
        
        mUUID = getIntent().getStringExtra(UID);
        goScan();
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
    
    }
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_code_scan;
    }
    
    @Override
    public void initData() {
    
    }
    
    @Override
    public void initEvent() {
    
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    protected void onDestroy() {
        map.remove(mUUID);
        super.onDestroy();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        closeAct();
        if (requestCode == REQUEST_CODE_GO_SCAN) {
            boolean isCancel = false;
            boolean isScanImg = false;
            HmsScan hmsScan = null;
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    isCancel = data.getBooleanExtra(HuaWeiScanActivity.SCAN_CANCEL, false);
                    isScanImg = data.getBooleanExtra(HuaWeiScanActivity.SCAN_IMG, false);
                    hmsScan = data.getParcelableExtra(HuaWeiScanActivity.SCAN_RESULT);
                }
            } else {
                isCancel = true;
            }
            
            doCallback(isCancel, isScanImg, hmsScan);
        }
    }
    
    private void goScan() {
        Intent intent = new Intent(this, HuaWeiScanActivity.class);
        intent.putExtra(HuaWeiScanActivity.SCAN_CONFIG, mConfig);
        startActWithIntentForResult(intent, REQUEST_CODE_GO_SCAN);
    }
    
    private void doCallback(boolean isCancel, boolean isScanImg, HmsScan hmsScan) {
        ScanCallBack scanCallBack = map.remove(mUUID);
        if (scanCallBack == null) {
            return;
        }
        
        ScanResultEntity entity = new ScanResultEntity();
        entity.setCancel(isCancel);
        entity.setScanImg(isScanImg);
        entity.setData(hmsScan);
        scanCallBack.onCallBack(entity);
    }
}
