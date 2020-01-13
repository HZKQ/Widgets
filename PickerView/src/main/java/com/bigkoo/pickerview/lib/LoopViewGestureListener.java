package com.bigkoo.pickerview.lib;

import android.view.MotionEvent;

final class LoopViewGestureListener extends android.view.GestureDetector.SimpleOnGestureListener {
    
    private final WheelView loopView;
    
    LoopViewGestureListener(WheelView loopview) {
        loopView = loopview;
    }
    
    @Override
    public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        loopView.scrollBy(velocityY);
        return true;
    }
}
