package com.hzkq.widgets.shadowlayout;

import android.os.Bundle;

import com.hzkq.widgets.R;
import com.lihang.ShadowLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * shape功能的各项使用
 */
public class ShapeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape);
        
        ShadowLayout shadowLayout = findViewById(R.id.ShadowLayout_image);
        shadowLayout.setSelected(true);
        shadowLayout.setOnClickListener(v -> shadowLayout.setSelected(!shadowLayout.isSelected()));
        
        findViewById(R.id.shadowLayout_bar_left).setOnClickListener(v -> finish());
    
        ShadowLayout shadowLayout2 =  findViewById(R.id.shadowLayout_select);
        shadowLayout2.setOnClickListener(v -> shadowLayout2.setSelected(!shadowLayout2.isSelected()));

        ShadowLayout shadowLayout3 = findViewById(R.id.shadowLayout_bindView);
        shadowLayout3.setOnClickListener(v -> shadowLayout3.setSelected(!shadowLayout3.isSelected()));
    }
}
