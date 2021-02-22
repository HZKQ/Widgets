package com.google.zxing.oned;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Map;

/**
 * 条码生成工具
 */
public class OneDEncoder {
    private static final int BLACK = 0xFF000000;
    
    private OneDEncoder() {
        
    }
    
    /**
     * 根据矩阵数据创建条码
     */
    public static Bitmap createBitmap(BitMatrix matrix) {
        return createBitmap(matrix, BLACK);
    }
    
    /**
     * 根据矩阵数据创建条码
     *
     * @param color barcode color
     */
    public static Bitmap createBitmap(BitMatrix matrix, int color) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (matrix.get(j, i))
                    pixels[i * width + j] = color;
            }
        }
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
    
    public static BitMatrix encode(String contents, int width, int height) throws WriterException {
        try {
            return new Code128Writer().encode(contents, BarcodeFormat.CODE_128, width, height);
        } catch (WriterException e) {
            throw e;
        } catch (Exception e) {
            // ZXing sometimes throws an IllegalArgumentException
            throw new WriterException(e);
        }
    }
    
    public static BitMatrix encode(String contents, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        try {
            return new Code128Writer().encode(contents, BarcodeFormat.CODE_128, width, height, hints);
        } catch (WriterException e) {
            throw e;
        } catch (Exception e) {
            throw new WriterException(e);
        }
    }
    
    public static Bitmap encodeBitmap(String contents, int width, int height) throws WriterException {
        return createBitmap(encode(contents, width, height));
    }
    
    public static Bitmap encodeBitmap(String contents, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        return createBitmap(encode(contents, width, height, hints));
    }
    
    /**
     * @param color barcode color
     */
    public static Bitmap encodeBitmap(String contents, int width, int height, int color) throws WriterException {
        return createBitmap(encode(contents, width, height), color);
    }
    
    /**
     * @param color barcode color
     */
    public static Bitmap encodeBitmap(String contents, int width, int height, int color, Map<EncodeHintType, ?> hints) throws WriterException {
        return createBitmap(encode(contents, width, height, hints), color);
    }
}
