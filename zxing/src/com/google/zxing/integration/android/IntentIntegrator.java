/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.integration.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.fragment.app.Fragment;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * @author Sean Owen
 * @author Fred Lin
 * @author Isaac Potoczny-Jones
 * @author Brad Drehmer
 * @author gcstang
 */
@SuppressWarnings({"unused", "WeakerAccess", "RedundantSuppression"})
public class IntentIntegrator {
    private static final String TAG = IntentIntegrator.class.getSimpleName();
    
    public static final int REQUEST_CODE = 0x0000c0de; // Only use bottom 16 bits
    
    private final Activity activity;
    private android.app.Fragment fragment;
    private Fragment supportFragment;
    
    private final Map<String, Object> moreExtras = new HashMap<>(3);
    
    private Class<?> captureActivity;
    
    private int requestCode = REQUEST_CODE;
    
    protected Class<?> getDefaultCaptureActivity() {
        return CaptureActivity.class;
    }
    
    public IntentIntegrator(Activity activity) {
        this.activity = activity;
    }
    
    public Class<?> getCaptureActivity() {
        if (captureActivity == null) {
            captureActivity = getDefaultCaptureActivity();
        }
        return captureActivity;
    }
    
    /**
     * Set the Activity class to use. It can be any activity, but should handle the intent extras
     * as used here.
     *
     * @param captureActivity the class
     */
    public IntentIntegrator setCaptureActivity(Class<?> captureActivity) {
        this.captureActivity = captureActivity;
        return this;
    }
    
    /**
     * Change the request code that is used for the Intent. If it is changed, it is the caller's
     * responsibility to check the request code from the result intent.
     *
     * @param requestCode the new request code
     * @return this
     */
    public IntentIntegrator setRequestCode(int requestCode) {
        if (requestCode <= 0 || requestCode > 0x0000ffff) {
            throw new IllegalArgumentException("requestCode out of range");
        }
        this.requestCode = requestCode;
        return this;
    }
    
    /**
     * @param fragment {@link Fragment} invoking the integration.
     *                 {@link #startActivityForResult(Intent, int)} will be called on the {@link Fragment} instead
     *                 of an {@link Activity}
     */
    public static IntentIntegrator forSupportFragment(Fragment fragment) {
        IntentIntegrator integrator = new IntentIntegrator(fragment.getActivity());
        integrator.supportFragment = fragment;
        return integrator;
    }
    
    /**
     * @param fragment {@link Fragment} invoking the integration.
     *                 {@link #startActivityForResult(Intent, int)} will be called on the {@link Fragment} instead
     *                 of an {@link Activity}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static IntentIntegrator forFragment(android.app.Fragment fragment) {
        IntentIntegrator integrator = new IntentIntegrator(fragment.getActivity());
        integrator.fragment = fragment;
        return integrator;
    }
    
    public Map<String, ?> getMoreExtras() {
        return moreExtras;
    }
    
    public final IntentIntegrator addExtra(String key, Object value) {
        moreExtras.put(key, value);
        return this;
    }
    
    /**
     * Set a prompt to display on the capture screen, instead of using the default.
     *
     * @param prompt the prompt to display
     */
    public final IntentIntegrator setPrompt(String prompt) {
        if (prompt != null) {
            addExtra(Intents.Scan.PROMPT_MESSAGE, prompt);
        }
        return this;
    }
    
    /**
     * By default, the orientation is locked. Set to false to not lock.
     *
     * @param locked true to lock orientation
     */
    public IntentIntegrator setOrientationLocked(boolean locked) {
        addExtra(Intents.Scan.ORIENTATION_LOCKED, locked);
        return this;
    }
    
    /**
     * Use the specified camera ID.
     *
     * @param cameraId camera ID of the camera to use. A negative value means "no preference".
     * @return this
     */
    public IntentIntegrator setCameraId(int cameraId) {
        if (cameraId >= 0) {
            addExtra(Intents.Scan.CAMERA_ID, cameraId);
        }
        return this;
    }
    
    /**
     * 设置标题
     *
     * @param title 标题
     * @return this
     */
    public IntentIntegrator setCustomTitle(String title) {
        if (title != null) {
            addExtra(Intents.Scan.CUSTOM_TITLE_TEXT, title);
        }
        return this;
    }
    
    /**
     * 设置标题背景
     *
     * @param bgRes 标题背景
     * @return this
     */
    public IntentIntegrator setCustomTitleBg(@DrawableRes @ColorRes int bgRes) {
        addExtra(Intents.Scan.CUSTOM_TITLE_BG, bgRes);
        return this;
    }
    
    /**
     * 设置标题文字颜色
     *
     * @param textColorRes 标题文字颜色
     * @return this
     */
    public IntentIntegrator setCustomTitleTextColor(@ColorRes int textColorRes) {
        addExtra(Intents.Scan.CUSTOM_TEXT_COLOR, SimpleUtil.getColor(textColorRes));
        return this;
    }
    
    /**
     * Set to false to disable beep on scan.
     *
     * @param enabled false to disable beep
     * @return this
     */
    public IntentIntegrator setBeepEnabled(boolean enabled) {
        addExtra(Intents.Scan.BEEP_ENABLED, enabled);
        return this;
    }
    
    /**
     * Set to true to enable saving the barcode image and sending its path in the result Intent.
     *
     * @param enabled true to enable barcode image
     * @return this
     */
    public IntentIntegrator setBarcodeImageEnabled(boolean enabled) {
        addExtra(Intents.Scan.BARCODE_IMAGE_ENABLED, enabled);
        return this;
    }
    
