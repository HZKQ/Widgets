package com.bigkoo.pickerview.lib;

import android.os.Handler;
import android.os.Message;

final class MessageHandler extends Handler {
    public static final int WHAT_INVALIDATE_LOOP_VIEW = 1000;
    public static final int WHAT_SMOOTH_SCROLL = 2000;
    public static final int WHAT_ITEM_SELECTED = 3000;
    
    private final WheelView loopView;
    
    MessageHandler(WheelView loopView) {
        this.loopView = loopView;
    }
    
    @Override
    public final void handleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_INVALIDATE_LOOP_VIEW:
                loopView.invalidate();
                break;
            
            case WHAT_SMOOTH_SCROLL:
                loopView.smoothScroll(WheelView.ACTION.FLING);
                break;
            
            case WHAT_ITEM_SELECTED:
                loopView.onItemSelected();
                break;
        }
    }
    
}
