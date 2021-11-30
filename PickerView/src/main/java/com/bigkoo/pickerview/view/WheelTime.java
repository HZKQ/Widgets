package com.bigkoo.pickerview.view;

import android.view.View;

import com.bigkoo.pickerview.R;
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter;
import com.bigkoo.pickerview.adapter.NumericWheelAdapter;
import com.bigkoo.pickerview.lib.WheelView;
import com.bigkoo.pickerview.listener.OnItemSelectedListener;
import com.bigkoo.pickerview.utils.ChinaDate;
import com.bigkoo.pickerview.utils.LunarCalendar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;


public class WheelTime {
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    
    private View view;
    @SuppressWarnings("rawtypes")
    private WheelView wv_year;
    @SuppressWarnings("rawtypes")
    private WheelView wv_month;
    @SuppressWarnings("rawtypes")
    private WheelView wv_day;
    private WheelView<Integer> wv_hours;
    private WheelView<Integer> wv_mins;
    private WheelView<Integer> wv_seconds;
    private int gravity;
    
    private final boolean[] type;
    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;
    private static final int DEFAULT_START_MONTH = 1;
    private static final int DEFAULT_END_MONTH = 12;
    private static final int DEFAULT_START_DAY = 1;
    private static final int DEFAULT_END_DAY = 31;
    private static final int DEFAULT_START_HOUR = 0;
    private static final int DEFAULT_END_HOUR = 23;
    private static final int DEFAULT_START_MIN = 0;
    private static final int DEFAULT_END_MIN = 59;
    private static final int DEFAULT_START_SECOND = 0;
    private static final int DEFAULT_END_SECOND = 59;
    
    private int startYear = DEFAULT_START_YEAR;
    private int endYear = DEFAULT_END_YEAR;
    private int startMonth = DEFAULT_START_MONTH;
    private int endMonth = DEFAULT_END_MONTH;
    private int startDay = DEFAULT_START_DAY;
    private int endDay = DEFAULT_END_DAY; //表示31天的
    private int startHour = DEFAULT_START_HOUR;
    private int endHour = DEFAULT_END_HOUR;
    private int startMin = DEFAULT_START_MIN;
    private int endMin = DEFAULT_END_MIN;
    private int startSecond = DEFAULT_START_SECOND;
    private int endSecond = DEFAULT_END_SECOND;
    private int currentYear;
    
    // 根据屏幕密度来指定选择器字体的大小(不同屏幕可能不同)
    private int textSize = 18;
    // 未选中文字大小
    private int textOutSize = 18;
    // 文字的颜色和分割线的颜色
    private int textColorOut;
    private int textColorCenter;
    private int dividerColor;
    // 条目间距倍数
    private float lineSpacingMultiplier = 1.6F;
    
    private WheelView.DividerType dividerType;
    
    /**
     * 是否是农历
     */
    private boolean isLunarCalendar = false;
    
    /**
     * 大月
     */
    private List<String> month_big;
    
    /**
     * 小月
     */
    private List<String> month_little;
    
    public WheelTime(View view) {
        super();
        this.view = view;
        type = new boolean[]{true, true, true, true, true, true};
        setView(view);
        init();
    }
    
    public WheelTime(View view, boolean[] type, int gravity, int textSize) {
        this(view, type, gravity, textSize, textSize);
    }
    
    public WheelTime(View view, boolean[] type, int gravity, int textSize, int textOutSize) {
        super();
        this.view = view;
        this.type = type;
        this.gravity = gravity;
        this.textSize = textSize;
        this.textOutSize = textOutSize;
        setView(view);
        init();
    }
    
    private void init() {
        // 添加大小月月份并将其转换为list,方便之后的判断
        String[] months_big = {"1", "3", "5", "7", "8", "10", "12"};
        String[] months_little = {"4", "6", "9", "11"};
        month_big = Arrays.asList(months_big);
        month_little = Arrays.asList(months_little);
    }
    
    public void setLunarCalendar(boolean isLunarCalendar) {
        this.isLunarCalendar = isLunarCalendar;
    }
    
    public boolean isLunarCalendar() {
        return isLunarCalendar;
    }
    
    public void setPicker(int year, int month, int day) {
        this.setPicker(year, month, day, 0, 0, 0);
    }
    
    public void setPicker(int year, final int month, int day, int h, int m, int s) {
        if (isLunarCalendar) {
            int[] lunar = LunarCalendar.solarToLunar(year, month + 1, day);
            setLunar(lunar[0], lunar[1], lunar[2], lunar[3] == 1, h, m, s);
        } else {
            setSolar(year, month, day, h, m, s);
        }
    }
    