    /**
     * 设置扫码模式
     *
     * @param mode {@link Intents.Scan#QR_CODE_MODE}、{@link Intents.Scan#BAR_CODE_MODE}
     */
    public IntentIntegrator setMode(String mode) {
        addExtra(Intents.Scan.MODE, mode);
        return this;
    }
    
    /**
     * 设置扫码需要支持的模式，如果设置了此值，则覆盖{@link #setMode(String)}
     *
     * @param formats 参考{@link BarcodeFormat}
     */
    public IntentIntegrator setFormat(List<BarcodeFormat> formats) {
        addExtra(Intents.Scan.FORMATS, formats);
        return this;
    }
    
    /**
     * 相册扫码
     *
     * @param enabled true to enable album scan
     * @return this
     */
    public IntentIntegrator setAlbumScanEnabled(boolean enabled) {
        addExtra(Intents.Scan.ALBUM_ENABLE, enabled);
        return this;
    }
    
    
    /**
     * Initiates a scan for all known barcode types with the default camera.
     */
    public final void initiateScan() {
        startActivityForResult(createScanIntent(), requestCode);
    }
    
    /**
     * Initiates a scan for all known barcode types with the default camera.
     * And starts a timer to finish on timeout
     *
     * @return Activity.RESULT_CANCELED and true on parameter TIMEOUT.
     */
    public IntentIntegrator setTimeout(long timeout) {
        addExtra(Intents.Scan.TIMEOUT, timeout);
        return this;
    }
    
    /**
     * Create an scan intent with the specified options.
     *
     * @return the intent
     */
    public Intent createScanIntent() {
        Intent intentScan = new Intent(activity, getCaptureActivity());
        intentScan.setAction(Intents.Scan.ACTION);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        attachMoreExtras(intentScan);
        return intentScan;
    }
    
    /**
     * Start an activity. This method is defined to allow different methods of activity starting for
     * newer versions of Android and for compatibility library.
     *
     * @param intent Intent to start.
     * @param code   Request code for the activity
     * @see android.app.Activity#startActivityForResult(Intent, int)
     * @see android.app.Fragment#startActivityForResult(Intent, int)
     */
    protected void startActivityForResult(Intent intent, int code) {
        if (fragment != null) {
            fragment.startActivityForResult(intent, code);
        } else if (supportFragment != null) {
            supportFragment.startActivityForResult(intent, code);
        } else {
            activity.startActivityForResult(intent, code);
        }
    }
    
    protected void startActivity(Intent intent) {
        if (fragment != null) {
            fragment.startActivity(intent);
        } else if (supportFragment != null) {
            supportFragment.startActivity(intent);
        } else {
            activity.startActivity(intent);
        }
    }
    
    /**
     * <p>Call this from your {@link Activity}'s onActivityResult(int, int, Intent) method.</p>
     *
     * This checks that the requestCode is equal to the default REQUEST_CODE.
     *
     * @param requestCode request code from {@code onActivityResult()}
     * @param resultCode  result code from {@code onActivityResult()}
     * @param intent      {@link Intent} from {@code onActivityResult()}
     * @return null if the event handled here was not related to this class, or
     * else an {@link IntentResult} containing the result of the scan. If the user cancelled scanning,
     * the fields will be null.
     */
    public static IntentResult parseActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE) {
            return parseActivityResult(resultCode, intent);
        }
        return null;
    }
    
    /**
     * Parse activity result, without checking the request code.
     *
     * @param resultCode result code from {@code onActivityResult()}
     * @param intent     {@link Intent} from {@code onActivityResult()}
     * @return an {@link IntentResult} containing the result of the scan. If the user cancelled scanning,
     * the fields will be null.
     */
    public static IntentResult parseActivityResult(int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            String contents = intent.getStringExtra(Intents.Scan.RESULT);
            String formatName = intent.getStringExtra(Intents.Scan.RESULT_FORMAT);
            byte[] rawBytes = intent.getByteArrayExtra(Intents.Scan.RESULT_BYTES);
            int intentOrientation = intent.getIntExtra(Intents.Scan.RESULT_ORIENTATION, Integer.MIN_VALUE);
            Integer orientation = intentOrientation == Integer.MIN_VALUE ? null : intentOrientation;
            String errorCorrectionLevel = intent.getStringExtra(Intents.Scan.RESULT_ERROR_CORRECTION_LEVEL);
            String barcodeImagePath = intent.getStringExtra(Intents.Scan.RESULT_BARCODE_IMAGE_PATH);
            return new IntentResult(contents,
                formatName,
                rawBytes,
                orientation,
                errorCorrectionLevel,
                barcodeImagePath);
        }
        return new IntentResult();
    }
    
    private static List<String> list(String... values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }
    
    private void attachMoreExtras(Intent intent) {
        for (Map.Entry<String, Object> entry : moreExtras.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // Kind of hacky
            if (value instanceof Integer) {
                intent.putExtra(key, (Integer) value);
            } else if (value instanceof Long) {
                intent.putExtra(key, (Long) value);
            } else if (value instanceof Boolean) {
                intent.putExtra(key, (Boolean) value);
            } else if (value instanceof Double) {
                intent.putExtra(key, (Double) value);
            } else if (value instanceof Float) {
                intent.putExtra(key, (Float) value);
            } else if (value instanceof Bundle) {
                intent.putExtra(key, (Bundle) value);
            } else {
                intent.putExtra(key, value.toString());
            }
        }
    }
}
