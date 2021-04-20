package com.hzkq.widgets;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.bigkoo.pickerview.OptionsPickerView;
import com.bigkoo.pickerview.TimePickerView;
import com.bigkoo.pickerview.model.IPickerViewData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hjq.toast.ToastUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import me.zhouzhuo810.magpiex.ui.act.BaseActivity;
import me.zhouzhuo810.magpiex.ui.widget.TitleBar;
import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * 联动选择器
 *
 * @author Created by 汪高皖 on 2020/1/13 14:24
 */
public class PickerViewActivity extends BaseActivity {
    private TitleBar mTitleBar;
    private TextView mTvNormal;
    private TextView mTvTime;
    
    private TimePickerView mStartTimePicker;
    private OptionsPickerView mPvOptions;
    
    private List<String> options1Items;
    private List<List<String>> options2Items;
    private List<List<List<String>>> options3Items;
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_picker_view;
    }
    
    @Override
    public boolean shouldSupportMultiLanguage() {
        return false;
    }
    
    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.title_bar);
        mTvNormal = findViewById(R.id.tv_normal);
        mTvTime = findViewById(R.id.tv_time);
    }
    
    @Override
    public void initData() {
        //条件选择器
        mPvOptions = new OptionsPickerView.Builder(this, (options1, option2, options3, v) -> {
            //返回的分别是三个级别的选中位置
        }).setCancelColor(0xff999999)
            .setContentTextSize(SimpleUtil.getScaledValue(46, true))
            .setOutTextSize(SimpleUtil.getScaledValue(36, true))
            .setLineSpacingMultiplier(3f)
            .setLayoutRes(R.layout.pickerview_options2)
            .setGravity(Gravity.BOTTOM)
            .isDialog(false)
            .build();
        
        prepareChinaCityData();
    }
    
    @Override
    public void initEvent() {
        mTitleBar.getLlLeft().setOnClickListener(v -> closeAct());
        
        mTvTime.setOnClickListener(v -> chooseStarTime());
        
        mTvNormal.setOnClickListener(showAddressChoosePop());
    }
    
    private void chooseStarTime() {
        if (mStartTimePicker == null) {
            //时间选择器 ，自定义布局
            mStartTimePicker = new TimePickerView.Builder(this, (date, v) -> {
                // 选中事件回调
            })
                .setLayoutRes(R.layout.pickerview_custom_time, v -> {
                    final TextView tvSubmit = v.findViewById(R.id.tv_ok);
                    TextView tvCancel = v.findViewById(R.id.tv_cancel);
                    tvSubmit.setOnClickListener(v12 -> {
                        mStartTimePicker.returnData();
                        mStartTimePicker.dismiss();
                    });
                    tvCancel.setOnClickListener(v1 -> mStartTimePicker.dismiss());
                })
                .setContentSize(SimpleUtil.getScaledValue(46, true))
                .setOutSize(SimpleUtil.getScaledValue(36, true))
                .setType(new boolean[]{true, true, true, false, false, false})
                .setLabel(getString(R.string.pickerview_year),
                    getString(R.string.pickerview_month),
                    getString(R.string.pickerview_day),
                    getString(R.string.pickerview_hours),
                    getString(R.string.pickerview_minutes),
                    getString(R.string.pickerview_seconds))
                .setLineSpacingMultiplier(1.8f)
                .setTextXOffset(0, 0, 0, 0, 0, 0)
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setDividerColor(ContextCompat.getColor(this, R.color.colorLine))
                .build();
        }
        
        mStartTimePicker.setDate(Calendar.getInstance());
        mStartTimePicker.show();
    }
    
    /**
     * 展示省份城市区县选择弹窗
     */
    private View.OnClickListener showAddressChoosePop() {
        return v -> {
            if (options1Items != null && options2Items != null && options3Items != null) {
                mPvOptions.show();
            } else {
                ToastUtils.show("数据加载中，请稍等");
            }
        };
    }
    
    /**
     * 准备中国省份-城市-区县数据
     */
    private void prepareChinaCityData() {
        new Thread(() -> {
            BufferedReader responseReader = null;
            InputStream open = null;
            try {
                open = getResources().getAssets().open("china-city_data.json");
                StringBuilder sb = new StringBuilder();
                String readLine;
                responseReader = new BufferedReader(new InputStreamReader(open, "UTF-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine);
                }
                List<Province> provinceList = new Gson()
                    .fromJson(sb.toString(), new TypeToken<List<Province>>() {
                    }.getType());
                
                parseProvinceCityArea(provinceList);
                //noinspection unchecked
                mPvOptions.setPicker(options1Items, options2Items, options3Items);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (open != null) {
                    try {
                        open.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                
                if (responseReader != null) {
                    try {
                        responseReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    
    /**
     * 解析省份-城市-区域信息
     *
     * @param provinceList 省份信息，里面包含每个省份的城市信息，城市信息包含每个城市的区域信息，
     *                     由于provinceList格式和控件要求格式不一致，所以进行转化
     */
    private void parseProvinceCityArea(List<Province> provinceList) {
        options1Items = new ArrayList<>();
        options2Items = new ArrayList<>();
        options3Items = new ArrayList<>();
        
        for (int i = 0; i < provinceList.size(); i++) {//遍历省份
            options1Items.add(provinceList.get(i).getName());
            //该省的城市列表（第二级）
            List<String> cityList = new ArrayList<>();
            //该省的所有地区列表（第三极）
            List<List<String>> province_AreaList = new ArrayList<>();
            
            //遍历该省份的所有城市
            for (int c = 0; c < provinceList.get(i).getCityList().size(); c++) {
                String cityName = provinceList.get(i).getCityList().get(c).getName();
                //添加城市
                cityList.add(cityName);
                //该城市的所有地区列表
                List<String> city_AreaList = new ArrayList<>();
                //如果无地区数据，建议添加空字符串，防止数据为null 导致三个选项长度不匹配造成崩溃
                if (provinceList.get(i).getCityList().get(c).getCityList() == null
                    || provinceList.get(i).getCityList().get(c).getCityList().size() == 0) {
                    city_AreaList.add("");
                } else {
                    //该城市对应地区所有数据
                    for (int d = 0; d < provinceList.get(i).getCityList().get(c).getCityList().size(); d++) {
                        String AreaName = provinceList.get(i).getCityList().get(c).getCityList().get(d).getName();
                        //添加该城市所有地区数据
                        city_AreaList.add(AreaName);
                    }
                }
                //添加该省所有地区数据
                province_AreaList.add(city_AreaList);
            }
            //添加城市数据
            options2Items.add(cityList);
            //添加地区数据
            options3Items.add(province_AreaList);
        }
    }
    
    public static class Province implements IPickerViewData {
        private int id;
        private String name;
        private String pinYin;
        private List<City> cityList;
        
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getPinYin() {
            return pinYin;
        }
        
        public void setPinYin(String pinYin) {
            this.pinYin = pinYin;
        }
        
        public List<City> getCityList() {
            return cityList;
        }
        
        public void setCityList(List<City> cityList) {
            this.cityList = cityList;
        }
        
        @Override
        public String getPickerViewText() {
            return name;
        }
        
        /**
         * 城市信息
         */
        public static class City implements IPickerViewData {
            private int id;
            private String name;
            private String pinYin;
            private List<Area> cityList;
            
            public int getId() {
                return id;
            }
            
            public void setId(int id) {
                this.id = id;
            }
            
            public String getName() {
                return name;
            }
            
            public void setName(String name) {
                this.name = name;
            }
            
            public String getPinYin() {
                return pinYin;
            }
            
            public void setPinYin(String pinYin) {
                this.pinYin = pinYin;
            }
            
            public List<Area> getCityList() {
                return cityList;
            }
            
            public void setCityList(List<Area> cityList) {
                this.cityList = cityList;
            }
            
            @Override
            public String getPickerViewText() {
                return name;
            }
            
            /**
             * 区域信息
             */
            public static class Area implements IPickerViewData {
                private int id;
                private String name;
                private String pinYin;
                
                public int getId() {
                    return id;
                }
                
                public void setId(int id) {
                    this.id = id;
                }
                
                public String getName() {
                    return name;
                }
                
                public void setName(String name) {
                    this.name = name;
                }
                
                public String getPinYin() {
                    return pinYin;
                }
                
                public void setPinYin(String pinYin) {
                    this.pinYin = pinYin;
                }
                
                @Override
                public String getPickerViewText() {
                    return name;
                }
            }
        }
    }
}