    /**
     * 设置农历
     */
    @SuppressWarnings("unchecked")
    private void setLunar(int year, final int month, int day, boolean isLeap, int h, int m, int s) {
        //TODO by 汪高皖 2019/8/2 需要做的内容：农历源码没有日期范围限制，故因此不对农历做日分秒的范围限制，本项目也用不上农历
        
        // 年
        wv_year = view.findViewById(R.id.year);
        // 设置"年"的显示数据
        wv_year.setAdapter(new ArrayWheelAdapter<>(ChinaDate.getYears(startYear, endYear)));
        // 添加文字
        wv_year.setLabel("");
        wv_year.setCurrentItem(year - startYear);// 初始化时显示的数据
        wv_year.setGravity(gravity);
        
        // 月
        wv_month = view.findViewById(R.id.month);
        wv_month.setAdapter(new ArrayWheelAdapter<>(ChinaDate.getMonths(year)));
        wv_month.setLabel("");
        wv_month.setCurrentItem(month);
        wv_month.setGravity(gravity);
        
        // 日
        wv_day = view.findViewById(R.id.day);
        // 判断大小月及是否闰年,用来确定"日"的数据
        if (ChinaDate.leapMonth(year) == 0) {
            wv_day.setAdapter(new ArrayWheelAdapter<>(ChinaDate.getLunarDays(ChinaDate.monthDays(year, month))));
        } else {
            wv_day.setAdapter(new ArrayWheelAdapter<>(ChinaDate.getLunarDays(ChinaDate.leapDays(year))));
        }
        wv_day.setLabel("");
        wv_day.setCurrentItem(day - 1);
        wv_day.setGravity(gravity);
        
        wv_hours = view.findViewById(R.id.hour);
        wv_hours.setAdapter(new NumericWheelAdapter(0, 23));
        //wv_hours.setLabel(context.getString(R.string.pickerview_hours));// 添加文字
        wv_hours.setCurrentItem(h);
        wv_hours.setGravity(gravity);
        
        wv_mins = view.findViewById(R.id.min);
        wv_mins.setAdapter(new NumericWheelAdapter(0, 59));
        //wv_mins.setLabel(context.getString(R.string.pickerview_minutes));// 添加文字
        wv_mins.setCurrentItem(m);
        wv_mins.setGravity(gravity);
        
        wv_seconds = view.findViewById(R.id.second);
        wv_seconds.setAdapter(new NumericWheelAdapter(0, 59));
        //wv_seconds.setLabel(context.getString(R.string.pickerview_minutes));// 添加文字
        wv_seconds.setCurrentItem(m);
        wv_seconds.setGravity(gravity);
        
        // 添加"年"监听
        OnItemSelectedListener wheelListener_year = index -> {
            int year_num = index + startYear;
            // 判断是不是闰年,来确定月和日的选择
            wv_month.setAdapter(new ArrayWheelAdapter<>(ChinaDate.getMonths(year_num)));
            if (ChinaDate.leapMonth(year_num) != 0 && wv_month.getCurrentItem() > ChinaDate.leapMonth(year_num) - 1) {
                wv_month.setCurrentItem(wv_month.getCurrentItem() + 1);
            } else {
                wv_month.setCurrentItem(wv_month.getCurrentItem());
            }
            
            int maxItem = 29;
            if (ChinaDate.leapMonth(year_num) != 0 && wv_month.getCurrentItem() > ChinaDate.leapMonth(year_num) - 1) {
                if (wv_month.getCurrentItem() == ChinaDate.leapMonth(year_num) + 1) {
                    wv_day.setAdapter(new ArrayWheelAdapter<>(ChinaDate.getLunarDays(ChinaDate.leapDays(year_num))));
                    maxItem = ChinaDate.leapDays(year_num);
                } else {
                    wv_day.setAdapter(new ArrayWheelAdapter<>(ChinaDate.getLunarDays(ChinaDate.monthDays(year_num, wv_month.getCurrentItem()))));
                    maxItem = ChinaDate.monthDays(year_num, wv_month.getCurrentItem());
                }
            } else {
                wv_day.setAdapter(new ArrayWheelAdapter<>(ChinaDate.getLunarDays(ChinaDate.monthDays(year_num, wv_month.getCurrentItem() + 1))));
                maxItem = ChinaDate.monthDays(year_num, wv_month.getCurrentItem() + 1);
            }
            
            if (wv_day.getCurrentItem() > maxItem - 1) {
                wv_day.setCurrentItem(maxItem - 1);
            }
        };
        
        // 添加"月"监听
        OnItemSelectedListener wheelListener_month = index -> {
            int year_num = wv_year.getCurrentItem() + startYear;
            int maxItem = 29;
            if (ChinaDate.leapMonth(year_num) != 0 && index > ChinaDate.leapMonth(year_num) - 1) {
                if (wv_month.getCurrentItem() == ChinaDate.leapMonth(year_num) + 1) {
                    wv_day.setAdapter(new ArrayWheelAdapter<>(ChinaDate.getLunarDays(ChinaDate.leapDays(year_num))));
                    maxItem = ChinaDate.leapDays(year_num);
                } else {
                    wv_day.setAdapter(new ArrayWheelAdapter<>(ChinaDate.getLunarDays(ChinaDate.monthDays(year_num, index))));
                    maxItem = ChinaDate.monthDays(year_num, index);
                }
            } else {
                wv_day.setAdapter(new ArrayWheelAdapter<>(ChinaDate.getLunarDays(ChinaDate.monthDays(year_num, index + 1))));
                maxItem = ChinaDate.monthDays(year_num, index + 1);
            }
            
            if (wv_day.getCurrentItem() > maxItem - 1) {
                wv_day.setCurrentItem(maxItem - 1);
            }
            
        };
        wv_year.setOnItemSelectedListener(wheelListener_year);
        wv_month.setOnItemSelectedListener(wheelListener_month);
        
        
        if (type.length != 6) {
            throw new RuntimeException("type[] length is not 6");
        }
        wv_year.setVisibility(type[0] ? View.VISIBLE : View.GONE);
        wv_month.setVisibility(type[1] ? View.VISIBLE : View.GONE);
        wv_day.setVisibility(type[2] ? View.VISIBLE : View.GONE);
        wv_hours.setVisibility(type[3] ? View.VISIBLE : View.GONE);
        wv_mins.setVisibility(type[4] ? View.VISIBLE : View.GONE);
        wv_seconds.setVisibility(type[5] ? View.VISIBLE : View.GONE);
        setContentTextSize();
        setOutTextSize();
    }
    
