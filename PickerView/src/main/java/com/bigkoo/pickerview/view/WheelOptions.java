package com.bigkoo.pickerview.view;

import android.graphics.Typeface;
import android.view.View;

import com.bigkoo.pickerview.R;
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter;
import com.bigkoo.pickerview.adapter.WheelAdapter;
import com.bigkoo.pickerview.lib.WheelView;
import com.bigkoo.pickerview.listener.OnItemSelectedListener;

import java.util.List;

import androidx.annotation.Nullable;

public class WheelOptions<T1, T2, T3> {
    private View view;
    private final WheelView<T1> wv_option1;
    @Nullable
    private final WheelView<T2> wv_option2;
    @Nullable
    private final WheelView<T3> wv_option3;
    
    private List<T1> mOptions1Items;
    private List<List<T2>> mOptions2Items;
    private List<List<List<T3>>> mOptions3Items;
    private final boolean linkage;
    private OnItemSelectedListener mWheelListenerOption2;
    
    // 文字的颜色和分割线的颜色
    private int textColorOut;
    private int textColorCenter;
    private int dividerColor;
    private int itemsVisible;
    
    private WheelView.DividerType dividerType;
    
    // 条目间距倍数
    float lineSpacingMultiplier = 1.6F;
    
    public View getView() {
        return view;
    }
    
