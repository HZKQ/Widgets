package com.hzkq.widgets.shadowlayout;

import android.content.Intent;
import android.os.Bundle;

import com.hzkq.widgets.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 首页展示
 */
public class ShadowLayoutActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shadow_layout);
        
        findViewById(R.id.ShadowLayout_shadow).setOnClickListener(v -> {
            startActivity(new Intent(ShadowLayoutActivity.this, ShadowActivity.class));
        });
        
        findViewById(R.id.ShadowLayout_shape).setOnClickListener(v -> {
            startActivity(new Intent(ShadowLayoutActivity.this, ShapeActivity.class));
        });
    }
}