    /**
     * 设置公历
     *
     * @param month 此处的month范围是0~11(0:1月，11:12月)，而本地设置的时间是从1~12，因此以下逻辑，对于month都会加1
     */
    @SuppressWarnings("unchecked")
    private void setSolar(int year, final int month, int day, int h, int m, int s) {
        /*  final Context context = view.getContext();*/
        currentYear = year;
        // 年
        wv_year = view.findViewById(R.id.year);
        // 设置"年"的显示数据
        wv_year.setAdapter(new NumericWheelAdapter(startYear, endYear));
        /*wv_year.setLabel(context.getString(R.string.pickerview_year));// 添加文字*/
        
        wv_year.setCurrentItem(year - startYear);// 初始化时显示的数据
        wv_year.setGravity(gravity);
        
        // 月
        wv_month = view.findViewById(R.id.month);
        if (startYear == endYear) {//开始年等于终止年
            wv_month.setAdapter(new NumericWheelAdapter(startMonth, endMonth));
            wv_month.setCurrentItem(month + 1 - startMonth);
        } else if (year == startYear) {
            //起始日期的月份控制
            wv_month.setAdapter(new NumericWheelAdapter(startMonth, 12));
            wv_month.setCurrentItem(month + 1 - startMonth);
        } else if (year == endYear) {
            //终止日期的月份控制
            wv_month.setAdapter(new NumericWheelAdapter(1, endMonth));
            wv_month.setCurrentItem(month);
        } else {
            wv_month.setAdapter(new NumericWheelAdapter(1, 12));
            wv_month.setCurrentItem(month);
        }
        
        /*   wv_month.setLabel(context.getString(R.string.pickerview_month));*/
        wv_month.setGravity(gravity);
        
        
        // 日
        wv_day = view.findViewById(R.id.day);
        boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
        if (startYear == endYear && startMonth == endMonth) {
            if (month_big.contains(String.valueOf(month + 1))) {
                if (endDay > 31) {
                    endDay = 31;
                }
                wv_day.setAdapter(new NumericWheelAdapter(startDay, endDay));
            } else if (month_little.contains(String.valueOf(month + 1))) {
                if (endDay > 30) {
                    endDay = 30;
                }
                wv_day.setAdapter(new NumericWheelAdapter(startDay, endDay));
            } else {
                // 闰年
                if (isLeapYear) {
                    if (endDay > 29) {
                        endDay = 29;
                    }
                } else {
                    if (endDay > 28) {
                        endDay = 28;
                    }
                }
                wv_day.setAdapter(new NumericWheelAdapter(startDay, endDay));
            }
            wv_day.setCurrentItem(day - startDay);
        } else if (year == startYear && month + 1 == startMonth) {
            // 起始日期的天数控制
            if (month_big.contains(String.valueOf(month + 1))) {
                wv_day.setAdapter(new NumericWheelAdapter(startDay, 31));
            } else if (month_little.contains(String.valueOf(month + 1))) {
                wv_day.setAdapter(new NumericWheelAdapter(startDay, 30));
            } else {
                // 闰年
                if (isLeapYear) {
                    
                    wv_day.setAdapter(new NumericWheelAdapter(startDay, 29));
                } else {
                    
                    wv_day.setAdapter(new NumericWheelAdapter(startDay, 28));
                }
            }
            wv_day.setCurrentItem(day - startDay);
        } else if (year == endYear && month + 1 == endMonth) {
            // 终止日期的天数控制
            if (month_big.contains(String.valueOf(month + 1))) {
                if (endDay > 31) {
                    endDay = 31;
                }
                wv_day.setAdapter(new NumericWheelAdapter(1, endDay));
            } else if (month_little.contains(String.valueOf(month + 1))) {
                if (endDay > 30) {
                    endDay = 30;
                }
                wv_day.setAdapter(new NumericWheelAdapter(1, endDay));
            } else {
                // 闰年
                if (isLeapYear) {
                    if (endDay > 29) {
                        endDay = 29;
                    }
                } else {
                    if (endDay > 28) {
                        endDay = 28;
                    }
                }
                wv_day.setAdapter(new NumericWheelAdapter(1, endDay));
            }
            wv_day.setCurrentItem(day - 1);
        } else {
            // 判断大小月及是否闰年,用来确定"日"的数据
            if (month_big.contains(String.valueOf(month + 1))) {
                wv_day.setAdapter(new NumericWheelAdapter(1, 31));
            } else if (month_little.contains(String.valueOf(month + 1))) {
                wv_day.setAdapter(new NumericWheelAdapter(1, 30));
            } else {
                // 闰年
                if (isLeapYear) {
                    wv_day.setAdapter(new NumericWheelAdapter(1, 29));
                } else {
                    wv_day.setAdapter(new NumericWheelAdapter(1, 28));
                }
            }
            wv_day.setCurrentItem(day - 1);
        }
        
        /* wv_day.setLabel(context.getString(R.string.pickerview_day));*/
        wv_day.setGravity(gravity);
        
        
        // 时
        wv_hours = view.findViewById(R.id.hour);
        if (startYear == endYear && startMonth == endMonth && startDay == endDay) {
            wv_hours.setAdapter(new NumericWheelAdapter(startHour, endHour));
            wv_hours.setCurrentItem(h - startHour);
        } else if (year == startYear && month + 1 == startMonth && day == startDay) {
            wv_hours.setAdapter(new NumericWheelAdapter(startHour, DEFAULT_END_HOUR));
            wv_hours.setCurrentItem(h - startHour);
        } else if (year == endYear && month + 1 == endMonth && day == endDay) {
            wv_hours.setAdapter(new NumericWheelAdapter(DEFAULT_START_HOUR, endHour));
            wv_hours.setCurrentItem(h);
        } else {
            wv_hours.setAdapter(new NumericWheelAdapter(DEFAULT_START_HOUR, DEFAULT_END_HOUR));
            wv_hours.setCurrentItem(h);
        }
        /*  wv_hours.setLabel(context.getString(R.string.pickerview_hours));// 添加文字*/
        wv_hours.setGravity(gravity);
        
        
        //分
        wv_mins = view.findViewById(R.id.min);
        if (startYear == endYear && startMonth == endMonth && startDay == endDay && startHour == endHour) {
            wv_mins.setAdapter(new NumericWheelAdapter(startMin, endMin));
            wv_mins.setCurrentItem(m - startMin);
        } else if (year == startYear && month + 1 == startMonth && day == startDay && h == startHour) {
            wv_mins.setAdapter(new NumericWheelAdapter(startMin, DEFAULT_END_MIN));
            wv_mins.setCurrentItem(m - startMin);
        } else if (year == endYear && month + 1 == endMonth && day == endDay && h == endHour) {
            wv_mins.setAdapter(new NumericWheelAdapter(DEFAULT_START_MIN, endMin));
            wv_mins.setCurrentItem(m);
        } else {
            wv_mins.setAdapter(new NumericWheelAdapter(DEFAULT_START_MIN, DEFAULT_END_MIN));
            wv_mins.setCurrentItem(m);
        }
        /* wv_mins.setLabel(context.getString(R.string.pickerview_minutes));// 添加文字*/
        wv_mins.setGravity(gravity);
        
        
        // 秒
        wv_seconds = view.findViewById(R.id.second);
        if (startYear == endYear && startMonth == endMonth && startDay == endDay && startHour == endHour && startMin == endMin) {
            wv_seconds.setAdapter(new NumericWheelAdapter(startSecond, endSecond));
            wv_seconds.setCurrentItem(s - startSecond);
        } else if (year == startYear && month + 1 == startMonth && day == startDay && h == startHour && m == startMin) {
            wv_seconds.setAdapter(new NumericWheelAdapter(startSecond, DEFAULT_END_SECOND));
            wv_seconds.setCurrentItem(s - startSecond);
        } else if (year == endYear && month + 1 == endMonth && day == endDay && h == endHour && m == endMin) {
            wv_seconds.setAdapter(new NumericWheelAdapter(DEFAULT_START_SECOND, endSecond));
            wv_seconds.setCurrentItem(s);
        } else {
            wv_seconds.setAdapter(new NumericWheelAdapter(DEFAULT_START_SECOND, DEFAULT_END_SECOND));
            wv_seconds.setCurrentItem(s);
        }
        
        /* wv_seconds.setLabel(context.getString(R.string.pickerview_seconds));// 添加文字*/
        wv_seconds.setGravity(gravity);
        
        wv_year.setOnItemSelectedListener(wheelListener_year);
        wv_month.setOnItemSelectedListener(wheelListener_month);
        wv_day.setOnItemSelectedListener(wheelListener_day);
        wv_hours.setOnItemSelectedListener(wheelListener_hour);
        wv_mins.setOnItemSelectedListener(wheelListener_min);
        if (type.length != 6) {
            throw new IllegalArgumentException("type[] length is not 6");
        }
        wv_year.setVisibility(type[0] ? View.VISIBLE : View.GONE);
        wv_month.setVisibility(type[1] ? View.VISIBLE : View.GONE);
        wv_day.setVisibility(type[2] ? View.VISIBLE : View.GONE);
        wv_hours.setVisibility(type[3] ? View.VISIBLE : View.GONE);
        wv_mins.setVisibility(type[4] ? View.VISIBLE : View.GONE);
        wv_seconds.setVisibility(type[5] ? View.VISIBLE : View.GONE);
        setContentTextSize();
        setOutTextSize();
    }
    