    public void setView(View view) {
        this.view = view;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public WheelOptions(View view, boolean linkage) {
        super();
        this.linkage = linkage;
        this.view = view;
        // 初始化时显示的数据
        wv_option1 = (WheelView) view.findViewById(R.id.options1);
        wv_option2 = (WheelView) view.findViewById(R.id.options2);
        wv_option3 = (WheelView) view.findViewById(R.id.options3);
    }
    
    public void setPicker(List<T1> options1Items,
                          List<List<T2>> options2Items,
                          List<List<List<T3>>> options3Items) {
        this.mOptions1Items = options1Items;
        this.mOptions2Items = options2Items == null || options2Items.size() == 0 ? null : options2Items;
        this.mOptions3Items = options3Items == null || options3Items.size() == 0 ? null : options3Items;
        int len = ArrayWheelAdapter.DEFAULT_LENGTH;
        if (this.mOptions3Items == null) {
            len = 8;
        }
        
        if (this.mOptions2Items == null) {
            len = 12;
        }
        
        // 选项1
        wv_option1.setAdapter(new ArrayWheelAdapter<>(mOptions1Items, len));
        // 初始化时显示的数据
        wv_option1.setCurrentItem(0);
        wv_option1.setIsOptions(true);
        
        if (wv_option2 != null) {
            // 选项2
            if (mOptions2Items != null) {
                wv_option2.setAdapter(new ArrayWheelAdapter<>(mOptions2Items.get(0)));
            }
            // 初始化时显示的数据
            wv_option2.setCurrentItem(wv_option1.getCurrentItem());
            
            wv_option2.setIsOptions(true);
            if (this.mOptions2Items == null) {
                wv_option2.setVisibility(View.GONE);
            } else {
                wv_option2.setVisibility(View.VISIBLE);
            }
        }
        
        if (wv_option3 != null) {
            // 选项3
            if (mOptions3Items != null) {
                List<List<T3>> lists = mOptions3Items.get(0);
                wv_option3.setAdapter(new ArrayWheelAdapter<>(lists != null && lists.size() > 0 ? lists.get(0) : null));
            }
            wv_option3.setCurrentItem(wv_option3.getCurrentItem());
            
            wv_option3.setIsOptions(true);
            if (this.mOptions3Items == null) {
                wv_option3.setVisibility(View.GONE);
            } else {
                wv_option3.setVisibility(View.VISIBLE);
            }
        }
        
        // 添加联动监听
        if (options2Items != null && linkage && wv_option2 != null) {
            // 联动监听器
            // 上一个opt2的选中位置
            // 新opt2的位置，判断如果旧位置没有超过数据范围，则沿用旧位置，否则选中最后一项
            OnItemSelectedListener wheelListener_option1 = index -> {
                int opt2Select = 0;
                if (mOptions2Items != null) {
                    List<T2> ts = index >= 0 && index < mOptions2Items.size() ? mOptions2Items.get(index) : null;
                    int size = ts == null ? -1 : ts.size() - 1;
                    opt2Select = wv_option2.getCurrentItem();//上一个opt2的选中位置
                    //新opt2的位置，判断如果旧位置没有超过数据范围，则沿用旧位置，否则选中最后一项
                    opt2Select = Math.min(opt2Select, size);
                    wv_option2.setAdapter(new ArrayWheelAdapter<>(ts));
                    wv_option2.setCurrentItem(opt2Select);
                }
                
                if (mOptions3Items != null && mWheelListenerOption2 != null) {
                    mWheelListenerOption2.onItemSelected(opt2Select);
                }
            };
            wv_option1.setOnItemSelectedListener(wheelListener_option1);
        }
        
        if (options3Items != null && linkage && wv_option2 != null && wv_option3 != null) {
            mWheelListenerOption2 = index -> {
                if (mOptions3Items != null) {
                    int opt1Select = wv_option1.getCurrentItem();
                    opt1Select = Math.min(opt1Select, mOptions3Items.size() - 1);
                    if (mOptions2Items != null && index >= 0) {
                        List<T2> ts = opt1Select >= 0 && opt1Select < mOptions2Items.size() ? mOptions2Items.get(opt1Select) : null;
                        int size = ts == null ? -1 : ts.size() - 1;
                        index = Math.min(index, size);
                    } else {
                        index = -1;
                    }
                    
                    List<List<T3>> lists = opt1Select >= 0 && opt1Select < mOptions3Items.size() ? mOptions3Items.get(opt1Select) : null;
                    List<T3> tList = lists == null || lists.size() == 0 || index < 0 || index >= lists.size() ? null : lists.get(index);
                    int opt3 = wv_option3.getCurrentItem();//上一个opt3的选中位置
                    //新opt3的位置，判断如果旧位置没有超过数据范围，则沿用旧位置，否则选中最后一项
                    int size = tList == null ? -1 : tList.size() - 1;
                    opt3 = Math.min(opt3, size);
                    wv_option3.setAdapter(new ArrayWheelAdapter<>(tList));
                    wv_option3.setCurrentItem(opt3);
                }
            };
            
            wv_option2.setOnItemSelectedListener(mWheelListenerOption2);
        }
    }
    
    // 不联动情况下
    public void setNPicker(List<T1> options1Items,
                           List<T2> options2Items,
                           List<T3> options3Items) {
        this.mOptions1Items = options1Items;
        // 不联动数据
        int len = ArrayWheelAdapter.DEFAULT_LENGTH;
        if (options3Items == null) {
            len = 8;
        }
        
        if (options2Items == null) {
            len = 12;
        }
        
        // 选项1
        wv_option1.setAdapter(new ArrayWheelAdapter<>(mOptions1Items, len));
        wv_option1.setCurrentItem(0);
        wv_option1.setIsOptions(true);
        
        if (wv_option2 != null) {
            // 选项2
            if (options2Items != null) {
                wv_option2.setAdapter(new ArrayWheelAdapter<>(options2Items));
            }
            wv_option2.setCurrentItem(wv_option1.getCurrentItem());
            
            wv_option2.setIsOptions(true);
            if (options2Items == null) {
                wv_option2.setVisibility(View.GONE);
            } else {
                wv_option2.setVisibility(View.VISIBLE);
            }
        }
        
        if (wv_option3 != null) {
            // 选项3
            if (options3Items != null) {
                wv_option3.setAdapter(new ArrayWheelAdapter<>(options3Items));
            }
            wv_option3.setCurrentItem(wv_option3.getCurrentItem());
            wv_option3.setIsOptions(true);
            if (options3Items == null) {
                wv_option3.setVisibility(View.GONE);
            } else {
                wv_option3.setVisibility(View.VISIBLE);
            }
        }
    }
    
    public void setTextContentSize(int textSize) {
        wv_option1.setTextSize(textSize);
        
        if (wv_option2 != null) {
            wv_option2.setTextSize(textSize);
        }
        
        if (wv_option3 != null) {
            wv_option3.setTextSize(textSize);
        }
    }
    
    public void setTextOutSize(int textSize) {
        wv_option1.setTextOutSize(textSize);
        
        if (wv_option2 != null) {
            wv_option2.setTextOutSize(textSize);
        }
        
        if (wv_option3 != null) {
            wv_option3.setTextOutSize(textSize);
        }
    }
    
    private void setTextColorOut() {
        wv_option1.setTextColorOut(textColorOut);
        
        if (wv_option2 != null) {
            wv_option2.setTextColorOut(textColorOut);
        }
        
        if (wv_option3 != null) {
            wv_option3.setTextColorOut(textColorOut);
        }
    }
    
    private void setTextColorCenter() {
        wv_option1.setTextColorCenter(textColorCenter);
        
        if (wv_option2 != null) {
            wv_option2.setTextColorCenter(textColorCenter);
        }
        
        if (wv_option3 != null) {
            wv_option3.setTextColorCenter(textColorCenter);
        }
        
    }
    
    private void setDividerColor() {
        wv_option1.setDividerColor(dividerColor);
        
        if (wv_option2 != null) {
            wv_option2.setDividerColor(dividerColor);
        }
        
        if (wv_option3 != null) {
            wv_option3.setDividerColor(dividerColor);
        }
    }
    
    private void setDividerType() {
        wv_option1.setDividerType(dividerType);
        
        if (wv_option2 != null) {
            wv_option2.setDividerType(dividerType);
        }
        
        if (wv_option3 != null) {
            wv_option3.setDividerType(dividerType);
        }
    }
    
    private void setLineSpacingMultiplier() {
        wv_option1.setLineSpacingMultiplier(lineSpacingMultiplier);
        
        if (wv_option2 != null) {
            wv_option2.setLineSpacingMultiplier(lineSpacingMultiplier);
        }
        
        if (wv_option3 != null) {
            wv_option3.setLineSpacingMultiplier(lineSpacingMultiplier);
        }
    }
    
    private void setItemsVisible() {
        wv_option1.setItemsVisible(itemsVisible);
        
        if (wv_option2 != null) {
            wv_option2.setItemsVisible(itemsVisible);
        }
        
        if (wv_option3 != null) {
            wv_option3.setItemsVisible(itemsVisible);
        }
    }
    
    /**
     * 设置选项的单位
     *
     * @param label1 单位
     * @param label2 单位
     * @param label3 单位
     */
    public void setLabels(String label1, String label2, String label3) {
        if (label1 != null) {
            wv_option1.setLabel(label1);
        }
        
        if (label2 != null && wv_option2 != null) {
            wv_option2.setLabel(label2);
        }
        
        if (label3 != null && wv_option3 != null) {
            wv_option3.setLabel(label3);
        }
    }
    
    /**
     * 设置x轴偏移量
     */
    public void setTextXOffset(int xoffset_one, int xoffset_two, int xoffset_three) {
        wv_option1.setTextXOffset(xoffset_one);
        
        if (wv_option2 != null) {
            wv_option2.setTextXOffset(xoffset_two);
        }
        
        if (wv_option3 != null) {
            wv_option3.setTextXOffset(xoffset_three);
        }
    }
    
    /**
     * 设置是否循环滚动
     *
     * @param cyclic 是否循环
     */
    public void setCyclic(boolean cyclic) {
        wv_option1.setCyclic(cyclic);
        
        if (wv_option2 != null) {
            wv_option2.setCyclic(cyclic);
        }
        
        if (wv_option3 != null) {
            wv_option3.setCyclic(cyclic);
        }
    }
    
    /**
     * 设置字体样式
     *
     * @param font 系统提供的几种样式
     */
    public void setTypeface(Typeface font) {
        wv_option1.setTypeface(font);
        
        if (wv_option2 != null) {
            wv_option2.setTypeface(font);
        }
        
        if (wv_option3 != null) {
            wv_option3.setTypeface(font);
        }
    }
    
    /**
     * 分别设置第一二三级是否循环滚动
     *
     * @param cyclic1,cyclic2,cyclic3 是否循环
     */
    public void setCyclic(boolean cyclic1, boolean cyclic2, boolean cyclic3) {
        wv_option1.setCyclic(cyclic1);
        
        if (wv_option2 != null) {
            wv_option2.setCyclic(cyclic2);
        }
        
        if (wv_option3 != null) {
            wv_option3.setCyclic(cyclic3);
        }
    }
    
    
    /**
     * 返回当前选中的结果对应的位置数组 因为支持三级联动效果，分三个级别索引，0，1，2。
     * 在快速滑动未停止时，点击确定按钮，会进行判断，如果匹配数据越界，则设为0，防止index出错导致崩溃。
     *
     * @return 索引数组
     */
    public int[] getCurrentItems() {
        int[] currentItems = new int[3];
        currentItems[0] = wv_option1.getCurrentItem();
        
        if (wv_option2 != null) {
            if (mOptions2Items != null && mOptions2Items.size() > 0) {
                // 非空判断
                currentItems[1] = wv_option2.getCurrentItem() > (mOptions2Items.get(currentItems[0]).size() - 1) ? 0 : wv_option2.getCurrentItem();
            } else {
                currentItems[1] = wv_option2.getCurrentItem();
            }
        }
        
        if (wv_option3 != null) {
            if (mOptions3Items != null && mOptions3Items.size() > 0) {
                // 非空判断
                currentItems[2] = wv_option3.getCurrentItem() > (mOptions3Items.get(currentItems[0]).get(currentItems[1]).size() - 1) ? 0 : wv_option3.getCurrentItem();
            } else {
                currentItems[2] = wv_option3.getCurrentItem();
            }
        }
        
        return currentItems;
    }
    
    public void setCurrentItems(int option1, int option2, int option3) {
        if (linkage) {
            itemSelected(option1, option2, option3);
        }
        
        wv_option1.setCurrentItem(option1);
        
        if (wv_option2 != null) {
            wv_option2.setCurrentItem(option2);
        }
        
        if (wv_option3 != null) {
            wv_option3.setCurrentItem(option3);
        }
    }
    
    private void itemSelected(int opt1Select, int opt2Select, int opt3Select) {
        if (mOptions2Items != null && wv_option2 != null) {
            wv_option2.setAdapter(new ArrayWheelAdapter<>(mOptions2Items.get(opt1Select)));
            wv_option2.setCurrentItem(opt2Select);
        }
        
        if (mOptions3Items != null && wv_option3 != null) {
            wv_option3.setAdapter(new ArrayWheelAdapter<>(mOptions3Items.get(opt1Select).get(opt2Select)));
            wv_option3.setCurrentItem(opt3Select);
        }
    }
    
    /**
     * 设置间距倍数,但是只能在1.2-2.0f之间
     */
    public void setLineSpacingMultiplier(float lineSpacingMultiplier) {
        this.lineSpacingMultiplier = lineSpacingMultiplier;
        setLineSpacingMultiplier();
    }
    
    /**
     * 设置分割线的颜色
     */
    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
        setDividerColor();
    }
    
