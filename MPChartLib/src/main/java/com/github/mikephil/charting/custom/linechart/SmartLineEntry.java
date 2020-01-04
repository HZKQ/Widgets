package com.github.mikephil.charting.custom.linechart;

import android.graphics.drawable.Drawable;
import android.os.Parcel;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

/**
 * @author Created by 汪高皖 on 2020/1/4 9:28
 */
public class SmartLineEntry extends Entry {
    /**
     * 是否绘制该节点，默认值{@code true}，此值目前只针对{@link SmartLineDataSet#setMode(LineDataSet.Mode)}值为{@link LineDataSet.Mode#LINEAR}有效
     */
    private boolean draw = true;
    
    public SmartLineEntry() {
    
    }
    
    /**
     * A Entry represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     */
    public SmartLineEntry(float x, float y) {
        super(x, y);
    }
    
    /**
     * A Entry represents one single entry in the chart.
     *
     * @param x    the x value
     * @param y    the y value (the actual value of the entry)
     * @param data Spot for additional data this Entry represents.
     */
    public SmartLineEntry(float x, float y, Object data) {
        super(x, y, data);
    }
    
    /**
     * A Entry represents one single entry in the chart.
     *
     * @param x    the x value
     * @param y    the y value (the actual value of the entry)
     * @param icon icon image
     */
    public SmartLineEntry(float x, float y, Drawable icon) {
        super(x, y, icon);
    }
    
    /**
     * A Entry represents one single entry in the chart.
     *
     * @param x    the x value
     * @param y    the y value (the actual value of the entry)
     * @param icon icon image
     * @param data Spot for additional data this Entry represents.
     */
    public SmartLineEntry(float x, float y, Drawable icon, Object data) {
        super(x, y, icon, data);
    }
    
    public boolean equalTo(SmartLineEntry e) {
        boolean equalTo = super.equalTo(e);
        if (!equalTo) {
            return false;
        }
        return e.isDraw() == this.isDraw();
    }
    
    public boolean isDraw() {
        return draw;
    }
    
    public void setDraw(boolean draw) {
        this.draw = draw;
    }
    
    @Override
    public SmartLineEntry copy() {
        SmartLineEntry e = new SmartLineEntry(getX(), getY(), getData());
        e.setDraw(this.draw);
        return e;
    }
    
    @Override
    public int describeContents() { return 0; }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(this.draw ? (byte) 1 : (byte) 0);
    }
    
    protected SmartLineEntry(Parcel in) {
        super(in);
        this.draw = in.readByte() != 0;
    }
    
    public static final Creator<SmartLineEntry> CREATOR = new Creator<SmartLineEntry>() {
        @Override
        public SmartLineEntry createFromParcel(Parcel source) {return new SmartLineEntry(source);}
        
        @Override
        public SmartLineEntry[] newArray(int size) {return new SmartLineEntry[size];}
    };
}
