package com.journeyapps.barcodescanner;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.R;
import com.journeyapps.barcodescanner.camera.CameraInstance;
import com.journeyapps.barcodescanner.camera.PreviewCallback;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class DecoderThread {
    private static final String TAG = DecoderThread.class.getSimpleName();
    
    private CameraInstance cameraInstance;
    private HandlerThread thread;
    private Handler handler;
    private Decoder decoder;
    private Handler resultHandler;
    private Rect cropRect;
    private boolean running = false;
    private final Object LOCK = new Object();
    private ExecutorService mThreadPool;
    
    private final Handler.Callback callback = message -> {
        if (message.what == R.id.zxing_decode) {
            decode((SourceData) message.obj);
        } else if (message.what == R.id.zxing_preview_failed) {
            // Error already logged. Try again.
            requestNextPreview();
        }
        return true;
    };
    
    public DecoderThread(CameraInstance cameraInstance, Decoder decoder, Handler resultHandler) {
        Util.validateMainThread();
        
        this.cameraInstance = cameraInstance;
        this.decoder = decoder;
        this.resultHandler = resultHandler;
    }
    
    public Decoder getDecoder() {
        return decoder;
    }
    
    public void setDecoder(Decoder decoder) {
        this.decoder = decoder;
    }
    
    public Rect getCropRect() {
        return cropRect;
    }
    
    public void setCropRect(Rect cropRect) {
        this.cropRect = cropRect;
    }
    
    /**
     * Start decoding.
     *
     * This must be called from the UI thread.
     */
    public void start() {
        Util.validateMainThread();
        synchronized (LOCK) {
            if (running) {
                return;
            }
            
            // 最大线程数量定为75% CPU核心数，防止线程过多导致内存溢出崩溃，华为手机出现过此情况
            int maximumPoolSize = Runtime.getRuntime().availableProcessors() * 3 / 4;
            if (maximumPoolSize < 1) {
                maximumPoolSize = 1;
            }
            
            mThreadPool = new ThreadPoolExecutor(
                0,
                maximumPoolSize,
                10L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                // 线程池数量>=maximumPoolSize且SynchronousQueue队列已满，则丢弃新加入的执行任务
                new ThreadPoolExecutor.DiscardPolicy());
            
            thread = new HandlerThread(TAG);
            thread.start();
            handler = new Handler(thread.getLooper(), callback);
            running = true;
            requestNextPreview();
        }
    }
    
    /**
     * Stop decoding.
     *
     * This must be called from the UI thread.
     */
    public void stop() {
        Util.validateMainThread();
        synchronized (LOCK) {
            if (!running) {
                return;
            }
            running = false;
            handler.removeCallbacksAndMessages(null);
            thread.quit();
            if (!mThreadPool.isShutdown()) {
                mThreadPool.shutdown();
            }
        }
    }
    
    private final PreviewCallback previewCallback = new PreviewCallback() {
        @Override
        public void onPreview(SourceData sourceData) {
            // Only post if running, to prevent a warning like this:
            //   java.lang.RuntimeException: Handler (android.os.Handler) sending message to a Handler on a dead thread
            
            // synchronize to handle cases where this is called concurrently with stop()
            synchronized (LOCK) {
                if (running) {
                    // Post to our thread.
                    handler.obtainMessage(R.id.zxing_decode, sourceData).sendToTarget();
                }
            }
        }
        
        @Override
        public void onPreviewError(Exception e) {
            synchronized (LOCK) {
                if (running) {
                    // Post to our thread.
                    handler.obtainMessage(R.id.zxing_preview_failed).sendToTarget();
                }
            }
        }
    };
    
    private void requestNextPreview() {
        cameraInstance.requestPreview(previewCallback);
    }
    
    protected LuminanceSource createSource(SourceData sourceData) {
        if (this.cropRect == null) {
            return null;
        } else {
            return sourceData.createSource();
        }
    }
    
    private void decode(SourceData sourceData) {
        // 由逐帧解析改为多帧并行解析
        mThreadPool.execute(() -> {
            Result rawResult = null;
            sourceData.setCropRect(cropRect);
            LuminanceSource source = createSource(sourceData);
            
            if (source != null) {
                rawResult = decoder.decode(source);
            }
            
            if (rawResult != null) {
                if (resultHandler != null) {
                    BarcodeResult barcodeResult = new BarcodeResult(rawResult, sourceData);
                    Message message = Message.obtain(resultHandler, R.id.zxing_decode_succeeded, barcodeResult);
                    Bundle bundle = new Bundle();
                    message.setData(bundle);
                    message.sendToTarget();
                }
            } else {
                if (resultHandler != null) {
                    Message message = Message.obtain(resultHandler, R.id.zxing_decode_failed);
                    message.sendToTarget();
                }
            }
            if (resultHandler != null) {
                List<ResultPoint> resultPoints = decoder.getPossibleResultPoints();
                Message message = Message.obtain(resultHandler, R.id.zxing_possible_result_points, resultPoints);
                message.sendToTarget();
            }
        });
        requestNextPreview();
    }
}
