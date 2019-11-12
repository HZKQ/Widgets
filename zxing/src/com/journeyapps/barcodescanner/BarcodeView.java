package com.journeyapps.barcodescanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BuildConfig;
import com.google.zxing.client.android.R;
import com.google.zxing.common.detector.MathUtils;
import com.journeyapps.barcodescanner.camera.CameraInstance;
import com.journeyapps.barcodescanner.camera.CameraManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A view for scanning barcodes.
 *
 * Two methods MUST be called to manage the state:
 * 1. resume() - initialize the camera and start the preview. Call from the Activity's onResume().
 * 2. pause() - stop the preview and release any resources. Call from the Activity's onPause().
 *
 * Start decoding with decodeSingle() or decodeContinuous(). Stop decoding with stopDecoding().
 *
 * @see CameraPreview for more details on the preview lifecycle.
 */
public class BarcodeView extends CameraPreview {
    
    private enum DecodeMode {
        NONE,
        SINGLE,
        CONTINUOUS
    }
    
    private DecodeMode decodeMode = DecodeMode.NONE;
    private BarcodeCallback callback = null;
    private DecoderThread decoderThread;
    
    private DecoderFactory decoderFactory;
    
    /**
     * 记录用户上一次双指之间的距离
     */
    private float mOldFingerSpacing = 0;
    
    /**
     * 记录重力感应各方向重力值
     */
    private float mGx = 0, mGy = 0, mGz = 0;
    
    /**
     * 当前重力感应较上次是否稳定
     */
    private boolean isStable = false;
    
    /**
     * 当前是否正在重新聚焦
     */
    private boolean focusing = false;
    
    private Handler resultHandler;
    
    private final Handler.Callback resultCallback = message -> {
        if (message.what == R.id.zxing_decode_succeeded) {
            BarcodeResult result = (BarcodeResult) message.obj;
            
            if (result != null) {
                if (callback != null && decodeMode != DecodeMode.NONE) {
                    callback.barcodeResult(result);
                    if (decodeMode == DecodeMode.SINGLE) {
                        stopDecoding();
                    }
                }
            }
            return true;
        } else if (message.what == R.id.zxing_decode_failed) {
            // Failed. Next preview is automatically tried.
            return true;
        } else if (message.what == R.id.zxing_possible_result_points) {
            //noinspection unchecked
            List<ResultPoint> resultPoints = (List<ResultPoint>) message.obj;
            if (callback != null && decodeMode != DecodeMode.NONE) {
                callback.possibleResultPoints(resultPoints);
            }
            return true;
        }
        return false;
    };
    
    
    public BarcodeView(Context context) {
        super(context);
        initialize();
    }
    
