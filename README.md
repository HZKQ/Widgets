# Android组件库

[基础库MagpieX](https://github.com/zhouzhuo810/MagpieX),以下组件清单中组件均以px作为单位进行尺寸配置，因此屏幕适配依赖基础库进行适配

## 组件清单
#### 1. [二维码](zxing)
#### 2. [图表](MPChartLib)
#### 3. [日历](CalendarPickerView)
#### 4. [索引](indexbar)
#### 5. [面包屑](breadcrumb)
#### 6. [倒计时](countdownview)
#### 7. [数据选择](PickerView)
#### 8. [进度条](progressbar)
#### 9. [彩虹条](rainbowbar)
#### 10. [滑动条](seekbar)
#### 11. [定制的简单Views](views)
#### 12. [RV适配器](brvah)


## 其它库
#### 1. [图片预览](https://github.com/wanggaowan/PhotoPreview)
#### 2. [表格](https://github.com/wanggaowan/TableLite)
#### 3. [多图展示](https://github.com/zhouzhuo810/ZzImageBox)


---
## 集成

[![](https://jitpack.io/v/HZKQ/Widgets.svg)](https://jitpack.io/#HZKQ/Widgets)

集成以上所有组件：
```groovy
implementation 'com.github.HZKQ:Widgets:latest.release.here'
```

分库集成
```groovy
// 二维码
implementation 'com.github.HZKQ.Widgets:zxing:latest.release.here'

// 图表
implementation 'com.github.HZKQ.Widgets:MPChartLib:latest.release.here'

// 日历
implementation 'com.github.HZKQ.Widgets:CalendarPickerView:latest.release.here'

// 索引
implementation 'com.github.HZKQ.Widgets:indexbar:latest.release.here'

// 面包屑
implementation 'com.github.HZKQ.Widgets:breadcrumb:latest.release.here'

// 倒计时
implementation 'com.github.HZKQ.Widgets:countdownview:latest.release.here'

// 数据选择
implementation 'com.github.HZKQ.Widgets:PickerView:latest.release.here'

// 进度条
implementation 'com.github.HZKQ.Widgets:progressbar:latest.release.here'

// 彩虹条
implementation 'com.github.HZKQ.Widgets:rainbowbar:latest.release.here'

// 滑动条
implementation 'com.github.HZKQ.Widgets:seekbar:latest.release.here'

// 定制的简单Views
implementation 'com.github.HZKQ.Widgets:views:latest.release.here'

// RV适配器
implementation 'com.github.HZKQ.Widgets:brvah:latest.release.here'
```

## 使用说明
1. 建议下载[demoApk](./app-debug.apk)查看各组件效果
2. 具体使用说明，查阅[sample](sample) demo