    /**
     * "年"监听
     */
    private final OnItemSelectedListener wheelListener_year = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(int index) {
            currentYear = index + startYear;
            setReMonth(currentYear);
        }
    };
    
    /**
     * "月"监听
     */
    private final OnItemSelectedListener wheelListener_month = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(int index) {
            //noinspection ConstantConditions
            setReDay(currentYear, ((int) wv_month.getAdapter().getItem(index)));
        }
    };
    
    /**
     * "天"监听
     */
    private final OnItemSelectedListener wheelListener_day = new OnItemSelectedListener() {
        @SuppressWarnings("ConstantConditions")
        @Override
        public void onItemSelected(int index) {
            setReHour(currentYear,
                ((int) wv_month.getAdapter().getItem(wv_month.getCurrentItem())),
                ((int) wv_day.getAdapter().getItem(index)));
        }
    };
    
    /**
     * "小时"监听
     */
    private final OnItemSelectedListener wheelListener_hour = new OnItemSelectedListener() {
        @SuppressWarnings("ConstantConditions")
        @Override
        public void onItemSelected(int index) {
            setReMin(currentYear,
                ((int) wv_month.getAdapter().getItem(wv_month.getCurrentItem())),
                ((int) wv_day.getAdapter().getItem(wv_day.getCurrentItem())),
                wv_hours.getAdapter().getItem(index));
        }
    };
    
    /**
     * "分钟"监听
     */
    private final OnItemSelectedListener wheelListener_min = new OnItemSelectedListener() {
        @SuppressWarnings("ConstantConditions")
        @Override
        public void onItemSelected(int index) {
            setReSecond(currentYear,
                ((int) wv_month.getAdapter().getItem(wv_month.getCurrentItem())),
                ((int) wv_day.getAdapter().getItem(wv_day.getCurrentItem())),
                wv_hours.getAdapter().getItem(wv_hours.getCurrentItem()),
                wv_mins.getAdapter().getItem(index));
        }
    };
    
    @SuppressWarnings("unchecked")
    private void setReMonth(int year) {
        // 记录上一次的item位置
        int currentMonthItem = wv_month.getCurrentItem();
        if (startYear == endYear) {
            // 开始年等于终止年
            wv_month.setAdapter(new NumericWheelAdapter(startMonth, endMonth));
        } else if (year == startYear) {
            // 起始日期的月份控制
            wv_month.setAdapter(new NumericWheelAdapter(startMonth, DEFAULT_END_MONTH));
        } else if (year == endYear) {
            // 终止日期的月份控制
            wv_month.setAdapter(new NumericWheelAdapter(DEFAULT_START_MONTH, endMonth));
        } else {
            wv_month.setAdapter(new NumericWheelAdapter(DEFAULT_START_MONTH, DEFAULT_END_MONTH));
        }
        
        if (currentMonthItem > wv_month.getAdapter().getItemsCount() - 1) {
            currentMonthItem = wv_month.getAdapter().getItemsCount() - 1;
            wv_month.setCurrentItem(currentMonthItem);
        }
        
        //noinspection ConstantConditions
        setReDay(currentYear, ((int) wv_month.getAdapter().getItem(currentMonthItem)));
    }
    
    /**
     * 根据选中的年月确定日期可选范围
     *
     * @param month 此处的month范围是从1~12，因此以下逻辑，对于month不用加1
     */
    @SuppressWarnings("unchecked")
    private void setReDay(int year, int month) {
        int currentItem = wv_day.getCurrentItem();
        
        // 是否是闰年
        boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
        if (startYear == endYear && startMonth == endMonth) {
            if (month_big.contains(String.valueOf(month))) {
                if (endDay > 31) {
                    endDay = 31;
                }
                wv_day.setAdapter(new NumericWheelAdapter(startDay, endDay));
            } else if (month_little.contains(String.valueOf(month))) {
                if (endDay > 30) {
                    endDay = 30;
                }
                wv_day.setAdapter(new NumericWheelAdapter(startDay, endDay));
            } else {
                // 闰年
                if (isLeapYear) {
                    if (endDay > 29) {
                        endDay = 29;
                    }
                } else {
                    if (endDay > 28) {
                        endDay = 28;
                    }
                }
                wv_day.setAdapter(new NumericWheelAdapter(startDay, endDay));
            }
        } else if (year == startYear && month == startMonth) {
            // 起始日期的天数控制
            if (month_big.contains(String.valueOf(month))) {
                wv_day.setAdapter(new NumericWheelAdapter(startDay, 31));
            } else if (month_little.contains(String.valueOf(month))) {
                wv_day.setAdapter(new NumericWheelAdapter(startDay, 30));
            } else {
                // 闰年
                if (isLeapYear) {
                    wv_day.setAdapter(new NumericWheelAdapter(startDay, 29));
                } else {
                    
                    wv_day.setAdapter(new NumericWheelAdapter(startDay, 28));
                }
            }
        } else if (year == endYear && month == endMonth) {
            // 终止日期的天数控制
            if (month_big.contains(String.valueOf(month))) {
                if (endDay > 31) {
                    endDay = 31;
                }
                wv_day.setAdapter(new NumericWheelAdapter(1, endDay));
            } else if (month_little.contains(String.valueOf(month))) {
                if (endDay > 30) {
                    endDay = 30;
                }
                wv_day.setAdapter(new NumericWheelAdapter(1, endDay));
            } else {
                // 闰年
                if (isLeapYear) {
                    if (endDay > 29) {
                        endDay = 29;
                    }
                } else {
                    if (endDay > 28) {
                        endDay = 28;
                    }
                }
                wv_day.setAdapter(new NumericWheelAdapter(1, endDay));
            }
        } else {
            // 判断大小月及是否闰年,用来确定"日"的数据
            if (month_big.contains(String.valueOf(month))) {
                wv_day.setAdapter(new NumericWheelAdapter(1, 31));
            } else if (month_little.contains(String.valueOf(month))) {
                wv_day.setAdapter(new NumericWheelAdapter(1, 30));
            } else {
                // 闰年
                if (isLeapYear) {
                    wv_day.setAdapter(new NumericWheelAdapter(1, 29));
                } else {
                    wv_day.setAdapter(new NumericWheelAdapter(1, 28));
                }
            }
        }
        
        if (currentItem > wv_day.getAdapter().getItemsCount() - 1) {
            currentItem = wv_day.getAdapter().getItemsCount() - 1;
            wv_day.setCurrentItem(currentItem);
        }
        
        //noinspection ConstantConditions
        setReHour(year, month, ((int) wv_day.getAdapter().getItem(currentItem)));
    }
    
    /**
     * 根据选中的年月日确定小时可选范围
     *
     * @param month 此处的month范围是从1~12，因此以下逻辑，对于month不用加1
     */
    private void setReHour(int year, int month, int day) {
        int currentItem = wv_hours.getCurrentItem();
        
        if (startYear == endYear && startMonth == endMonth && startDay == endDay) {
            wv_hours.setAdapter(new NumericWheelAdapter(startHour, endHour));
        } else if (year == startYear && month == startMonth && day == startDay) {
            wv_hours.setAdapter(new NumericWheelAdapter(startHour, DEFAULT_END_HOUR));
        } else if (year == endYear && month == endMonth && day == endDay) {
            wv_hours.setAdapter(new NumericWheelAdapter(DEFAULT_START_HOUR, endHour));
        } else {
            wv_hours.setAdapter(new NumericWheelAdapter(DEFAULT_START_HOUR, DEFAULT_END_HOUR));
        }
        
        if (currentItem > wv_hours.getAdapter().getItemsCount() - 1) {
            currentItem = wv_hours.getAdapter().getItemsCount() - 1;
            wv_hours.setCurrentItem(currentItem);
        }
        
        //noinspection ConstantConditions
        setReMin(year, month, day, wv_hours.getAdapter().getItem(currentItem));
    }
    
    /**
     * 根据选中的年月日小时确定分组可选范围
     *
     * @param month 此处的month范围是从1~12，因此以下逻辑，对于month不用加1
     */
    private void setReMin(int year, int month, int day, int hour) {
        int currentItem = wv_mins.getCurrentItem();
        
        if (startYear == endYear && startMonth == endMonth && startDay == endDay && startHour == endHour) {
            wv_mins.setAdapter(new NumericWheelAdapter(startMin, endMin));
        } else if (year == startYear && month == startMonth && day == startDay && hour == startHour) {
            wv_mins.setAdapter(new NumericWheelAdapter(startMin, DEFAULT_END_MIN));
        } else if (year == endYear && month == endMonth && day == endDay && hour == endHour) {
            wv_mins.setAdapter(new NumericWheelAdapter(DEFAULT_START_MIN, endMin));
        } else {
            wv_mins.setAdapter(new NumericWheelAdapter(DEFAULT_START_MIN, DEFAULT_END_MIN));
        }
        
        if (currentItem > wv_mins.getAdapter().getItemsCount() - 1) {
            currentItem = wv_mins.getAdapter().getItemsCount() - 1;
            wv_mins.setCurrentItem(currentItem);
        }
        
        //noinspection ConstantConditions
        setReSecond(year, month, day, hour, wv_mins.getAdapter().getItem(currentItem));
    }
    
    /**
     * 根据选中的年月日小时分组确定秒可选范围
     *
     * @param month 此处的month范围是从1~12，因此以下逻辑，对于month不用加1
     */
    private void setReSecond(int year, int month, int day, int hour, int min) {
        int currentItem = wv_seconds.getCurrentItem();
        
        if (startYear == endYear && startMonth == endMonth && startDay == endDay && startHour == endHour && startMin == endMin) {
            wv_seconds.setAdapter(new NumericWheelAdapter(startSecond, endSecond));
        } else if (year == startYear && month == startMonth && day == startDay && hour == startHour && min == startMin) {
            wv_seconds.setAdapter(new NumericWheelAdapter(startSecond, DEFAULT_END_SECOND));
        } else if (year == endYear && month == endMonth && day == endDay && hour == endHour && min == endMin) {
            wv_seconds.setAdapter(new NumericWheelAdapter(DEFAULT_START_SECOND, endSecond));
        } else {
            wv_seconds.setAdapter(new NumericWheelAdapter(DEFAULT_START_SECOND, DEFAULT_END_SECOND));
        }
        
        if (currentItem > wv_seconds.getAdapter().getItemsCount() - 1) {
            currentItem = wv_seconds.getAdapter().getItemsCount() - 1;
            wv_seconds.setCurrentItem(currentItem);
        }
    }
    
    private void setContentTextSize() {
        wv_day.setTextSize(textSize);
        wv_month.setTextSize(textSize);
        wv_year.setTextSize(textSize);
        wv_hours.setTextSize(textSize);
        wv_mins.setTextSize(textSize);
        wv_seconds.setTextSize(textSize);
    }
    
    private void setOutTextSize() {
        wv_day.setTextOutSize(textOutSize);
        wv_month.setTextOutSize(textOutSize);
        wv_year.setTextOutSize(textOutSize);
        wv_hours.setTextOutSize(textOutSize);
        wv_mins.setTextOutSize(textOutSize);
        wv_seconds.setTextOutSize(textOutSize);
    }
    
    private void setTextColorOut() {
        wv_day.setTextColorOut(textColorOut);
        wv_month.setTextColorOut(textColorOut);
        wv_year.setTextColorOut(textColorOut);
        wv_hours.setTextColorOut(textColorOut);
        wv_mins.setTextColorOut(textColorOut);
        wv_seconds.setTextColorOut(textColorOut);
    }
    
    private void setTextColorCenter() {
        wv_day.setTextColorCenter(textColorCenter);
        wv_month.setTextColorCenter(textColorCenter);
        wv_year.setTextColorCenter(textColorCenter);
        wv_hours.setTextColorCenter(textColorCenter);
        wv_mins.setTextColorCenter(textColorCenter);
        wv_seconds.setTextColorCenter(textColorCenter);
    }
    
    private void setDividerColor() {
        wv_day.setDividerColor(dividerColor);
        wv_month.setDividerColor(dividerColor);
        wv_year.setDividerColor(dividerColor);
        wv_hours.setDividerColor(dividerColor);
        wv_mins.setDividerColor(dividerColor);
        wv_seconds.setDividerColor(dividerColor);
    }
    
    private void setDividerType() {
        
        wv_day.setDividerType(dividerType);
        wv_month.setDividerType(dividerType);
        wv_year.setDividerType(dividerType);
        wv_hours.setDividerType(dividerType);
        wv_mins.setDividerType(dividerType);
        wv_seconds.setDividerType(dividerType);
        
    }
    
    private void setLineSpacingMultiplier() {
        wv_day.setLineSpacingMultiplier(lineSpacingMultiplier);
        wv_month.setLineSpacingMultiplier(lineSpacingMultiplier);
        wv_year.setLineSpacingMultiplier(lineSpacingMultiplier);
        wv_hours.setLineSpacingMultiplier(lineSpacingMultiplier);
        wv_mins.setLineSpacingMultiplier(lineSpacingMultiplier);
        wv_seconds.setLineSpacingMultiplier(lineSpacingMultiplier);
    }
    
    public void setLabels(String label_year, String label_month, String label_day, String label_hours, String label_mins, String label_seconds) {
        if (isLunarCalendar) {
            return;
        }
        
        if (label_year != null) {
            wv_year.setLabel(label_year);
        } else {
            wv_year.setLabel(view.getContext().getString(R.string.pickerview_year));
        }
        if (label_month != null) {
            wv_month.setLabel(label_month);
        } else {
            wv_month.setLabel(view.getContext().getString(R.string.pickerview_month));
        }
        if (label_day != null) {
            wv_day.setLabel(label_day);
        } else {
            wv_day.setLabel(view.getContext().getString(R.string.pickerview_day));
        }
        if (label_hours != null) {
            wv_hours.setLabel(label_hours);
        } else {
            wv_hours.setLabel(view.getContext().getString(R.string.pickerview_hours));
        }
        if (label_mins != null) {
            wv_mins.setLabel(label_mins);
        } else {
            wv_mins.setLabel(view.getContext().getString(R.string.pickerview_minutes));
        }
        if (label_seconds != null) {
            wv_seconds.setLabel(label_seconds);
        } else {
            wv_seconds.setLabel(view.getContext().getString(R.string.pickerview_seconds));
        }
        
    }
    
    public void setTextXOffset(int xoffset_year, int xoffset_month, int xoffset_day, int xoffset_hours, int xoffset_mins, int xoffset_seconds) {
        wv_year.setTextXOffset(-xoffset_year);
        wv_month.setTextXOffset(-xoffset_month);
        wv_day.setTextXOffset(-xoffset_day);
        wv_hours.setTextXOffset(-xoffset_hours);
        wv_mins.setTextXOffset(-xoffset_mins);
        wv_seconds.setTextXOffset(-xoffset_seconds);
    }
    
    /**
     * 设置是否循环滚动
     */
    public void setCyclic(boolean cyclic) {
        wv_year.setCyclic(cyclic);
        wv_month.setCyclic(cyclic);
        wv_day.setCyclic(cyclic);
        wv_hours.setCyclic(cyclic);
        wv_mins.setCyclic(cyclic);
        wv_seconds.setCyclic(cyclic);
    }
    
    @Nullable
    public Date getDate() {
        try {
            return DATE_FORMAT.parse(getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 返回选中的日期，格式: yyyy-MM-dd HH:mm:ss
     */
    @SuppressWarnings("ConstantConditions")
    private String getTime() {
        if (isLunarCalendar) {
            // 如果是农历 返回对应的公历时间
            return getLunarTime();
        }
        
        StringBuilder sb = new StringBuilder();
        int year = (int) wv_year.getAdapter().getItem(wv_year.getCurrentItem());
        int month = (int) wv_month.getAdapter().getItem(wv_month.getCurrentItem());
        int day = (int) wv_day.getAdapter().getItem(wv_day.getCurrentItem());
        int hour = wv_hours.getAdapter().getItem(wv_hours.getCurrentItem());
        int mins = wv_mins.getAdapter().getItem(wv_mins.getCurrentItem());
        int seconds = wv_seconds.getAdapter().getItem(wv_seconds.getCurrentItem());
        sb.append(year).append("-")
            .append(month < 9 ? "0" + month : month).append("-")
            .append(day < 9 ? "0" + day : day).append(" ")
            .append(hour < 9 ? "0" + hour : hour).append(":")
            .append(mins < 9 ? "0" + mins : mins).append(":")
            .append(seconds < 9 ? "0" + seconds : seconds);
        return sb.toString();
    }
    
    /**
     * 农历返回对应的公历时间
     */
    private String getLunarTime() {
        StringBuilder sb = new StringBuilder();
        int year = wv_year.getCurrentItem() + startYear;
        int month;
        boolean isLeapMonth = false;
        if (ChinaDate.leapMonth(year) == 0) {
            month = wv_month.getCurrentItem() + 1;
        } else {
            if ((wv_month.getCurrentItem() + 1) - ChinaDate.leapMonth(year) <= 0) {
                month = wv_month.getCurrentItem() + 1;
            } else if ((wv_month.getCurrentItem() + 1) - ChinaDate.leapMonth(year) == 1) {
                month = wv_month.getCurrentItem();
                isLeapMonth = true;
            } else {
                month = wv_month.getCurrentItem();
            }
        }
        int day = wv_day.getCurrentItem() + 1;
        int[] solar = LunarCalendar.lunarToSolar(year, month, day, isLeapMonth);
        
        int hour = wv_hours.getCurrentItem();
        int mins = wv_mins.getCurrentItem();
        int seconds = wv_seconds.getCurrentItem();
        sb.append(solar[0]).append("-")
            .append(solar[1] < 9 ? "0" + solar[1] : solar[1]).append("-")
            .append(solar[2] < 9 ? "0" + solar[2] : solar[2]).append(" ")
            .append(hour < 9 ? "0" + hour : hour).append(":")
            .append(mins < 9 ? "0" + mins : mins).append(":")
            .append(seconds < 9 ? "0" + seconds : seconds);
        return sb.toString();
    }
    
    public View getView() {
        return view;
    }
    
    public void setView(View view) {
        this.view = view;
    }
    
    public int getStartYear() {
        return startYear;
    }
    
    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }
    
    public int getEndYear() {
        return endYear;
    }
    
    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }
    
    public void setRangDate(Calendar startDate, Calendar endDate) {
        if (startDate == null && endDate != null) {
            int year = endDate.get(Calendar.YEAR);
            int month = endDate.get(Calendar.MONTH) + 1;
            int day = endDate.get(Calendar.DAY_OF_MONTH);
            int hour = endDate.get(Calendar.HOUR_OF_DAY);
            int min = endDate.get(Calendar.MINUTE);
            int second = endDate.get(Calendar.SECOND);
            if (year > startYear) {
                initEndTime(year, month, day, hour, min, second);
            } else if (year == startYear) {
                if (month > startMonth) {
                    initEndTime(year, month, day, hour, min, second);
                } else if (month == startMonth) {
                    if (day > startDay) {
                        initEndTime(year, month, day, hour, min, second);
                    } else if (day == startDay) {
                        if (hour > startHour) {
                            initEndTime(year, month, day, hour, min, second);
                        } else if (hour == startHour) {
                            if (min > startMin) {
                                initEndTime(year, month, day, hour, min, second);
                            } else if (min == startMin) {
                                if (second > startSecond) {
                                    initEndTime(year, month, day, hour, min, second);
                                }
                            }
                        }
                    }
                }
            }
            
        } else if (startDate != null && endDate == null) {
            int year = startDate.get(Calendar.YEAR);
            int month = startDate.get(Calendar.MONTH) + 1;
            int day = startDate.get(Calendar.DAY_OF_MONTH);
            int hour = startDate.get(Calendar.HOUR_OF_DAY);
            int min = startDate.get(Calendar.MINUTE);
            int second = startDate.get(Calendar.SECOND);
            if (year < endYear) {
                initStartTime(year, month, day, hour, min, second);
            } else if (year == endYear) {
                if (month < endMonth) {
                    initStartTime(year, month, day, hour, min, second);
                } else if (month == endMonth) {
                    if (day < endDay) {
                        initStartTime(year, month, day, hour, min, second);
                    } else if (day == endDay) {
                        if (hour < endHour) {
                            initStartTime(year, month, day, hour, min, second);
                        } else if (hour == endHour) {
                            if (min < endMin) {
                                initStartTime(year, month, day, hour, min, second);
                            } else if (min == endMin) {
                                if (second < endSecond) {
                                    initStartTime(year, month, day, hour, min, second);
                                }
                            }
                        }
                    }
                }
            }
            
        } else if (startDate != null && endDate != null) {
            initStartTime(startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH) + 1,
                startDate.get(Calendar.DAY_OF_MONTH),
                startDate.get(Calendar.HOUR_OF_DAY),
                startDate.get(Calendar.MINUTE),
                startDate.get(Calendar.SECOND));
            
            initEndTime(endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH) + 1,
                endDate.get(Calendar.DAY_OF_MONTH),
                endDate.get(Calendar.HOUR_OF_DAY),
                endDate.get(Calendar.MINUTE),
                endDate.get(Calendar.SECOND));
        }
    }
    
    private void initEndTime(int year, final int month, int day, int hour, int min, int second) {
        this.endYear = year;
        this.endMonth = month;
        this.endDay = day;
        this.endHour = hour;
        this.endMin = min;
        this.endSecond = second;
    }
    
    private void initStartTime(int year, final int month, int day, int hour, int min, int second) {
        this.startYear = year;
        this.startMonth = month;
        this.startDay = day;
        this.startHour = hour;
        this.startMin = min;
        this.startSecond = second;
    }
    
    /**
     * 设置间距倍数,但是只能在1.0-2.0f之间
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
    
    /**
     * Label 是否只显示中间选中项的
     */
    
    public void isCenterLabel(Boolean isCenterLabel) {
        wv_day.isCenterLabel(isCenterLabel);
        wv_month.isCenterLabel(isCenterLabel);
        wv_year.isCenterLabel(isCenterLabel);
        wv_hours.isCenterLabel(isCenterLabel);
        wv_mins.isCenterLabel(isCenterLabel);
        wv_seconds.isCenterLabel(isCenterLabel);
    }
}
