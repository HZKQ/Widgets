package com.keqiang.countdownview;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * @author Created by 汪高皖 on 2020/1/14 10:05
 */
public class SmsCountdownView extends AppCompatTextView {
    public static final IValueFormat DEFAULT_VALUE_FORMAT = second -> second + "s";
    
    private int mCurDuration;
    private Handler mHandler;
    private CountdownListener mCountdownListener;
    private IValueFormat mIValueFormat = DEFAULT_VALUE_FORMAT;
    private String mNormalText;
    private boolean mStartCountdown;
    
    public SmsCountdownView(@NonNull Context context) {
        this(context, null);
    }
    
    public SmsCountdownView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public SmsCountdownView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        mHandler = new Handler();
        mNormalText = getText().toString();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
    }
    
    /**
     * 开始倒计时,开始计时后，按钮不可用
     *
     * @param second            倒计时时长，单位S
     * @param countdownListener 倒计时监听
     */
    public void startCountdown(int second, final CountdownListener countdownListener) {
        if (second <= 0) {
            return;
        }
        
        mCountdownListener = countdownListener;
        mCurDuration = second;
        String format = mIValueFormat.format(mCurDuration);
        setText(format);
        setEnabled(false);
        mStartCountdown = true;
        countdown();
    }
    
    private void countdown() {
        mHandler.postDelayed(() -> {
            mCurDuration -= 1;
            if (mCurDuration == 0) {
                mStartCountdown = false;
                setEnabled(true);
                setText(mNormalText);
                if (mCountdownListener != null) {
                    mCountdownListener.onEnd();
                }
            } else {
                String format = mIValueFormat.format(mCurDuration);
                setText(format);
                countdown();
            }
        }, 1000);
    }
    
    /**
     * 结束倒计时，按钮可用，调用此方法将直接触发{@link CountdownListener#onEnd()}
     */
    public void endCountDown() {
        mStartCountdown = false;
        mHandler.removeCallbacksAndMessages(null);
        setEnabled(true);
        setText(mNormalText);
        if (mCountdownListener != null) {
            mCountdownListener.onEnd();
        }
    }
    
    /**
     * 非倒计时显示的文本
     */
    public void setNormalText(String normalText) {
        mNormalText = normalText;
        if (!mStartCountdown) {
            setText(mNormalText);
        }
    }
    
    /**
     * 是否开启了倒计时
     */
    public boolean isStartCountdown() {
        return mStartCountdown;
    }
}
