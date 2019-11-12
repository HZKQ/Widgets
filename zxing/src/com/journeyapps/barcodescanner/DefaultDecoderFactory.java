package com.journeyapps.barcodescanner;

import android.graphics.Rect;

import com.google.zxing.DecodeHintType;
import com.google.zxing.QRCodeReader;
import com.journeyapps.barcodescanner.camera.CameraManager;

import java.util.EnumMap;
import java.util.Map;

/**
 * DecoderFactory that creates a QRCodeReader with specified hints.
 */
public class DefaultDecoderFactory implements DecoderFactory {
    private Map<DecodeHintType, ?> hints;
    private String characterSet;
    private int scanType;
    
    public DefaultDecoderFactory() {
    }
    
    public DefaultDecoderFactory(Map<DecodeHintType, ?> hints, String characterSet, int scanType) {
        this.hints = hints;
        this.characterSet = characterSet;
        this.scanType = scanType;
    }
    
    @Override
    public Decoder createDecoder(Map<DecodeHintType, ?> baseHints, CameraManager cameraManager, Rect framingRectSize) {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.putAll(baseHints);
        hints.put(DecodeHintType.TRY_HARDER, true);
        
        if (this.hints != null) {
            hints.putAll(this.hints);
        }
        
        if (characterSet != null) {
            hints.put(DecodeHintType.CHARACTER_SET, characterSet);
        }
        
        QRCodeReader reader = new QRCodeReader();
        reader.setHints(hints);
        reader.setCameraManager(cameraManager);
        reader.setFramingRect(framingRectSize);
        switch (scanType) {
            case 0:
                return new Decoder(reader);
            case 1:
                return new InvertedDecoder(reader);
            case 2:
                return new MixedDecoder(reader);
            default:
                return new Decoder(reader);
        }
    }
}
