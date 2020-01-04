package com.github.mikephil.charting.custom.linechart;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.List;

/**
 * 显示固定标签数量，当给定的标签数量大于固定显示数量时，间隔显示内容。<br>
 * 注意：使用此类时，chart必须使用{@link SmartXAxis}且设置{@link SmartXAxis#setCustomCalculateXOffset(boolean)}为true，否则始终返回""
 *
 * @author Created by 汪高皖 on 2018/10/17 0017 08:55
 */
public class FixLabelCountValueFormat<T extends FixLabelCountValueFormat.ILabel> extends ValueFormatter {
    
    private List<T> mTotalLabel;
    private int mSpace;
    private boolean mMustShowLastLabel;
    
    /**
     * 如果totalLabel.size() > maxShowLabelCount,那么标签将间隔显示,间距为Math.ceil(totalLabel.Size() *1.f / maxShowLabelCount)
     *
     * @param totalLabel        总标签数据
     * @param maxShowLabelCount 最多可显示的标签数量,>=1
     */
    public FixLabelCountValueFormat(List<T> totalLabel, int maxShowLabelCount) {
        this(totalLabel, maxShowLabelCount, true);
    }
    
    /**
     * 如果totalLabel.size() > maxShowLabelCount,那么标签将间隔显示,间距为Math.ceil(totalLabel.Size() *1.f / maxShowLabelCount)
     *
     * @param totalLabel        总标签数据
     * @param showLabelCount    最多可显示的标签数量,>=1
     * @param mustShowLastLabel 是否必须显示总标签的最后一个标签,默认值true
     */
    public FixLabelCountValueFormat(List<T> totalLabel, int showLabelCount, boolean mustShowLastLabel) {
        mTotalLabel = totalLabel;
        if (mTotalLabel == null || mTotalLabel.size() == 0 || showLabelCount < 1) {
            mSpace = 0;
        } else {
            mSpace = (int) Math.ceil(mTotalLabel.size() * 1.f / showLabelCount);
        }
        mMustShowLastLabel = mustShowLastLabel;
    }
    
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if (!(axis instanceof SmartXAxis) || !((SmartXAxis) axis).isCustomCalculateXOffset()) {
            return "";
        }
        
        if (mSpace == 0) {
            return "";
        }
        
        int index = (int) value;
        if (index >= mTotalLabel.size()) {
            return "";
        }
        
        if (index % mSpace == 0) {
            if (mMustShowLastLabel) {
                int length = mTotalLabel.size() - 1 - index;
                if (length >= mSpace) {
                    // 因为需要显示开始和结束标签，所以只有中间的标签离最后一个标签的距离大于Space才显示，否则可能和最后一个标签label重合
                    String label = mTotalLabel.get(index).getLabel();
                    return label == null ? "" : label;
                }
            } else {
                String label = mTotalLabel.get(index).getLabel();
                return label == null ? "" : label;
            }
        }
        
        if (mMustShowLastLabel && index == mTotalLabel.size() - 1) {
            String label = mTotalLabel.get(index).getLabel();
            return label == null ? "" : label;
        }
        
        return "";
    }
    
    public interface ILabel {
        String getLabel();
    }
}
