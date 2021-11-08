package com.keqiang.huaweiscan;

import android.os.Parcel;

import com.huawei.hms.ml.scan.HmsScan;

/**
 * 扫码配置项
 *
 * @author Created by wanggaowan on 2021/6/3 14:56
 */
public class ScanConfig implements android.os.Parcelable {
    private String title;
    private String hint;
    private boolean couldScanImg = true;
    private boolean imgScanFailedBack;
    private String imgScanFailedHint;
    
    private int format;
    private int[] formats;
    
    private ScanConfig() {
    
    }
    
    public static ScanConfig copy() {
        return new ScanConfig();
    }
    
    public String getTitle() {
        return title;
    }
    
    /**
     * 设置扫码标题,如果为null，则使用默认值{@link R.string#scan_code_title_label}
     */
    public ScanConfig title(String title) {
        this.title = title;
        return this;
    }
    
    public String getHint() {
        return hint;
    }
    
    /**
     * 设置扫码框底部提示文本，如果为null，则使用默认值{@link R.string#scan_code_hint}
     */
    public ScanConfig hint(String hint) {
        this.hint = hint;
        return this;
    }
    
    public boolean isCouldScanImg() {
        return couldScanImg;
    }
    
    /**
     * 设置扫码框是否可从相册选择内容
     */
    public ScanConfig couldScanImg(boolean couldScanImg) {
        this.couldScanImg = couldScanImg;
        return this;
    }
    
    public boolean isImgScanFailedBack() {
        return imgScanFailedBack;
    }
    
    /**
     * 设置扫描图片失败，是否退出扫码界面
     */
    public ScanConfig imgScanFailedBack(boolean imgScanFailedBack) {
        this.imgScanFailedBack = imgScanFailedBack;
        return this;
    }
    
    public String getImgScanFailedHint() {
        return imgScanFailedHint;
    }
    
    /**
     * 如果扫描图片失败且不退出扫码界面，则提示此文本。如果此值为null，则使用默认值{@link R.string#image_scan_error_hint}
     */
    public ScanConfig imgScanFailedHint(String imgScanFailedHint) {
        this.imgScanFailedHint = imgScanFailedHint;
        return this;
    }
    
    public int getFormat() {
        return format;
    }
    
    public int[] getFormats() {
        return formats;
    }
    
    /**
     * 设置扫码支持的格式，具体可查看{@link HmsScan.SCAN_TYPE}
     *
     * @param format  至少指定一个扫码格式
     * @param formats 其它需要支持的扫码格式
     */
    public ScanConfig formats(int format, int... formats) {
        this.format = format;
        this.formats = formats;
        return this;
    }
    
    @Override
    public int describeContents() { return 0; }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.hint);
        dest.writeByte(this.couldScanImg ? (byte) 1 : (byte) 0);
        dest.writeInt(this.format);
        dest.writeIntArray(this.formats);
    }
    
    protected ScanConfig(Parcel in) {
        this.title = in.readString();
        this.hint = in.readString();
        this.couldScanImg = in.readByte() != 0;
        this.format = in.readInt();
        this.formats = in.createIntArray();
    }
    
    public static final Creator<ScanConfig> CREATOR = new Creator<ScanConfig>() {
        @Override
        public ScanConfig createFromParcel(Parcel source) { return new ScanConfig(source);}
        
        @Override
        public ScanConfig[] newArray(int size) { return new ScanConfig[size];}
    };
}
