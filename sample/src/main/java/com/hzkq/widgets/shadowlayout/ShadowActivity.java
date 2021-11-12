package com.hzkq.widgets.shadowlayout;

import android.content.Intent;
import android.os.Bundle;

import com.hzkq.widgets.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by leo
 * on 2020/10/27.
 * shadow阴影的各项使用
 */
public class ShadowActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shadow);
        findViewById(R.id.shadowLayout_bar_left).setOnClickListener(v -> finish());
        
        findViewById(R.id.ShadowLayoutIntent).setOnClickListener(v ->
            startActivity(new Intent(ShadowActivity.this, StarShowActivity.class)));
    }
}
