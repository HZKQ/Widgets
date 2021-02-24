package com.hzkq.widgets;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.keqiang.countdownview.CountdownView;
import com.keqiang.countdownview.SmsCountdownView;

import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;
import me.zhouzhuo810.magpiex.utils.ToastUtil;

/**
 * @author Created by 汪高皖 on 2020/1/13 16:47
 */
public class CountdownActivity extends BaseActivity {
    private TitleBar mTitleBar;
    private TextView mTvStart;
    private TextView mTvEnd;
    private TextView mTvReverse;
    private CountdownView mCountdownView;
    private SmsCountdownView mSmsCountdownView;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_count_view;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mTvStart = findViewById(R.id.tv_start);
        mTvEnd = findViewById(R.id.tv_end);
        mTvReverse = findViewById(R.id.tv_reverse);
        mCountdownView = findViewById(R.id.countdown_view);
        mSmsCountdownView = findViewById(R.id.sms_countdown_view);
    }
    
    @Override
    public void initData() {
    
    }
    
    @Override
    public void initEvent() {
        mTitleBar.getLlLeft().setOnClickListener(v -> closeAct());
        
        mTvStart.setOnClickListener(v -> mCountdownView.startCountdown(5, () -> ToastUtil.showToast("倒计时结束")));
        
        mTvEnd.setOnClickListener(v -> mCountdownView.endCountDown());
        
        mTvReverse.setOnClickListener(v -> {
            mCountdownView.setReverse(!mCountdownView.isReverse());
            mCountdownView.endCountDown();
            mCountdownView.startCountdown(5, () -> ToastUtil.showToast("倒计时结束"));
        });
        
        mSmsCountdownView.setOnClickListener(v ->
            mSmsCountdownView.startCountdown(9, () -> {
                mSmsCountdownView.setNormalText("重新发送短信");
                ToastUtil.showToast("倒计时结束");
            }));
    }
}
