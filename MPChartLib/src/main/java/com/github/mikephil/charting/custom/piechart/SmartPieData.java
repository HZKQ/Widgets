package com.github.mikephil.charting.custom.piechart;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

/**
 * @author Created by 汪高皖 on 2018/4/16 0016 14:46
 */
public class SmartPieData extends ChartData<ISmartPieDataSet> {
    public SmartPieData() {
        super();
    }
    
    public SmartPieData(ISmartPieDataSet dataSet) {
        super(dataSet);
    }
    
    /**
     * Sets the PieDataSet this data object should represent.
     *
     * @param dataSet
     */
    public void setDataSet(ISmartPieDataSet dataSet) {
        mDataSets.clear();
        mDataSets.add(dataSet);
        notifyDataChanged();
    }
    
    /**
     * Returns the DataSet this PieData object represents. A PieData object can
     * only contain one DataSet.
     *
     * @return
     */
    public ISmartPieDataSet getDataSet() {
        return mDataSets.get(0);
    }
    
    /**
     * The PieData object can only have one DataSet. Use getDataSet() method instead.
     *
     * @param index
     * @return
     */
    @Override
    public ISmartPieDataSet getDataSetByIndex(int index) {
        return index == 0 ? getDataSet() : null;
    }
    
    @Override
    public ISmartPieDataSet getDataSetByLabel(String label, boolean ignorecase) {
        return ignorecase ? label.equalsIgnoreCase(mDataSets.get(0).getLabel()) ? mDataSets.get(0)
            : null : label.equals(mDataSets.get(0).getLabel()) ? mDataSets.get(0) : null;
    }
    
    @Override
    public Entry getEntryForHighlight(Highlight highlight) {
        return getDataSet().getEntryForIndex((int) highlight.getX());
    }
    
    /**
     * Returns the sum of all values in this PieData object.
     *
     * @return
     */
    public float getYValueSum() {
        
        float sum = 0;
        
        for (int i = 0; i < getDataSet().getEntryCount(); i++)
            sum += getDataSet().getEntryForIndex(i).getY();
        
        
        return sum;
    }
}
