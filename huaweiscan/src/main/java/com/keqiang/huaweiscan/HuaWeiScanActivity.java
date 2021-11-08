package com.keqiang.huaweiscan;

import android.Manifest.permission;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huawei.hms.hmsscankit.RemoteView;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;
import me.zhouzhuo810.magpiex.utils.ToastUtil;

/**
 * 华为扫码界面
 *
 * @author Created by wanggaowan on 2021/10/26 16:56
 */
public class HuaWeiScanActivity extends BaseActivity {
    
    /**
     * 扫码配置数据
     */
    public static final String SCAN_CONFIG = "scanConfig";
    
    /**
     * 扫码是否取消
     */
    public static final String SCAN_CANCEL = "scanCancel";
    
    /**
     * 是否扫描图片
     */
    public static final String SCAN_IMG = "scanImg";
    
    /**
     * 扫码结果
     */
    public static final String SCAN_RESULT = "scanResult";
    
    private static final int REQUEST_CODE_PERMISSION = 0x2333;
    private static final int REQUEST_CODE_PHOTO = 0x1113;
    
    private RemoteView remoteView;
    private View mBackBtn;
    private View mAlbumBtn;
    private View mVScanLine;
    private ObjectAnimator mTranslationY;
    
    private ScanConfig mScanConfig;
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mScanConfig = getIntent().getParcelableExtra(SCAN_CONFIG);
        if (mScanConfig == null) {
            closeAct();
            return;
        }
        
        mBackBtn = findViewById(R.id.back_btn);
        mAlbumBtn = findViewById(R.id.album_btn);
        mVScanLine = findViewById(R.id.v_scan_line);
        FrameLayout frameLayout = findViewById(R.id.rim);
        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvHint = findViewById(R.id.tv_hint);
        
        if (!mScanConfig.isCouldScanImg()) {
            mAlbumBtn.setVisibility(View.GONE);
        }
        
        if (mScanConfig.getTitle() != null) {
            tvTitle.setText(mScanConfig.getTitle());
        }
        
        if (mScanConfig.getHint() != null) {
            tvHint.setText(mScanConfig.getHint());
        }
        
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        
        // 界面展示的识别区域大小
        int scanFrameSize = SimpleUtil.getScaledValue(950);
        // 实际识别区域扩大offset范围
        int offset = SimpleUtil.getScaledValue(50);
        // 配置识别区域位置
        Rect rect = new Rect();
        rect.left = screenWidth / 2 - scanFrameSize / 2 - offset;
        rect.right = screenWidth / 2 + scanFrameSize / 2 + offset;
        rect.top = screenHeight / 2 - scanFrameSize / 2 - offset;
        rect.bottom = screenHeight / 2 + scanFrameSize / 2 + offset;
        remoteView = new RemoteView.Builder()
            .setContext(this)
            .setBoundingBox(rect)
            .setFormat(mScanConfig.getFormat(), mScanConfig.getFormats())
            // 是否循环扫码
            .setContinuouslyScan(false)
            .build();
        
        remoteView.onCreate(savedInstanceState);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        frameLayout.addView(remoteView, params);
    }
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_huawei_scan;
    }
    
    @Override
    public void initData() {
    
    }
    
    @Override
    public void initEvent() {
        mAlbumBtn.setOnClickListener(v ->
            ActivityCompat.requestPermissions(this,
                new String[]{permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_PERMISSION));
        
        mBackBtn.setOnClickListener(v -> closeAct());
        
        remoteView.setOnResultCallback(result -> {
            if (result != null && result.length > 0 && result[0] != null && result[0].getOriginalValue() != null) {
                HmsScan hmsScan = result[0];
                Intent intent = new Intent();
                intent.putExtra(SCAN_CANCEL, false);
                intent.putExtra(SCAN_RESULT, hmsScan);
                setResult(RESULT_OK, intent);
                closeAct();
            }
        });
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        remoteView.onStart();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        remoteView.onResume();
        startScanAnim();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        remoteView.onPause();
        endScanAnim();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        remoteView.onDestroy();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        remoteView.onStop();
    }
    
    /**
     * Handle the return results from the album.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                HmsScan[] hmsScans = ScanUtil.decodeWithBitmap(this, bitmap,
                    new HmsScanAnalyzerOptions.Creator().setPhotoMode(true).create());
                if (hmsScans != null && hmsScans.length > 0 && hmsScans[0] != null
                    && hmsScans[0].getOriginalValue() != null) {
                    HmsScan hmsScan = hmsScans[0];
                    Intent intent = new Intent();
                    intent.putExtra(SCAN_CANCEL, false);
                    intent.putExtra(SCAN_IMG, true);
                    intent.putExtra(SCAN_RESULT, hmsScan);
                    setResult(RESULT_OK, intent);
                    closeAct();
                } else {
                    imgScanFailed();
                }
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
                imgScanFailed();
            }
        }
    }
    
    private void imgScanFailed() {
        if (mScanConfig.isImgScanFailedBack()) {
            Intent intent = new Intent();
            intent.putExtra(SCAN_CANCEL, false);
            intent.putExtra(SCAN_IMG, true);
            setResult(RESULT_OK, intent);
            closeAct();
            return;
        }
        
        if (mScanConfig.getImgScanFailedHint() != null) {
            ToastUtil.showToast(mScanConfig.getImgScanFailedHint());
            return;
        }
        
        ToastUtil.showToast(getString(R.string.image_scan_error_hint));
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length == 0 || grantResults.length == 0) {
            return;
        }
        
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActWithIntentForResult(pickIntent, REQUEST_CODE_PHOTO);
            }
        }
    }
    
    private void startScanAnim() {
        if (mTranslationY != null && mTranslationY.isStarted()) {
            return;
        }
        
        mTranslationY = ObjectAnimator.ofFloat(mVScanLine, "translationY", 0, SimpleUtil.getScaledValue(945));
        mTranslationY.setDuration(3000);
        mVScanLine.setVisibility(View.VISIBLE);
        mTranslationY.setRepeatCount(ValueAnimator.INFINITE);
        mTranslationY.start();
    }
    
    private void endScanAnim() {
        mVScanLine.setVisibility(View.GONE);
        if (mTranslationY != null && mTranslationY.isRunning()) {
            mTranslationY.end();
        }
    }
}
