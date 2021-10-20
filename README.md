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

// 布局库
implementation 'com.github.HZKQ.Widgets:layout:latest.release.here'
```

## 使用说明
1. 建议下载[demoApk](./app-debug.apk)查看各组件效果
2. 具体使用说明，查阅[sample](sample) demo

---
> ## Layout库
> 该库主要由LazyColumn、LazyRow、AdapterView、GroupPlaceholder组成
> 
>>### LazyColumn、LazyRow
>> Layout库主要控件，用于组合添加到其下的LazyColumn、LazyRow、AdapterView、GroupPlaceholder
>>
>>### AdapterView
>> 用于处理列表数据，用法与RecyclerView相似，且增加了isolateViewTypes和type_flag属性，用于配置多个AdapterView item布局复用。内部不可添加其它视图，且只有作为LazyColumn、LazyRow、GroupPlaceholder 直接子节点时，才会执行预期逻辑，否则就是一个普通的View控件
>>
>>### GroupPlaceholder
>> 组视图的占位符，用于整体处理一组View的添加、移除、滚动等操作，其内部可添加其它View(如：LazyColumn、LazyRow、AdapterView、GroupPlaceholder)
>>
>> 使用方式如下(优点在于多个列表可整体滚动，且需要给列表加上头部，底部，多个列表直接增加分隔符或其它内容，都可简单通过xml配置，且可以一目了然的预览)，详细使用请参考[Demo](./sample/src/main/java/com/hzkq/widgets/layout):
>>```xml
>>···
>><com.keqiang.layout.combination.LazyColumn
>>        android:layout_width="match_parent"
>>        android:layout_height="match_parent"
>>        android:background="#ff61b4fe">
>>
>>        <TextView
>>            android:layout_width="match_parent"
>>            android:layout_height="100dp"
>>            android:background="#fffb9a55"
>>            android:gravity="center"
>>            android:text="顶部View"
>>            android:textSize="20sp" />
>>
>>        <TextView
>>            android:layout_width="match_parent"
>>            android:layout_height="40dp"
>>            android:layout_marginTop="10dp"
>>            android:background="#DDD3CC"
>>            android:gravity="center_vertical"
>>            android:paddingStart="20dp"
>>            android:paddingEnd="20dp"
>>            android:text="列表"
>>            android:textSize="14sp" />
>>
>>        <com.keqiang.layout.combination.AdapterView
>>            android:id="@+id/lazyColumn"
>>            android:layout_width="match_parent"
>>            android:layout_height="wrap_content"
>>            android:background="@color/colorBlack"
>>            app:itemCount="2"
>>            app:layout="@layout/rv_item_layout" />
>>
>>        <TextView
>>            android:layout_width="match_parent"
>>            android:layout_height="100dp"
>>            android:background="#fffb9a55"
>>            android:gravity="center"
>>            android:text="底部View"
>>            android:textSize="20sp" />
>>
>>    </com.keqiang.layout.combination.LazyColumn>
>>···
>>```
---