    public BarcodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }
    
    public BarcodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }
    
    private void initialize() {
        decoderFactory = new DefaultDecoderFactory();
        resultHandler = new Handler(resultCallback);
        SensorManager sensorManager =
            (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(mAccelerometerSensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI);
        }
    }
    
    /**
     * Set the DecoderFactory to use. Use this to specify the formats to decode.
     *
     * Call this from UI thread only.
     *
     * @param decoderFactory the DecoderFactory creating Decoders.
     * @see DefaultDecoderFactory
     */
    public void setDecoderFactory(DecoderFactory decoderFactory) {
        Util.validateMainThread();
        
        this.decoderFactory = decoderFactory;
        if (this.decoderThread != null) {
            this.decoderThread.setDecoder(createDecoder());
        }
    }
    
    private Decoder createDecoder() {
        if (decoderFactory == null) {
            decoderFactory = createDefaultDecoderFactory();
        }
        DecoderResultPointCallback callback = new DecoderResultPointCallback();
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, callback);
        Decoder decoder = this.decoderFactory.createDecoder(hints, getCameraInstance().getCameraManager(), getFramingRect());
        callback.setDecoder(decoder);
        return decoder;
    }
    
    /**
     * @return the current DecoderFactory in use.
     */
    public DecoderFactory getDecoderFactory() {
        return decoderFactory;
    }
    
    /**
     * Decode a single barcode, then stop decoding.
     *
     * The callback will only be called on the UI thread.
     *
     * @param callback called with the barcode result, as well as possible ResultPoints
     */
    public void decodeSingle(BarcodeCallback callback) {
        this.decodeMode = DecodeMode.SINGLE;
        this.callback = callback;
        startDecoderThread();
    }
    
    /**
     * Continuously decode barcodes. The same barcode may be returned multiple times per second.
     *
     * The callback will only be called on the UI thread.
     *
     * @param callback called with the barcode result, as well as possible ResultPoints
     */
    public void decodeContinuous(BarcodeCallback callback) {
        this.decodeMode = DecodeMode.CONTINUOUS;
        this.callback = callback;
        startDecoderThread();
    }
    
    /**
     * Stop decoding, but do not stop the preview.
     */
    public void stopDecoding() {
        this.decodeMode = DecodeMode.NONE;
        this.callback = null;
        stopDecoderThread();
    }
    
    protected DecoderFactory createDefaultDecoderFactory() {
        return new DefaultDecoderFactory();
    }
    
    private void startDecoderThread() {
        stopDecoderThread(); // To be safe
        
        if (decodeMode != DecodeMode.NONE && isPreviewActive()) {
            // We only start the thread if both:
            // 1. decoding was requested
            // 2. the preview is active
            decoderThread = new DecoderThread(getCameraInstance(), createDecoder(), resultHandler);
            decoderThread.setCropRect(getPreviewFramingRect());
            decoderThread.start();
        }
    }
    
    @Override
    protected void previewStarted() {
        super.previewStarted();
        
        startDecoderThread();
    }
    
    private void stopDecoderThread() {
        if (decoderThread != null) {
            decoderThread.stop();
            decoderThread = null;
        }
    }
    
    /**
     * Stops the live preview and decoding.
     *
     * Call from the Activity's onPause() method.
     */
    @Override
    public void pause() {
        stopDecoderThread();
        
        super.pause();
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        int pointerCount = event.getPointerCount();
        if (pointerCount >= 2) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    mOldFingerSpacing = getFingerSpacing(event);
                    break;
                
                case MotionEvent.ACTION_MOVE:
                    float newFingerSpacing = getFingerSpacing(event);
                    if (mOldFingerSpacing > newFingerSpacing) {
                        handleZoom(false);
                    } else if (mOldFingerSpacing < newFingerSpacing) {
                        handleZoom(true);
                    }
                    mOldFingerSpacing = newFingerSpacing;
                    break;
            }
        }
        return true;
    }
    
    /**
     * 获取用户双指之间的距离
     */
    private float getFingerSpacing(MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            return MathUtils.distance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
        }
        return -1;
    }
    
    /**
     * 手势调整焦距
     *
     * @param zoomIn {@code true}放大<br>
     *               {@code false}缩小
     */
    private void handleZoom(boolean zoomIn) {
        if (!isPreviewActive()) {
            return;
        }
        
        CameraInstance instance = getCameraInstance();
        if (instance == null) {
            return;
        }
        
        CameraManager cameraManager = instance.getCameraManager();
        if (cameraManager == null) {
            return;
        }
        
        Camera camera = cameraManager.getCamera();
        if (camera == null) {
            return;
        }
        
        Camera.Parameters parameters = camera.getParameters();
        if (!parameters.isZoomSupported()) {
            return;
        }
        
        int zoom = parameters.getZoom();
        int maxZoom = parameters.getMaxZoom();
        int setUp = Math.round(maxZoom * 1.f / 20);
        setUp = setUp < 1 ? 1 : setUp;
        if (zoomIn && zoom < maxZoom) {
            zoom += setUp;
            if (zoom > maxZoom) {
                zoom = maxZoom;
            }
            parameters.setZoom(zoom);
            camera.setParameters(parameters);
            isStable = false;
            cameraManager.setUserChangeZoom(true);
        } else if (!zoomIn && zoom > 0) {
            zoom -= setUp;
            if (zoom < 0) {
                zoom = 0;
            }
            parameters.setZoom(zoom);
            camera.setParameters(parameters);
            isStable = false;
            cameraManager.setUserChangeZoom(true);
        }
    }
    
    /**
     * 用户双击改变焦距，单机聚焦
     */
    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isPreviewActive()) {
                CameraInstance instance = getCameraInstance();
                if (instance != null) {
                    CameraManager cameraManager = instance.getCameraManager();
                    if (cameraManager != null) {
                        Camera camera = cameraManager.getCamera();
                        if (camera != null) {
                            autoFocus(camera, 10);
                        }
                    }
                }
            }
            
            return super.onSingleTapUp(e);
        }
        
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (isPreviewActive()) {
                CameraInstance instance = getCameraInstance();
                if (instance != null) {
                    CameraManager cameraManager = instance.getCameraManager();
                    if (cameraManager != null) {
                        Camera camera = cameraManager.getCamera();
                        if (camera != null) {
                            Camera.Parameters parameters = camera.getParameters();
                            if (parameters.isZoomSupported()) {
                                int zoom = parameters.getZoom();
                                int maxZoom = parameters.getMaxZoom();
                                if (zoom >= maxZoom / 2) {
                                    parameters.setZoom(0);
                                } else {
                                    parameters.setZoom(maxZoom / 2);
                                }
                                camera.setParameters(parameters);
                                isStable = false;
                                cameraManager.setUserChangeZoom(true);
                            }
                        }
                    }
                }
            }
            
            return super.onDoubleTap(e);
        }
    });
    
    /**
     * 重力感应监听，当手机不在晃动时进行聚焦
     */
    private SensorEventListener mAccelerometerSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!isPreviewActive()) {
                return;
            }
            
            float gx = event.values[0];
            float gy = event.values[1];
            float gz = event.values[2];
            if (Math.abs(gx - mGx) <= 0.5
                && Math.abs(gy - mGy) < 0.5
                && Math.abs(gz - mGz) < 0.5) {
                if (!isStable) {
                    isStable = true;
                    CameraInstance instance = getCameraInstance();
                    if (instance != null) {
                        CameraManager cameraManager = instance.getCameraManager();
                        if (cameraManager != null) {
                            Camera camera = cameraManager.getCamera();
                            if (camera != null && !focusing) {
                                focusing = true;
                                autoFocus(camera, 10);
                            }
                        }
                    }
                }
            } else {
                isStable = false;
            }
            mGx = gx;
            mGy = gy;
            mGz = gz;
        }
        
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        
        }
    };
    
    /**
     * 自动聚焦，失败后重新尝试
     *
     * @param tryCount 重新尝试次数
     */
    private void autoFocus(Camera camera, int tryCount) {
        try {
            camera.autoFocus((success, camera1) -> {
                if (!success && tryCount > 0 && getCameraInstance() != null) {
                    // getCameraInstance() != null用来判断界面是否不可见或退出
                    autoFocus(camera1, tryCount - 1);
                } else {
                    focusing = false;
                }
            });
        } catch(Exception e) {
            if(BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            focusing =  false;
        }
    }
}
