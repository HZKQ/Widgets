# Android组件库

## 组件清单

### 1. [二维码扫描-zxing](zxing)

#### 用法说明

```java
    new IntentIntegrator(this)
        .setCustomTitle("二维码扫描") //标题栏文字
        .setCustomTitleBg(R.color.colorPrimary) //标题背景
        .setCustomTitleTextColor(R.color.colorWhite) //标题文字颜色
        .setPrompt("将扫码框对准二维码开始扫描")  //扫描框底部文字
        .setCameraId(0)  // 后置摄像头
        .setBeepEnabled(true) //是否有提示音
        .setBarcodeImageEnabled(false)
        .setAlbumScanEnabled(false) //是否启用相册扫码
        .initiateScan();
```

```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result == null || result.getContents() == null) {
            return;
        }
        String contents = result.getContents();
        ToastUtil.showToast(contents);

    }
```

### 2. 图表库

#### 用法说明

### 3. 日历选择库

```java

private CalendarPicker mCalendarPicker;

    Calendar c = Calendar.getInstance();
    c.add(Calendar.YEAR, -1);
    Date mStartTime = c.getTime();
    Date mEndTime = new Date();
    if (mCalendarPicker == null) {
        mCalendarPicker = new CalendarPicker(this);
    }
    mCalendarPicker.showCalendar(mViewMask, mViewAnchor, mStartTime, mEndTime, new CalendarPicker.OnDatePickerListener() {
        @Override
        public boolean onDateSelected(List<Date> selectedDates, Date startDate, Date endDate) {
            ToastUtil.showToast(DateUtil.get_yMd(startDate)+" ~ " + DateUtil.get_yMd(endDate));
            return false;
        }
    });
```


