package com.bigkoo.pickerview.lib;

final class OnItemSelectedRunnable implements Runnable {
    private final WheelView loopView;
    
    OnItemSelectedRunnable(WheelView loopView) {
        this.loopView = loopView;
    }
    
    @Override
    public final void run() {
        loopView.onItemSelectedListener.onItemSelected(loopView.getCurrentItem());
    }
}