    /**
     * 设置分割线的类型
     */
    public void setDividerType(WheelView.DividerType dividerType) {
        this.dividerType = dividerType;
        setDividerType();
    }
    
    /**
     * 设置分割线之间的文字的颜色
     */
    public void setTextColorCenter(int textColorCenter) {
        this.textColorCenter = textColorCenter;
        setTextColorCenter();
    }
    
    /**
     * 设置分割线以外文字的颜色
     */
    public void setTextColorOut(int textColorOut) {
        this.textColorOut = textColorOut;
        setTextColorOut();
    }
    
    public void setItemsVisible(int itemsVisible) {
        this.itemsVisible = itemsVisible;
        setItemsVisible();
    }
    
    /**
     * Label 是否只显示中间选中项的
     */
    public void isCenterLabel(boolean isCenterLabel) {
        wv_option1.isCenterLabel(isCenterLabel);
        
        if (wv_option2 != null) {
            wv_option2.isCenterLabel(isCenterLabel);
        }
        
        if (wv_option3 != null) {
            wv_option3.isCenterLabel(isCenterLabel);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> List<T> getData(int index) {
        if (index < 0 || index >= 3) {
            return null;
        }
        
        if (index == 0) {
            WheelAdapter<T1> adapter = wv_option1.getAdapter();
            if (adapter instanceof ArrayWheelAdapter) {
                return (List<T>) ((ArrayWheelAdapter<T1>) adapter).getItems();
            }
            return null;
        } else if (index == 1) {
            if (wv_option2 == null) {
                return null;
            }
            
            WheelAdapter<T2> adapter = wv_option2.getAdapter();
            if (adapter instanceof ArrayWheelAdapter) {
                return (List<T>) ((ArrayWheelAdapter<T2>) adapter).getItems();
            }
            return null;
        } else {
            if (wv_option3 == null) {
                return null;
            }
            
            WheelAdapter<T3> adapter = wv_option3.getAdapter();
            if (adapter instanceof ArrayWheelAdapter) {
                return (List<T>) ((ArrayWheelAdapter<T3>) adapter).getItems();
            }
            return null;
        }
    }
}
