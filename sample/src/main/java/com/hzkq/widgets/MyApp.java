package com.hzkq.widgets;

import android.content.Context;

import java.util.Locale;
import java.util.Map;

import androidx.multidex.MultiDex;
import me.zhouzhuo810.magpiex.app.BaseApplication;

public class MyApp extends BaseApplication {
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public Map<Integer, Locale> getSupportLanguages() {
        return null;
    }
    
    @Override
    public boolean isScreenAdaptDisable() {
        return false;
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
