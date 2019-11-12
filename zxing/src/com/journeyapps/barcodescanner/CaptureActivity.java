package com.journeyapps.barcodescanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.QRCodeReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.R;
import com.google.zxing.common.HybridBinarizer;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.yanzhenjie.permission.runtime.setting.SettingRequest;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.dialog.TwoBtnTextDialog;
import me.zhouzhuo810.magpiex.ui.widget.MarkView;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;
import me.zhouzhuo810.magpiex.utils.DisplayUtil;
import me.zhouzhuo810.magpiex.utils.RxHelper;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;
import me.zhouzhuo810.magpiex.utils.ToastUtil;

/**
 * 二维码扫描
 */
public class CaptureActivity extends BaseActivity {
    
    public static final int REQUEST_CODE_CHOOSE = 0x11;
    public static final int REQUEST_CODE_PERMISSION_SETTING = 0x12;
    
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private boolean mShowPermissionHint;
    private AlertDialog mConfirmDialog;
    private Disposable mSubscribe;
    
    @Override
    public void initView(@Nullable Bundle bundle) {
        barcodeScannerView = initializeContent();
        
        TitleBar ttb = barcodeScannerView.getTitleBar();
        if (ttb != null) {
            ttb.setOnTitleClickListener(new TitleBar.OnTitleClick() {
                @Override
                public void onLeftClick(ImageView imageView, MarkView markView, TextView textView) {
                    closeAct();
                }
                
                @Override
                public void onTitleClick(TextView textView) {
                
                }
                
                @Override
                public void onRightClick(ImageView imageView, MarkView markView, TextView textView) {
                    choosePhoto();
                }
            });
        }
        
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), bundle);
        capture.decode();
    }
    
    
    public void choosePhoto() {
        AndPermission.with(this)
            .runtime()
            .permission(Permission.WRITE_EXTERNAL_STORAGE)
            .rationale((context, data, executor) -> {
                mShowPermissionHint = true;
                showConfirmDialog(getString(R.string.permission_apply_zxing),
                    getString(R.string.refuse_not_use_album_zxing),
                    null, null, false,
                    new TwoBtnTextDialog.OnTwoBtnTextClick() {
                        @Override
                        public void onLeftClick(TextView v) {
                            executor.cancel();
                        }
                        
                        @Override
                        public void onRightClick(TextView v) {
                            executor.execute();
                        }
                    });
            })
            .onDenied(data -> {
                if (mShowPermissionHint) {
                    return;
                }
                
                if (AndPermission.hasAlwaysDeniedPermission(CaptureActivity.this, data)) {
                    // 这里使用一个Dialog展示没有这些权限应用程序无法继续运行，询问用户是否去设置中授权。
                    final SettingRequest setting = AndPermission.with(CaptureActivity.this).runtime().setting();
                    showConfirmDialog(getString(R.string.permission_set_zxing),
                        getString(R.string.set_write_external_storage_zxing),
                        null, null, false,
                        new TwoBtnTextDialog.OnTwoBtnTextClick() {
                            @Override
                            public void onLeftClick(TextView v) {
                            }
                            
                            @Override
                            public void onRightClick(TextView v) {
                                setting.start(REQUEST_CODE_PERMISSION_SETTING);
                            }
                        });
                } else {
                    showConfirmDialog(getString(R.string.permission_apply_zxing),
                        getString(R.string.refuse_not_use_album_zxing),
                        null, null, false,
                        new TwoBtnTextDialog.OnTwoBtnTextClick() {
                            @Override
                            public void onLeftClick(TextView v) {
                            
                            }
                            
                            @Override
                            public void onRightClick(TextView v) {
                                AndPermission.with(CaptureActivity.this)
                                    .runtime()
                                    .permission(Permission.WRITE_EXTERNAL_STORAGE)
                                    .onGranted(data1 -> {
                                        Intent intent = new Intent(Intent.ACTION_PICK, null);
                                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                        startActivityForResult(intent, REQUEST_CODE_CHOOSE);
                                    })
                                    .start();
                            }
                        });
                }
            })
            .onGranted(data -> {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, REQUEST_CODE_CHOOSE);
            }).start();
    }
    
    
    @Override
    public void initData() {
    
    }
    
    @Override
    public void initEvent() {
    
    }
    
    /**
     * Override to use a different layout.
     *
     * @return the DecoratedBarcodeView
     */
    protected DecoratedBarcodeView initializeContent() {
        return (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }
    
    @Override
    protected void onDestroy() {
        dismissConfirmDialog();
        cancelDisposable(mSubscribe);
        super.onDestroy();
        capture.onDestroy();
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
    
    @Override
    public int getLayoutId() {
        return R.layout.zxing_capture;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return true;
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CHOOSE:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    String path = FileUtils.getRealFilePath(CaptureActivity.this, data.getData());
                    onPhotoChosen(TextUtils.isEmpty(path) ? null : new File(path), path, false);
                } else if (resultCode == RESULT_OK) {
                    onPhotoChosen(null, null, false);
                } else {
                    onPhotoChosen(null, null, true);
                }
                break;
            case REQUEST_CODE_PERMISSION_SETTING:
                if (AndPermission.hasPermissions(CaptureActivity.this, Permission.WRITE_EXTERNAL_STORAGE)) {
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, REQUEST_CODE_CHOOSE);
                }
                break;
        }
    }
    
    /**
     * 相册选图完成
     *
     * @param file     图片文件
     * @param filePath 图片路径
     */
    public void onPhotoChosen(@Nullable File file, @Nullable String filePath, boolean isCancel) {
        if (TextUtils.isEmpty(filePath)) {
            if (!isCancel) {
                ToastUtil.showToast(getString(R.string.not_found_pic_path));
            }
            return;
        }
        mSubscribe = Observable.just(filePath)
            .map(new Function<String, String>() {
                @Override
                public String apply(String s) throws Exception {
                    return scanAlbum(s);
                }
            })
            .compose(RxHelper.<String>io_main())
            .subscribe(new Consumer<String>() {
                @Override
                public void accept(String s) throws Exception {
                    capture.closeAndFinish();
                    Intent intent = new Intent(Intents.Scan.ACTION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    intent.putExtra(Intents.Scan.RESULT, s);
                    intent.putExtra(Intents.Scan.RESULT_FORMAT, BarcodeFormat.QR_CODE.toString());
                    byte[] rawBytes = s.getBytes();
                    if (rawBytes.length > 0) {
                        intent.putExtra(Intents.Scan.RESULT_BYTES, rawBytes);
                    }
                    //                    intent.putExtra(Intents.Scan.RESULT_BARCODE_IMAGE_PATH, filePath);
                    setResult(Activity.RESULT_OK, intent);
                    closeActWithOutAnim();
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    throwable.printStackTrace();
                }
            });
    }
    
    
    private String scanAlbum(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        // DecodeHintType 和EncodeHintType
        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
        int[] intArray = new int[scanBitmap.getWidth() * scanBitmap.getHeight()];
        scanBitmap.getPixels(intArray, 0, scanBitmap.getWidth(), 0, 0, scanBitmap.getWidth(), scanBitmap.getHeight());
        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap.getWidth(), scanBitmap.getHeight(), intArray);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            Result decode = reader.decode(bitmap1, hints);
            return recode(decode.toString());
        } catch (NotFoundException | com.google.zxing.FormatException | ChecksumException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    
    private String recode(String str) {
        String formart = "";
        try {
            boolean ISO = Charset.forName("ISO-8859-1").newEncoder().canEncode(str);
            if (ISO) {
                formart = new String(str.getBytes(StandardCharsets.ISO_8859_1), "GB2312");
            } else {
                formart = str;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return formart;
    }
    
    
    /**
     * 显示确认对话框,当显示一行时居中显示，当显示多行时，居左和垂直居中
     */
    public void showConfirmDialog(String msg, TwoBtnTextDialog.OnTwoBtnTextClick onTwoBtnTextClick) {
        showConfirmDialog(null, msg, onTwoBtnTextClick);
    }
    
    /**
     * 显示确认对话框，当显示一行时居中显示，当显示多行时，居左和垂直居中
     */
    public void showConfirmDialog(String title, String msg, TwoBtnTextDialog.OnTwoBtnTextClick onTwoBtnTextClick) {
        showConfirmDialog(title, msg, null, null, onTwoBtnTextClick);
    }
    
    /**
     * 显示确认对话框，当显示一行时居中显示，当显示多行时，居左和垂直居中
     */
    public void showConfirmDialog(String title, String msg, String leftText, String rightText, TwoBtnTextDialog.OnTwoBtnTextClick onTwoBtnTextClick) {
        showConfirmDialog(title, msg, leftText, rightText, true, onTwoBtnTextClick);
    }
    
    /**
     * 显示确认对话框，当显示一行时居中显示，当显示多行时，居左和垂直居中
     */
    public void showConfirmDialog(String title, String msg, String leftText, String rightText, boolean cancelable, TwoBtnTextDialog.OnTwoBtnTextClick onTwoBtnTextClick) {
        dismissConfirmDialog();
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(CaptureActivity.this).inflate(R.layout.pop_confirm_dialog_zxing, null);
        SimpleUtil.scaleView(view);
        mConfirmDialog = new AlertDialog.Builder(CaptureActivity.this, R.style.transparentDialog)
            .setView(view)
            .setCancelable(cancelable)
            .create();
        mConfirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        if (title == null) {
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(title);
        }
        TextView tvMsg = view.findViewById(R.id.tv_msg);
        tvMsg.setText(msg);
        tvMsg.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int lineCount = tvMsg.getLineCount();
                tvMsg.setGravity(lineCount > 1 ? Gravity.START | Gravity.CENTER_VERTICAL : Gravity.CENTER);
                tvMsg.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        
        TextView tvCancel = view.findViewById(R.id.tv_cancel);
        if (leftText != null) {
            tvCancel.setText(leftText);
        }
        tvCancel.setOnClickListener(v -> {
            dismissConfirmDialog();
            if (onTwoBtnTextClick != null) {
                onTwoBtnTextClick.onLeftClick(tvCancel);
            }
        });
        TextView tvOk = view.findViewById(R.id.tv_ok);
        if (rightText != null) {
            tvOk.setText(rightText);
        }
        tvOk.setOnClickListener(v -> {
            dismissConfirmDialog();
            if (onTwoBtnTextClick != null) {
                onTwoBtnTextClick.onRightClick(tvOk);
            }
        });
        
        mConfirmDialog.show();
        Window window = mConfirmDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams wl = window.getAttributes();
            wl.width = (int) (DisplayUtil.getScreenWidth() * 0.75f);
            wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setAttributes(wl);
        }
    }
    
    public void dismissConfirmDialog() {
        if (mConfirmDialog != null && mConfirmDialog.isShowing()) {
            mConfirmDialog.dismiss();
        }
    }
    
    
}
