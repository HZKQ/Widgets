package com.hzkq.widgets;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;
import me.zhouzhuo810.magpiex.utils.ToastUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends BaseActivity {
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
    
    }
    
    @Override
    public void initData() {
    
    }
    
    @Override
    public void initEvent() {
    
    }
    
    
    public void onZxingClick(View v) {
        new IntentIntegrator(this)
            .setCustomTitle("二维码扫描") //标题栏文字
            .setCustomTitleBg(R.color.colorPrimary)
            .setCustomTitleTextColor(R.color.colorWhite)
            .setPrompt("将扫码框对准二维码开始扫描")  //扫描框底部文字
            .setCameraId(0)  // 后置摄像头
            .setBeepEnabled(true) //是否有提示音
            .setBarcodeImageEnabled(false)
            .setAlbumScanEnabled(false) //是否启用相册扫码
            .initiateScan();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result == null || result.getContents() == null) {
            return;
        }
        String contents = result.getContents();
        ToastUtil.showToast(contents);
        
    }
    
}
